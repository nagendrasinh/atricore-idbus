package com.atricore.idbus.console.lifecycle.main.transform.transformers;

import com.atricore.idbus.console.lifecycle.main.domain.metadata.Keystore;
import com.atricore.idbus.console.lifecycle.main.domain.metadata.Location;
import com.atricore.idbus.console.lifecycle.main.domain.metadata.SamlR2ProviderConfig;
import com.atricore.idbus.console.lifecycle.main.domain.metadata.ServiceProvider;
import com.atricore.idbus.console.lifecycle.main.exception.TransformException;
import com.atricore.idbus.console.lifecycle.main.transform.IdProjectModule;
import com.atricore.idbus.console.lifecycle.main.transform.IdProjectResource;
import com.atricore.idbus.console.lifecycle.main.transform.TransformEvent;
import oasis.names.tc.saml._2_0.metadata.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atricore.idbus.capabilities.samlr2.support.binding.SamlR2Binding;
import org.atricore.idbus.kernel.main.authn.util.CipherUtil;
import org.atricore.idbus.kernel.main.util.UUIDGenerator;
import org.springframework.beans.factory.InitializingBean;
import org.w3._2000._09.xmldsig_.KeyInfoType;
import org.w3._2000._09.xmldsig_.X509DataType;
import org.w3._2001._04.xmlenc_.EncryptionMethodType;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.cert.Certificate;

/**
 * @version $Id$
 */
public class SamlR2SPTransformer extends AbstractTransformer implements InitializingBean  {

    private static final Log logger = LogFactory.getLog(SamlR2SPTransformer.class);

    private String baseSrcPath = "/org/atricore/idbus/examples/simplefederation/idau/";

    private UUIDGenerator idGenerator = new UUIDGenerator();

    private Keystore sampleKeystore;

    public void afterPropertiesSet() throws Exception {

        if (sampleKeystore.getStore() != null &&
                (sampleKeystore.getStore().getValue() == null ||
                sampleKeystore.getStore().getValue().length == 0)) {
            resolveResource(sampleKeystore.getStore());
        }

        if (sampleKeystore.getStore() == null &&
            sampleKeystore.getStore().getValue() == null ||
                sampleKeystore.getStore().getValue().length == 0) {
            logger.debug("Sample Keystore invalid or not found!");
        } else {
            logger.debug("Sample Keystore size " + sampleKeystore.getStore());
        }

    }

    @Override
    public boolean accept(TransformEvent event) {
        return event.getData() instanceof ServiceProvider;
    }

    @Override
    public void before(TransformEvent event) throws TransformException {
        try {
            ServiceProvider provider = (ServiceProvider) event.getData();
            IdProjectModule module = event.getContext().getCurrentModule();
            String baseDestPath = (String) event.getContext().get("idauPath");
            String providerBeanName = normalizeBeanName(provider.getName());

            // sp1-samlr2-metadata.xml
            IdProjectResource<EntityDescriptorType> spMetadata =  new IdProjectResource<EntityDescriptorType>(idGen.generateId(),
                    baseDestPath + providerBeanName, providerBeanName, "saml2", generateSPMetadata(provider));
            spMetadata.setClassifier("jaxb");
            module.addResource(spMetadata);
        } catch (Exception e) {
            throw new TransformException(e);
        }
    }
    
    private EntityDescriptorType generateSPMetadata(ServiceProvider provider) throws TransformException {
        SamlR2ProviderConfig cfg = (SamlR2ProviderConfig) provider.getConfig();

        Location location = provider.getLocation();

        EntityDescriptorType entityDescriptor = new EntityDescriptorType();
        // TODO : Take ID from provider entityId attribute (To be created)
        entityDescriptor.setID(idGenerator.generateId());
        entityDescriptor.setEntityID(resolveLocationUrl(location) + "/SAML2/MD");

        // SPSSODescriptor
        SPSSODescriptorType spSSODescriptor = new SPSSODescriptorType();
        // TODO : Take ID from provider entityId attribute (To be created)
        spSSODescriptor.setID(idGenerator.generateId());
        spSSODescriptor.getProtocolSupportEnumeration().add("urn:oasis:names:tc:SAML:2.0:protocol");

        // signing key descriptor
        KeyDescriptorType signingKeyDescriptor = new KeyDescriptorType();
        signingKeyDescriptor.setUse(KeyTypes.SIGNING);
        KeyInfoType signingKeyInfo = new KeyInfoType();
        X509DataType signingX509Data = new X509DataType();
        String signingCertificate = "";

        Keystore signKs = null;
        Keystore encryptKs = null;

        if (cfg != null) {

            signKs = cfg.getSigner();

            if (signKs == null && cfg.isUseSampleStore()) {
                logger.warn("Using Sample keystore for signing : " + cfg.getName());
                signKs = sampleKeystore;
            }

            encryptKs = cfg.getEncrypter();
            if (encryptKs == null && cfg.isUseSampleStore()) {
                logger.warn("Using Sample keystore for encryption : " + cfg.getName());
                encryptKs = sampleKeystore;
            }

        }

        if (signKs != null) {
            try {
                KeyStore ks = KeyStore.getInstance("PKCS#12".equals(signKs.getType()) ? "PKCS12" : "JKS");
                byte[] keystore = signKs.getStore().getValue();
                ks.load(new ByteArrayInputStream(keystore), signKs.getPassword().toCharArray());
                Certificate signerCertificate = ks.getCertificate(signKs.getCertificateAlias());
                StringWriter writer = new StringWriter();
                CipherUtil.writeBase64Encoded(writer, signerCertificate.getEncoded());
                signingCertificate = writer.toString();
            } catch (Exception e) {
                throw new TransformException(e);
            }
        } else {
            throw new TransformException("No Signer defined for " + provider.getName());
        }

        JAXBElement jaxbSigningX509Certificate = new JAXBElement(
                                    new QName("http://www.w3.org/2000/09/xmldsig#", "X509Certificate"),
                                    signingCertificate.getClass(), signingCertificate);
        signingX509Data.getX509IssuerSerialOrX509SKIOrX509SubjectName().add(jaxbSigningX509Certificate);
        JAXBElement jaxbSigningX509Data = new JAXBElement(
                                    new QName("http://www.w3.org/2000/09/xmldsig#", "X509Data"),
                                    signingX509Data.getClass(), signingX509Data);
        signingKeyInfo.getContent().add(jaxbSigningX509Data);
        signingKeyDescriptor.setKeyInfo(signingKeyInfo);
        EncryptionMethodType signingEncryptionMethod = new EncryptionMethodType();
        signingEncryptionMethod.setAlgorithm("http://www.w3.org/2001/04/xmlenc#tripledes-cbc");
        signingKeyDescriptor.getEncryptionMethod().add(signingEncryptionMethod);
        spSSODescriptor.getKeyDescriptor().add(signingKeyDescriptor);

        // encryption key descriptor
        KeyDescriptorType encryptionKeyDescriptor = new KeyDescriptorType();
        encryptionKeyDescriptor.setUse(KeyTypes.ENCRYPTION);
        KeyInfoType encryptionKeyInfo = new KeyInfoType();
        X509DataType encryptionX509Data = new X509DataType();
        String encryptionCertificate = "";
        if (encryptKs != null) {
            try {
                KeyStore ks = KeyStore.getInstance("PKCS#12".equals(encryptKs.getType()) ? "PKCS12" : "JKS");
                byte[] keystore = encryptKs.getStore().getValue();
                ks.load(new ByteArrayInputStream(keystore), encryptKs.getPassword().toCharArray());
                Certificate encrypterCertificate = ks.getCertificate(encryptKs.getCertificateAlias());
                StringWriter writer = new StringWriter();
                CipherUtil.writeBase64Encoded(writer, encrypterCertificate.getEncoded());
                encryptionCertificate = writer.toString();
            } catch (Exception e) {
                throw new TransformException(e);
            }
        } else {
            throw new TransformException("No Encrypter defined for " + provider.getName());
        }
        JAXBElement jaxbEncryptionX509Certificate = new JAXBElement(
                                    new QName("http://www.w3.org/2000/09/xmldsig#", "X509Certificate"),
                                    encryptionCertificate.getClass(), encryptionCertificate);
        encryptionX509Data.getX509IssuerSerialOrX509SKIOrX509SubjectName().add(jaxbEncryptionX509Certificate);
        JAXBElement jaxbEncryptionX509Data = new JAXBElement(
                                    new QName("http://www.w3.org/2000/09/xmldsig#", "X509Data"),
                                    encryptionX509Data.getClass(), encryptionX509Data);
        encryptionKeyInfo.getContent().add(jaxbEncryptionX509Data);
        encryptionKeyDescriptor.setKeyInfo(encryptionKeyInfo);
        EncryptionMethodType encryptionEncryptionMethod = new EncryptionMethodType();
        encryptionEncryptionMethod.setAlgorithm("http://www.w3.org/2001/04/xmlenc#tripledes-cbc");
        encryptionKeyDescriptor.getEncryptionMethod().add(encryptionEncryptionMethod);
        spSSODescriptor.getKeyDescriptor().add(encryptionKeyDescriptor);

        // services
        IndexedEndpointType artifactResolutionService = new IndexedEndpointType();
        artifactResolutionService.setBinding(SamlR2Binding.SAMLR2_SOAP.getValue());
        artifactResolutionService.setLocation(resolveLocationUrl(location) + "/SAML2/ARTIFACT/SOAP");
        artifactResolutionService.setIndex(0);
        artifactResolutionService.setIsDefault(true);
        spSSODescriptor.getArtifactResolutionService().add(artifactResolutionService);

        IndexedEndpointType artifactResolutionServiceLocal = new IndexedEndpointType();
        artifactResolutionServiceLocal.setBinding(SamlR2Binding.SAMLR2_LOCAL.getValue());
        artifactResolutionServiceLocal.setLocation(resolveLocationUrl(location) + "/SAML2/ARTIFACT/LOCAL");
        artifactResolutionServiceLocal.setIndex(0);
        artifactResolutionServiceLocal.setIsDefault(true);
        spSSODescriptor.getArtifactResolutionService().add(artifactResolutionServiceLocal);

        EndpointType singleLogoutServicePost = new EndpointType();
        singleLogoutServicePost.setBinding(SamlR2Binding.SAMLR2_POST.getValue());
        singleLogoutServicePost.setLocation(resolveLocationUrl(location) + "/SAML2/SLO/POST");
        singleLogoutServicePost.setResponseLocation(resolveLocationUrl(location) + "/SAML2/SLO_RESPONSE/POST");
        spSSODescriptor.getSingleLogoutService().add(singleLogoutServicePost);

        EndpointType singleLogoutServiceArtifact = new EndpointType();
        singleLogoutServiceArtifact.setBinding(SamlR2Binding.SAMLR2_ARTIFACT.getValue());
        singleLogoutServiceArtifact.setLocation(resolveLocationUrl(location) + "/SAML2/SLO/ARTIFACT");
        spSSODescriptor.getSingleLogoutService().add(singleLogoutServiceArtifact);

        EndpointType singleLogoutServiceSOAP = new EndpointType();
        singleLogoutServiceSOAP.setBinding(SamlR2Binding.SAMLR2_SOAP.getValue());
        singleLogoutServiceSOAP.setLocation(resolveLocationUrl(location) + "/SAML2/SLO/SOAP");
        spSSODescriptor.getSingleLogoutService().add(singleLogoutServiceSOAP);

        EndpointType singleLogoutServiceLocal = new EndpointType();
        singleLogoutServiceLocal.setBinding(SamlR2Binding.SSO_LOCAL.getValue());
        singleLogoutServiceLocal.setLocation("local://" + location.getUri().toUpperCase() + "/SAML2/SLO/LOCAL");
        spSSODescriptor.getSingleLogoutService().add(singleLogoutServiceLocal);
        
        EndpointType singleLogoutServiceRedirect = new EndpointType();
        singleLogoutServiceRedirect.setBinding(SamlR2Binding.SAMLR2_REDIRECT.getValue());
        singleLogoutServiceRedirect.setLocation(resolveLocationUrl(location) + "/SAML2/SLO/REDIR");
        singleLogoutServiceRedirect.setResponseLocation(resolveLocationUrl(location) + "/SAML2/SLO_RESPONSE/REDIR");
        spSSODescriptor.getSingleLogoutService().add(singleLogoutServiceRedirect);

        EndpointType manageNameIDServiceSOAP = new EndpointType();
        manageNameIDServiceSOAP.setBinding(SamlR2Binding.SAMLR2_SOAP.getValue());
        manageNameIDServiceSOAP.setLocation(resolveLocationUrl(location) + "/SAML2/MNI/SOAP");
        spSSODescriptor.getManageNameIDService().add(manageNameIDServiceSOAP);

        EndpointType manageNameIDServicePost = new EndpointType();
        manageNameIDServicePost.setBinding(SamlR2Binding.SAMLR2_POST.getValue());
        manageNameIDServicePost.setLocation(resolveLocationUrl(location) + "/SAML2/MNI/POST");
        manageNameIDServicePost.setResponseLocation(resolveLocationUrl(location) + "/SAML2/MNI_RESPONSE/POST");
        spSSODescriptor.getManageNameIDService().add(manageNameIDServicePost);

        EndpointType manageNameIDServiceRedirect = new EndpointType();
        manageNameIDServiceRedirect.setBinding(SamlR2Binding.SAMLR2_REDIRECT.getValue());
        manageNameIDServiceRedirect.setLocation(resolveLocationUrl(location) + "/SAML2/MNI/REDIR");
        manageNameIDServiceRedirect.setResponseLocation(resolveLocationUrl(location) + "/SAML2/MNI_RESPONSE/REDIR");
        spSSODescriptor.getManageNameIDService().add(manageNameIDServiceRedirect);

        spSSODescriptor.getNameIDFormat().add("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
        spSSODescriptor.getNameIDFormat().add("urn:oasis:names:tc:SAML:2.0:nameid-format:transient");

        IndexedEndpointType assertionConsumerService = new IndexedEndpointType();
        assertionConsumerService.setBinding(SamlR2Binding.SAMLR2_POST.getValue());
        assertionConsumerService.setLocation(resolveLocationUrl(location) + "/SAML2/ACS/POST");
        assertionConsumerService.setIndex(0);
        assertionConsumerService.setIsDefault(true);
        spSSODescriptor.getAssertionConsumerService().add(assertionConsumerService);

        entityDescriptor.getRoleDescriptorOrIDPSSODescriptorOrSPSSODescriptor().add(spSSODescriptor);

        // organization
        OrganizationType organization = new OrganizationType();
        LocalizedNameType organizationName = new LocalizedNameType();
        organizationName.setLang("en");
        organizationName.setValue("Atricore IDBUs SAMLR2 JOSSO SP Sample");
        organization.getOrganizationName().add(organizationName);
        LocalizedNameType organizationDisplayName = new LocalizedNameType();
        organizationDisplayName.setLang("en");
        organizationDisplayName.setValue("Atricore, Inc.");
        organization.getOrganizationDisplayName().add(organizationDisplayName);
        LocalizedURIType organizationURL = new LocalizedURIType();
        organizationURL.setLang("en");
        organizationURL.setValue("http://www.atricore.org");
        organization.getOrganizationURL().add(organizationURL);
        entityDescriptor.setOrganization(organization);

        // contact person
        ContactType contactPerson = new ContactType();
        contactPerson.setContactType(ContactTypeType.OTHER);
        entityDescriptor.getContactPerson().add(contactPerson);

        return entityDescriptor;
    }

    public Keystore getSampleKeystore() {
        return sampleKeystore;
    }

    public void setSampleKeystore(Keystore sampleKeystore) {
        this.sampleKeystore = sampleKeystore;
    }
}
