package com.atricore.idbus.console.liveservices.liveupdate.main.repository.impl;

import com.atricore.idbus.console.liveservices.liveupdate.main.LiveUpdateException;
import com.atricore.idbus.console.liveservices.liveupdate.main.profile.impl.DependencyNode;
import com.atricore.idbus.console.liveservices.liveupdate.main.repository.MetadataRepository;
import com.atricore.idbus.console.liveservices.liveupdate.main.repository.MetadataRepositoryManager;
import com.atricore.idbus.console.liveservices.liveupdate.main.repository.RepositoryTransport;
import com.atricore.liveservices.liveupdate._1_0.md.*;
import com.atricore.liveservices.liveupdate._1_0.util.XmlUtils1;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URI;
import java.util.*;

/**
 * Manages a set of LiveUpdate MD repositories.
 *
 * It retrieves MD information for actual update services and stores it in the local repository representation.
 * Different transports are supported
 *
 * @author <a href=mailto:sgonzalez@atricore.org>Sebastian Gonzalez Oyuela</a>
 */
public class MetadataRepositoryManagerImpl extends AbstractRepositoryManager<MetadataRepository> implements MetadataRepositoryManager {

    private static final Log logger = LogFactory.getLog(MetadataRepositoryManagerImpl.class);

    public void init() {
        // RFU
    }

    /**
     * Adds a new repository to this manager
     */
    public synchronized void addRepository(MetadataRepository repo) throws LiveUpdateException {
        repo.init();
        repos.add(repo);
    }

    /**
     * Refresh updates indexes of all repositories
     */
    public synchronized Collection<UpdateDescriptorType> refreshRepositories() {

        // Loop over configured repos
        List<UpdateDescriptorType> newUpdates = new ArrayList<UpdateDescriptorType>();

        for (MetadataRepository repo : repos) {

            if (logger.isTraceEnabled())
                logger.trace("Refreshing repository content for " + repo.getName() + " [" + repo.getId() + "]");

            URI location = repo.getLocation();

            for (RepositoryTransport t : transports) {

                if (t.canHandle(location)) {

                    if (logger.isTraceEnabled())
                        logger.trace("Loading repository content using transport " + t.getClass().getSimpleName());

                    try {
                        byte[] idxBin = t.loadContent(location);

                        if (logger.isTraceEnabled())
                            logger.trace("Repository content is " + (idxBin == null ? 0 : idxBin.length) + " bytes length");

                        UpdatesIndexType idx = XmlUtils1.unmarshallUpdatesIndex(new String(idxBin), false);

                        if (logger.isTraceEnabled())
                            logger.trace("Found Updates Index " + idx.getID());

                        // TODO : Validate Digital signature!

                        // Store updates to in repo
                        for (UpdateDescriptorType ud : idx.getUpdateDescriptor()) {

                            if (logger.isTraceEnabled())
                                logger.trace("Found update [" + ud.getID() + "] " + ud.getDescription());
                            
                            if (!repo.hasUpdate(ud.getID())) {
                                logger.info("Found new update [" + ud.getID() + "] " + ud.getDescription());
                                newUpdates.add(ud);
                            }
                        }

                        repo.addUpdatesIndex(idx);

                    } catch (Exception e) {
                        logger.error("Cannot load updates list from repository " + repo.getName() +
                                " ["+repo.getId()+"] " + e.getMessage());

                        if (logger.isTraceEnabled())
                            logger.error("Cannot load updates list from repository " + repo.getName() +
                                    " ["+repo.getId()+"] " + e.getMessage(), e);
                    }

                }
            }
        }

        return newUpdates;

        // Contact remote service,
        // Retrieve update list,
        // Store it locally
    }

    public synchronized Collection<UpdateDescriptorType> getUpdates() throws LiveUpdateException {
        Map<String, UpdateDescriptorType> updates = new HashMap<String, UpdateDescriptorType>();

        for (MetadataRepository repo : repos) {
            for (UpdateDescriptorType ud : repo.getAvailableUpdates()) {
                updates.put(ud.getID(), ud);
            }
        }

        return updates.values();
    }

    public synchronized UpdatesIndexType getUpdatesIndex(String repoId) throws LiveUpdateException {
        for (MetadataRepository metadataRepository : repos) {
            if (metadataRepository.getId().equals(repoId)) {
                return metadataRepository.getUpdates();
            }
        }
        return null;
    }

    public synchronized UpdateDescriptorType getUpdate(String id) throws LiveUpdateException {

        for ( MetadataRepository repo : repos) {
            for (UpdateDescriptorType ud : repo.getAvailableUpdates())
                if (ud.getID().equals(id))
                    return ud;
        }
        return null;
    }

    public synchronized UpdateDescriptorType getUpdate(String group, String name, String version) throws LiveUpdateException {

        for ( MetadataRepository repo : repos) {
            for (UpdateDescriptorType ud : repo.getAvailableUpdates()) {
                for (InstallableUnitType iu : ud.getInstallableUnit()) {
                    if (iu.getGroup().equals(group) &&
                            iu.getName().equals(name) &&
                            iu.getVersion().equals(version)) {
                        return ud;
                    }
                }
            }
        }
        return null;
    }


    /**
     * Get list of dependent objects
     */
    protected List<DependencyNode> getDependents(DependencyNode dep) {
        List<DependencyNode> updates = new ArrayList<DependencyNode>();
        getDependents(dep, updates);
        return updates;
    }

    /**
     * Get list of dependent objects
     */
    protected void getDependents(DependencyNode dep, List<DependencyNode> updates) {
        if (dep.getChildren() != null) {
            updates.addAll(dep.getChildren());
            for (DependencyNode c : dep.getChildren()) {
                getDependents(c, updates);
            }
        }
    }


}
