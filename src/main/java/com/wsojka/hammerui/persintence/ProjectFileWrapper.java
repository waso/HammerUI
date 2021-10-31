/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.persintence;

import com.wsojka.hammerui.dto.Environment;
import com.wsojka.hammerui.dto.Item;
import com.wsojka.hammerui.dto.Proxy;
import javafx.collections.ObservableList;

import java.io.File;
import java.nio.file.Path;

public interface ProjectFileWrapper {

    ObservableList<Item> getTreeItems();

    void setFile(File file);

    String getAbsolutePath();

    Path getPath();

    boolean isSaved();

    void saveProject();

    boolean loadProject(File projectFile);

    void updateWindowTitle();

    ObservableList<Environment> getEnvironments();

    void setEnvironments(ObservableList<Environment> environments);

    ObservableList<Proxy> getProxies();

    void setProxies(ObservableList<Proxy> proxies);

    String getProjectUuid();

    boolean isProjectFileCompatible();

    String getDefaultEnvironmentUuid();

    void setDefaultEnvironmentUuid(String defaultEnvironmentUuid);

    String getDefaultProxyUuid();

    void setDefaultProxyUuid(String defaultProxyUuid);

    void setProjectSettings(ProjectSettings projectSettings);

    ProjectSettings getProjectSettings();
}
