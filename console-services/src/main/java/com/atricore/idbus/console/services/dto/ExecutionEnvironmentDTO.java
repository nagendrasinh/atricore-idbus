package com.atricore.idbus.console.services.dto;

import java.io.Serializable;
import java.util.Set;

/**
 * Author: Dejan Maric
 */
public class ExecutionEnvironmentDTO implements Serializable {

    private long id;
    private String name;
    private String displayName;
    private String description;
    private String installUri;

    private Set<ActivationDTO> activations;

    private static final long serialVersionUID = 175340870033867780L;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInstallUri() {
        return installUri;
    }

    public void setInstallUri(String installUri) {
        this.installUri = installUri;
    }

    public Set<ActivationDTO> getActivations() {
        return activations;
    }

    public void setActivations(Set<ActivationDTO> activations) {
        this.activations = activations;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}