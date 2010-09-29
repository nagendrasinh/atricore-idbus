package com.atricore.idbus.console.lifecycle.main.impl;

import com.atricore.idbus.console.lifecycle.main.domain.IdentityAppliance;
import com.atricore.idbus.console.lifecycle.main.domain.IdentityApplianceDeployment;
import com.atricore.idbus.console.lifecycle.main.domain.dao.IdentityApplianceDAO;
import com.atricore.idbus.console.lifecycle.main.domain.metadata.*;
import com.atricore.idbus.console.lifecycle.main.exception.ApplianceNotFoundException;
import com.atricore.idbus.console.lifecycle.main.exception.ApplianceValidationException;
import com.atricore.idbus.console.lifecycle.main.exception.IdentityServerException;
import com.atricore.idbus.console.lifecycle.main.spi.ApplianceValidator;
import com.atricore.idbus.console.lifecycle.main.spi.IdentityApplianceDefinitionWalker;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import java.util.*;

/**
 * @author <a href=mailto:sgonzalez@atricor.org>Sebastian Gonzalez Oyuela</a>
 */
public class ApplianceValidatorImpl extends AbstractApplianceDefinitionVisitor
        implements ApplianceValidator {

    private static final Log logger = LogFactory.getLog(ApplianceValidatorImpl.class);

    private IdentityApplianceDefinitionWalker walker;

    private IdentityApplianceDAO dao;

    private static ThreadLocal<ValidationContext> ctx = new ThreadLocal<ValidationContext>();

    public void validate(IdentityAppliance appliance) throws ApplianceValidationException {
        validate(appliance, Operation.ANY);
    }

    public void validate(IdentityAppliance appliance, Operation op) throws ApplianceValidationException {

        ValidationContext vctx = new ValidationContext();
        vctx.setOperation(op);
        ctx.set(vctx);

        try {
            arrive(appliance);
            walker.walk(appliance.getIdApplianceDefinition(), this);
            arrive(appliance.getIdApplianceDeployment());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            addError("Fatal error", e);
        }

        if (vctx.getErrors().size() > 0) {
            throw new ApplianceValidationException(appliance, vctx.getErrors());
        }
    }

    public IdentityApplianceDefinitionWalker getWalker() {
        return walker;
    }

    public void setWalker(IdentityApplianceDefinitionWalker walker) {
        this.walker = walker;
    }

    public IdentityApplianceDAO getDao() {
        return dao;
    }

    public void setDao(IdentityApplianceDAO dao) {
        this.dao = dao;
    }

    /**
     * @param appliance
     */
    public void arrive(IdentityAppliance appliance) {

        if (getOperation().equals(Operation.UPDATE)) {

            if (appliance.getId() < 1) {
                addError("Appliance instance has invalid ID " + appliance.getId() + ", is it new ?" );
                return;
            }

            // Make sure that the appliance exists!
            if (!dao.exists(appliance.getId())) {
                addError("Appliance does not exist with ID : " + appliance.getId());
                return ;
            }

            IdentityAppliance oldAppliance = dao.findById(appliance.getId());
            ctx.get().setOldAppliance(oldAppliance);

            if (!oldAppliance.getState().equals(appliance.getState()))
                addError("Identity Appliance state cannot be modified");

            if (oldAppliance.getIdApplianceDefinition() == null &&
                    appliance.getIdApplianceDefinition() !=null)
                addError("Identity Appliance deployment information cannot be added");

            if (oldAppliance.getIdApplianceDefinition() != null &&
                    appliance.getIdApplianceDefinition() ==null)
                addError("Identity Appliance deployment information cannot be deleted");

        }
    }

    public void arrive(IdentityApplianceDeployment applianceDep) {
        switch (getOperation()) {
            case UPDATE:
                IdentityApplianceDeployment oldApplianceDep = ctx.get().getOldAppliance().getIdApplianceDeployment();

                if (oldApplianceDep == null) {
                    // TODO : Somewhere this dep.info is  added, check it! addError("Deployment information cannot be added");
                    return;
                }

                // TODO : Validate other lifecycle related infomation ...
                if (oldApplianceDep.getDeployedRevision() != applianceDep.getDeployedRevision())
                    addError("Identity Appliance deployed revision cannot be modified");

                if (oldApplianceDep.getDeploymentTime() != null && !oldApplianceDep.getDeploymentTime().equals(applianceDep.getDeploymentTime()))
                    addError("Identity Appliance deployment time cannot be modified");
                break;
            case ADD:
            case IMPORT:
                if (applianceDep != null)
                    addError("New appliances can't have deployment information ");
                break;
            default:
                break;

        }

    }

    @Override
    public void arrive(IdentityApplianceDefinition node) throws Exception {

        validateName("Appliance name", node.getName(), node);
        validateDisplayName("Appliance display name", node.getDisplayName());
        validatePackageName("Appliance namespace", node.getNamespace());
        validateLocation("Appliance", node.getLocation());

        if (getOperation() == Operation.ADD ||
            getOperation() == Operation.IMPORT) {

            try {
                IdentityAppliance oldAppliance = dao.findByName(node.getName());
                addError("Appliance name already in use '" +
                        node.getName() + "' by " + oldAppliance.getId());

            } catch (ApplianceNotFoundException e) {
                // OK!
            }

        }


    }

    @Override
    public void arrive(IdentityProvider node) throws Exception {
        validateName("IDP name", node.getName(), node);
        validateDisplayName("IDP display name", node.getDisplayName());

        validateLocation("IDP", node.getLocation(), true);

        for (FederatedConnection fcA : node.getFederatedConnectionsA()) {
            if (fcA.getRoleA() != node) {
                addError("Federated Connection A does not point to this provider " + node.getName() + "["+fcA.getRoleA().getName()+"]");
            }
        }

        for (FederatedConnection fcB : node.getFederatedConnectionsB()) {
            if (fcB.getRoleB() != node) {
                addError("Federated Connection B does not point to this provider " + node.getName() + "["+fcB.getRoleA().getName()+"]");
            }
        }

        if (node.getAuthenticationMechanisms() == null || node.getAuthenticationMechanisms().size() < 1) {
            addError("Identity Provider needs at least one Authentication Mechanism");
        }

        if (node.getIdentityLookup() == null)
            addError("Identity Provider needs an Indentity Lookup connection");

        if (node.getConfig() ==null)
            addError("No configuration found for IDP " + node.getName());
        else {
            if (node.getConfig() instanceof SamlR2ProviderConfig) {
                SamlR2ProviderConfig samlCfg = (SamlR2ProviderConfig) node.getConfig();

                if (samlCfg.getSigner() == null && !samlCfg.isUseSampleStore()) {
                    addError("No singer found and use sample store is set to false for " + node.getName());
                }

                if (samlCfg.getEncrypter() == null && !samlCfg.isUseSampleStore()) {
                    addError("No encrypter found and use sample store is set to false for " + node.getName());
                }

            }
        }
    }

    @Override
    public void arrive(ServiceProvider node) throws Exception {
        validateName("SP name", node.getName(), node);
        validateDisplayName("SP display name", node.getDisplayName());

        validateLocation("SP", node.getLocation(), true);

        int preferred = 0;

        for (FederatedConnection fcA : node.getFederatedConnectionsA()) {

            if (fcA.getChannelA() instanceof IdentityProviderChannel) {
                IdentityProviderChannel c = (IdentityProviderChannel) fcA.getChannelA();
                if (c.isPreferred())
                    preferred ++;
            } else {
                addError("Federated Connection A does not relate this SP with an IDP : " + fcA.getChannelA().getClass().getSimpleName());
            }

            if (fcA.getRoleA() != node) {
                addError("Federated Connection A does not point to this provider " + node.getName() + "["+fcA.getRoleA().getName()+"]");
            }
        }

        for (FederatedConnection fcB : node.getFederatedConnectionsB()) {

            if (fcB.getChannelB() instanceof IdentityProviderChannel) {
                IdentityProviderChannel c = (IdentityProviderChannel) fcB.getChannelB();
                if (c.isPreferred())
                    preferred ++;
            } else {
                addError("Federated Connection B does not relate this SP with an IDP : " + fcB.getChannelB().getClass().getSimpleName());
            }

            if (fcB.getRoleB() != node) {
                addError("Federated Connection B does not point to this provider " + node.getName() + "["+fcB.getRoleA().getName()+"]");
            }
        }

        if (preferred < 1)
            addError("No preferred Identity Provider Channel defined for SP " + node.getName());

        if (preferred > 1)
            addError("Too many preferred IDP Channels defined for SP " + node.getName() + ", found " + preferred);

        if (node.getConfig() ==null)
            addError("No provider configuration found for SP " + node.getName());
        else {
            if (node.getConfig() instanceof SamlR2ProviderConfig) {
                SamlR2ProviderConfig samlCfg = (SamlR2ProviderConfig) node.getConfig();

                if (samlCfg.getSigner() == null && !samlCfg.isUseSampleStore()) {
                    addError("No singer found and use sample store is set to false for " + node.getName());
                }

                if (samlCfg.getEncrypter() == null && !samlCfg.isUseSampleStore()) {
                    addError("No encrypter found and use sample store is set to false for " + node.getName());
                }

            }
        }

        if (node.getActivation() == null) {
            addError("Local Serivice Provider requires an activation connection " + node.getName());
        }
    }

    @Override
    public void arrive(FederatedConnection node) throws Exception {
        validateName("Federated Connection name", node.getName(), node);

        // Role A
        if (node.getRoleA() == null)
            addError("Federated Connection roleA cannot be null for " + node.getName());

        if (node.getChannelA() == null)
            addError("Federated Connection channelA cannot be null for " + node.getName());

        // Role B
        if (node.getRoleB() == null)
            addError("Federated Connection roleB cannot be null for " + node.getName());

        if (node.getChannelB() == null)
            addError("Federated Connection channelB cannot be null for " + node.getName());

        // TODO !
    }

    @Override
    public void arrive(IdentityLookup node) throws Exception {
        validateName("Identity Lookup name", node.getName(), node);

        if (node.getProvider() == null)
            addError("Identity Lookup provider cannot be null " + node.getName());

        if (node.getProvider() == null)
            addError("Identity Lookup " + node.getName() + " Provider cannot be null");
        else {
            if (node.getProvider().getIdentityLookup() != node) {
                addError("Provider Identity Lookup is not this Identity Lookup" +
                        node.getName() +
                        " ["+node.getProvider().getIdentityLookup()+"]");
            }
        }

        if (node.getIdentitySource() == null) {
            addError("Identity Lookup " + node.getName() + " Identity Source cannot be null");
        }

    }

    @Override
    public void arrive(JOSSOActivation node) throws Exception {

        validateName("JOSSO Activation name", node.getName(), node);
        validateDisplayName("JOSSO Activation display name", node.getDisplayName());

        if (node.getPartnerAppId() == null)
            addError("JOSSO Activation partner app. ID cannot be null ");

        validateLocation("JOSSO Activation partner app.", node.getPartnerAppLocation());

        if (node.getSp() == null)
            addError("JOSSO Activation " + node.getName() + " SP cannot be null");
        else {
            if (node.getSp().getActivation() != node) {
                addError("SP Activation is not this activation " +
                        node.getName() +
                        " ["+node.getSp().getActivation().getName()+"]");
            }
        }

    }

    @Override
    public void arrive(ExecutionEnvironment node) throws Exception {

        validateName("Execution Environment name" , node.getName(), node);
        validateDisplayName("Execution Environment display name" , node.getDisplayName());

        if (node.getPlatformId() == null)
            addError("Execution Environment platform ID cannot be null");

        if (node.getInstallUri() == null)
            addError("Execution Environment install URI cannot be null");

    }

    @Override
    public void arrive(DbIdentitySource node) throws Exception {
        validateName("DB Identity Source name" , node.getName(), node);
        validateDisplayName("DB Identity Source display name" , node.getDisplayName());

        // TODO !
    }

    @Override
    public void arrive(EmbeddedIdentitySource node) throws Exception {
        validateName("Embedded Identity Source name" , node.getName(), node);
        validateDisplayName("Ebmedded Identity Source display name" , node.getDisplayName());
        // TODO !
    }

    @Override
    public void arrive(LdapIdentitySource node) throws Exception {
        validateName("LDAP Identity Source name" , node.getName(), node);
        validateDisplayName("LDAP Identity Source display name" , node.getDisplayName());

        // TODO !
    }

    @Override
    public void arrive(XmlIdentitySource node) throws Exception {
        validateDisplayName("XML Identity Source name" , node.getName());
        validateDisplayName("XML Identity Source display name" , node.getDisplayName());

        if (node.getXmlUrl() == null)
            addError("XML Idenity Source must define a XML Url");
    }


    @Override
    public void arrive(ServiceProviderChannel node) throws Exception {
        validateName("Service Provider channel", node.getName(), node);
        if (node.isOverrideProviderSetup())
            validateLocation("Serivce Provider channel ", node.getLocation());
    }

    @Override
    public void arrive(IdentityProviderChannel node) throws Exception {
        validateName("Identity Provider channel", node.getName(), node);
        if (node.isOverrideProviderSetup())
            validateLocation("Identity Provider channel ", node.getLocation());


    }




    // ---------------------------------------------------------------------
    // UTILS
    // ---------------------------------------------------------------------

    protected void validateName(String propertyName, String name, Object o) {

        //
        if (name == null || name.length() == 0) {
            addError(propertyName + " cannot be null or empty");
            return;
        }

        for (int i = 0 ; i < name.length() ; i ++) {
            char c = name.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                if (c != '-') {
                    addError(propertyName + " must contain only letters, numbers and '-' characters, value : " + name);
                }
            }
        }

        ValidationContext vctx = ctx.get();
        if (vctx.isNameUsed(name, o))
            addError(propertyName + " already in use in other component : " + name);

        vctx.registerName(name, o);

    }

    protected void validateDisplayName(String propertyName, String name) {

        /* TODO ! Disabled for now ... 
        if (name == null || name.length() == 0) {
            addError(propertyName + " cannot be null or empty");
            return;
        } */

    }


    protected void validatePackageName(String propertyName, String pkgName) {
        if (pkgName == null || pkgName.length() == 0) {
            addError(propertyName + " cannot be null or empty");
            return;
        }

        for (int i = 0 ; i < pkgName.length() ; i ++) {
            char c = pkgName.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                if (c != '.') {
                    addError(propertyName + " must contain only letters, numbers and '.' characters. value : " + pkgName);
                    return ;
                }
            }

        }
    }

    protected void validateLocation(String propertyName, Location location) {
        validateLocation(propertyName, location, false);
    }

    protected void validateLocation(String propertyName, Location location, boolean validateUri) {
        if (location == null) {
            addError(propertyName + " location cannot be null");
            return ;
        }

        if (location.getHost() == null)
            addError(propertyName + " location host cannot be null");

        if (location.getPort() == 0)
            addError(propertyName + " location port cannot be zero");

        if (location.getProtocol() == null)
            addError(propertyName + " location protocol cannot be null");

        if (location.getContext() == null)
            addError(propertyName + " location context cannot be null");
        else {
            if (location.getContext().startsWith("/"))
                addError(propertyName + " location context must be relative (do not start it with '/')");

            if (location.getContext().lastIndexOf("/") > 1)
                addError(propertyName + " location context must not be a path (do not use '/')");

        }

        if (validateUri) {
            if (location.getUri() == null)
                addError(propertyName + " location URI cannot be null");
            else if (location.getUri().startsWith("/"))
                addError(propertyName + " location URI must be relative (do not start it with '/')");
        }




    }

    protected boolean isNumeric(String v) {
        for (int i = 0 ; i < v.length() ; i ++) {
            if (!Character.isDigit(v.charAt(i)))
                return false;
        }
        return true;

    }

    protected void addError(String msg, Throwable t) {
        ctx.get().getErrors().add(new ValidationError(msg, t));
    }

    protected void addError(String msg) {
        ctx.get().getErrors().add(new ValidationError(msg));
    }

    protected Operation getOperation() {
        return ctx.get().getOperation();
    }

    protected class ValidationContext {

        private IdentityAppliance oldAppliance;

        private Operation operation;

        private Map<String, Set<Object>> usedNames = new HashMap<String, Set<Object>>();

        boolean isNameUsed(String name, Object o) {
            Set objs = usedNames.get(name);
            if (objs == null) {
                objs = new HashSet<Object>();
                usedNames.put(name, objs);
            }

            return objs.size() > 0 && !objs.contains(o);

        }

        void registerName(String name, Object o) {
            Set objs = usedNames.get(name);
            if (objs == null) {
                objs = new HashSet<Object>();
                usedNames.put(name, objs);
            }
            objs.add(o);
        }

        private List<ValidationError> errors = new ArrayList<ValidationError>();

        public List<ValidationError> getErrors() {
            return errors;
        }

        public void setOperation(Operation op) {
            this.operation = op;
        }

        public Operation getOperation() {
            return operation;
        }

        public void setOldAppliance(IdentityAppliance oldAppliance) {
            this.oldAppliance = oldAppliance;
        }

        public IdentityAppliance getOldAppliance() {
            return oldAppliance;
        }
    }


}