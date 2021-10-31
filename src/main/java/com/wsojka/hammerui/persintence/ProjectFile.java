/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.persintence;

import com.google.common.collect.Lists;
import com.wsojka.hammerui.dto.*;

import java.util.List;

public class ProjectFile {

    protected String appVersion = "";

    protected String minimumAppVersion = "";

    protected String projectUuid = "";

    protected List<Item> requests = Lists.newArrayList();

    protected List<Environment> environments = Lists.newArrayList();

    protected List<Proxy> proxies = Lists.newArrayList();

    protected String defaultEnvironmentUuid;

    protected String defaultProxyUuid;

    protected ProjectSettings projectSettings = new ProjectSettings();

    public ProjectSettings getProjectSettings() {
        return projectSettings;
    }

    public void setProjectSettings(ProjectSettings projectSettings) {
        this.projectSettings = projectSettings;
    }

    public List<Item> getItems() {
        return requests;
    }

    public void setItems(List<Item> requests) {
        this.requests = requests;
    }

    public List<Environment> getEnvironments() {
        return environments;
    }

    public void setEnvironments(List<Environment> environments) {
        this.environments = environments;
    }

    public List<Proxy> getProxies() {
        return proxies;
    }

    public void setProxies(List<Proxy> proxies) {
        this.proxies = proxies;
    }

    public String getProjectUuid() {
        return projectUuid;
    }

    public void setProjectUuid(String projectUuid) {
        this.projectUuid = projectUuid;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getMinimumAppVersion() {
        return minimumAppVersion;
    }

    public void setMinimumAppVersion(String minimumAppVersion) {
        this.minimumAppVersion = minimumAppVersion;
    }

    public String getDefaultEnvironmentUuid() {
        return defaultEnvironmentUuid;
    }

    public void setDefaultEnvironmentUuid(String defaultEnvironmentUuid) {
        this.defaultEnvironmentUuid = defaultEnvironmentUuid;
    }

    public String getDefaultProxyUuid() {
        return defaultProxyUuid;
    }

    public void setDefaultProxyUuid(String defaultProxyUuid) {
        this.defaultProxyUuid = defaultProxyUuid;
    }
}
