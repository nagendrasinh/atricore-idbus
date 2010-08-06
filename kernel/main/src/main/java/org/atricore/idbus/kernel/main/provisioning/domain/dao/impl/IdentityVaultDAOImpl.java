package org.atricore.idbus.kernel.main.provisioning.domain.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atricore.idbus.kernel.main.provisioning.domain.IdentityVault;
import org.atricore.idbus.kernel.main.provisioning.domain.dao.IdentityVaultDAO;

import javax.jdo.PersistenceManagerFactory;

/**
 * @author <a href=mailto:sgonzalez@atricor.org>Sebastian Gonzalez Oyuela</a>
 */
public class IdentityVaultDAOImpl extends GenericDAOImpl<IdentityVault> implements IdentityVaultDAO {
    
    private static final Log logger = LogFactory.getLog(IdentityVaultDAOImpl.class);

    public IdentityVaultDAOImpl() {
        super();
    }

    public IdentityVaultDAOImpl(PersistenceManagerFactory pmf) {
        super(pmf);
    }
}
