package com.atricore.idbus.console.lifecycle.main.transform.transformers.authn;

import com.atricore.idbus.console.lifecycle.main.domain.metadata.AuthenticationService;
import com.atricore.idbus.console.lifecycle.main.domain.metadata.IdentityProvider;
import com.atricore.idbus.console.lifecycle.main.domain.metadata.TwoFactorAuthentication;
import com.atricore.idbus.console.lifecycle.main.domain.metadata.WikidAuthenticationService;
import com.atricore.idbus.console.lifecycle.main.exception.TransformException;
import com.atricore.idbus.console.lifecycle.main.transform.IdProjectResource;
import com.atricore.idbus.console.lifecycle.main.transform.TransformEvent;
import com.atricore.idbus.console.lifecycle.main.transform.transformers.AbstractTransformer;
import com.atricore.idbus.console.lifecycle.support.springmetadata.model.Bean;
import com.atricore.idbus.console.lifecycle.support.springmetadata.model.Beans;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atricore.idbus.kernel.main.authn.AuthenticatorImpl;
import org.atricore.idbus.kernel.main.mediation.provider.IdentityProviderImpl;
import org.atricore.idbus.kernel.main.store.identity.SimpleIdentityStoreKeyAdapter;

import java.util.Collection;

import static com.atricore.idbus.console.lifecycle.support.springmetadata.util.BeanUtils.*;
import static com.atricore.idbus.console.lifecycle.support.springmetadata.util.BeanUtils.setPropertyValue;

public class WiKIDAuthenticationTransformer extends AbstractTransformer {

    private static final Log logger = LogFactory.getLog(WiKIDAuthenticationTransformer.class);

    @Override
    public boolean accept(TransformEvent event) {
        if (!(event.getData() instanceof TwoFactorAuthentication))
            return false;

        IdentityProvider idp = (IdentityProvider) event.getContext().getParentNode();
        TwoFactorAuthentication tfa = (TwoFactorAuthentication) event.getData();
        AuthenticationService authnService = tfa.getDelegatedAuthentication().getAuthnService();

        return authnService != null && authnService instanceof WikidAuthenticationService;
    }

    @Override
    public void before(TransformEvent event) throws TransformException {

        Beans idpBeans = (Beans) event.getContext().get("idpBeans");
        String idauPath = (String) event.getContext().get("idauPath");

        TwoFactorAuthentication twoFactorAuthn = (TwoFactorAuthentication) event.getData();
        IdentityProvider idp = (IdentityProvider) event.getContext().getParentNode();

        Bean idpBean = null;
        Collection<Bean> b = getBeansOfType(idpBeans, IdentityProviderImpl.class.getName());
        if (b.size() != 1) {
            throw new TransformException("Invalid IdP definition count : " + b.size());
        }
        idpBean = b.iterator().next();

        if (logger.isTraceEnabled())
            logger.trace("Generating Two-Factor Authentication Scheme for IdP " + idpBean.getName());

        //AuthenticationService authnService = idp.getDelegatedAuthentication().getAuthnService();
        AuthenticationService authnService = twoFactorAuthn.getDelegatedAuthentication().getAuthnService();
        
        if (authnService instanceof WikidAuthenticationService) {
            WikidAuthenticationService wikidAuthnService = (WikidAuthenticationService) authnService;

            Bean twoFactorAuthnBean = newBean(idpBeans, normalizeBeanName(twoFactorAuthn.getName()),
                "com.atricore.idbus.console.twofactor.wikid.authscheme.WiKIDAuthenticationScheme");

            // priority
            setPropertyValue(twoFactorAuthnBean, "priority", twoFactorAuthn.getPriority() + "");

            // Auth scheme name cannot be changed!
            setPropertyValue(twoFactorAuthnBean, "name", "2factor-authentication");
            setPropertyValue(twoFactorAuthnBean, "serverHost", wikidAuthnService.getServerHost());
            setPropertyValue(twoFactorAuthnBean, "serverPort", String.valueOf(wikidAuthnService.getServerPort()));
            setPropertyValue(twoFactorAuthnBean, "serverCode", wikidAuthnService.getServerCode());

            String storeBasePath = "/data/work/maven/projects/" + event.getContext().getProject().getId() +
                    "/project/idau/src/main/resources/" + idauPath + idpBean.getName() + "/";

            // CA Store
            String caStoreFileName = wikidAuthnService.getCaStore().getStore().getName() +
                    ("PKCS#12".equalsIgnoreCase(wikidAuthnService.getCaStore().getType()) ? ".p12" : "");

            IdProjectResource<byte[]> caStore = new IdProjectResource<byte[]>(idGen.generateId(),
                    idauPath + idpBean.getName() + "/", caStoreFileName,
                    "binary", wikidAuthnService.getCaStore().getStore().getValue());
            caStore.setClassifier("byte");
            event.getContext().getCurrentModule().addResource(caStore);

            String caStorePath = storeBasePath + caStoreFileName;
            
            setPropertyValue(twoFactorAuthnBean, "caCertStorePath", caStorePath);
            setPropertyValue(twoFactorAuthnBean, "caCertStorePass", wikidAuthnService.getCaStore().getPassword());

            // Client Store
            String wcStoreFileName = wikidAuthnService.getWcStore().getStore().getName() +
                    ("PKCS#12".equalsIgnoreCase(wikidAuthnService.getWcStore().getType()) ? ".p12" : "");

            IdProjectResource<byte[]> wcStore = new IdProjectResource<byte[]>(idGen.generateId(),
                    idauPath + idpBean.getName() + "/", wcStoreFileName,
                    "binary", wikidAuthnService.getWcStore().getStore().getValue());
            wcStore.setClassifier("byte");
            event.getContext().getCurrentModule().addResource(wcStore);

            String wcStorePath = storeBasePath + wcStoreFileName;

            setPropertyValue(twoFactorAuthnBean, "wcStorePath", wcStorePath);
            setPropertyValue(twoFactorAuthnBean, "wcStorePass", wikidAuthnService.getWcStore().getPassword());

            setPropertyBean(twoFactorAuthnBean, "credentialStoreKeyAdapter", newAnonymousBean(SimpleIdentityStoreKeyAdapter.class));
        }
    }

    @Override
    public Object after(TransformEvent event) throws TransformException {
        TwoFactorAuthentication twoFactorAuthn = (TwoFactorAuthentication) event.getData();
        Beans idpBeans = (Beans) event.getContext().get("idpBeans");
        Bean twoFactorAuthnBean = getBean(idpBeans, normalizeBeanName(twoFactorAuthn.getName()));
        Bean idpBean = null;
        Collection<Bean> b = getBeansOfType(idpBeans, IdentityProviderImpl.class.getName());
        if (b.size() != 1) {
            throw new TransformException("Invalid IdP definition count : " + b.size());
        }
        idpBean = b.iterator().next();

        Collection<Bean> legacyAuthenticators = getBeansOfType(idpBeans, AuthenticatorImpl.class.getName());
        if (legacyAuthenticators.size() == 1) {

            // Wire two factor authentication scheme to Authenticator
            Bean legacyAuthenticator = legacyAuthenticators.iterator().next();
            addPropertyBeansAsRefs(legacyAuthenticator, "authenticationSchemes", twoFactorAuthnBean);

            // Add new 2F Authenticator
            Bean sts = getBean(idpBeans, idpBean.getName() + "-sts");
            Bean twoFactorAuthenticator = newAnonymousBean("org.atricore.idbus.capabilities.sts.main.authenticators.TwoFactorSecurityTokenAuthenticator");
            setPropertyRef(twoFactorAuthenticator, "authenticator", legacyAuthenticator.getName());

            addPropertyBean(sts, "authenticators", twoFactorAuthenticator);

        }



        return twoFactorAuthnBean;
    }
}