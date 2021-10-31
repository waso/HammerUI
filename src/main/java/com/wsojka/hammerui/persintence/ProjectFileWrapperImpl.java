/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.persintence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wsojka.hammerui.Constants;
import com.wsojka.hammerui.Main;
import com.wsojka.hammerui.dto.*;
import com.wsojka.hammerui.dto.authentication.*;
import com.wsojka.hammerui.enums.AuthenticationType;
import com.wsojka.hammerui.utils.ConsoleLogger;
import com.wsojka.hammerui.utils.UIUtils;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static com.wsojka.hammerui.persintence.ObservableListTypeAdapterFactory.getObservableListTypeAdapterFactory;


class ProjectFileWrapperImpl implements ProjectFileWrapper, Serializable {

    private static final String TITLE_PREFIX = "HammerUI - ";

    private PreferencesWrapper preferences = PreferencesWrapperFactory.getInstance();

    private File file;

    private ObservableList<Item> requests = FXCollections.observableArrayList();

    private ObservableList<Environment> environments = FXCollections.observableArrayList();

    private ObservableList<Proxy> proxies = FXCollections.observableArrayList();

    private ProjectChangeListener changeListener = ProjectChangeListenerFactory.getInstance();

    private String projectUuid;

    private Gson gson;

    private String defaultEnvironmentUuid;

    private String defaultProxyUuid;

    private boolean isProjectFileCompatible = true;

    private ProjectSettings projectSettings = new ProjectSettings();

    public ProjectFileWrapperImpl() {
        requests.addListener((ListChangeListener<Item>) (c -> changeListener.setChanged()));
        projectUuid = UUID.randomUUID().toString();
        RuntimeTypeAdapterFactory<Item> itemAdapterFactory = RuntimeTypeAdapterFactory.of(Item.class, "_type")
                .registerSubtype(Request.class, "Request")
                .registerSubtype(Folder.class, "Folder");

        RuntimeTypeAdapterFactory<Authentication> authAdapterFactory = AuthenticationType.getRuntimeTypeAdapterFactory();

        GsonBuilder builder = new GsonBuilder();
        gson = builder
                .setPrettyPrinting()
                .registerTypeAdapterFactory(getObservableListTypeAdapterFactory())
                .registerTypeAdapter(SimpleBooleanProperty.class, new SimpleBooleanPropertySerializer())
                .registerTypeAdapter(SimpleBooleanProperty.class, new SimpleBooleanPropertyDeserializer())
                .registerTypeAdapterFactory(itemAdapterFactory)
                .registerTypeAdapterFactory(authAdapterFactory)
                .create();
    }

    @Override
    public ObservableList<Item> getTreeItems() {
        return requests;
    }

    @Override
    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public String getAbsolutePath() {
        if (isSaved())
            return file.getAbsolutePath();
        else
            return "";
    }

    @Override
    public Path getPath() {
        if (isSaved())
            return file.toPath();
        else
            return null;
    }

    @Override
    public boolean isSaved() {
        return file != null;
    }

    @Override
    public void saveProject() {
        if (!isSaved()) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Project");
            File projectFile = fileChooser.showSaveDialog(Main.primaryStage);
            if (projectFile == null) {
                return;
            }
            file = projectFile;
        }
        saveTestCasesToProjectFile();
        updateWindowTitle();
    }

    @Override
    public boolean loadProject(File projectFile) {
        ConsoleLogger.debug("loading project from file " + projectFile.getAbsolutePath());
        ProjectFile wrapper;
        try {
            String json = FileUtils.readFileToString(projectFile, Charsets.UTF_8);
            wrapper = gson.fromJson(json, ProjectFile.class);
            if (UIUtils.compareVersions(wrapper.getMinimumAppVersion(),
                    Constants.VERSION_MAJOR_NUMBER,
                    Constants.VERSION_MINOR_NUMBER) < 0) {
                ConsoleLogger.error("project file not compatible, please upgrade to the latest version");
                isProjectFileCompatible = false;
                throw new RuntimeException("project file not compatible");
            }
        } catch (Exception e) {
            ConsoleLogger.error("problem with loading project file", e);
            return false;
        }
        isProjectFileCompatible = true;
        /* load requests */
        requests.clear();
        if (wrapper.requests != null)
            requests.addAll(wrapper.getItems());

        /* load environments */
        environments.clear();
        if (wrapper.getEnvironments() != null)
            environments.addAll(wrapper.getEnvironments());

        /* load proxies */
        proxies.clear();
        if (wrapper.getProxies() != null)
            proxies.addAll(wrapper.getProxies());

        /* load defaults */
        defaultEnvironmentUuid = wrapper.getDefaultEnvironmentUuid();
        defaultProxyUuid = wrapper.getDefaultProxyUuid();

        changeListener.reset();
        file = projectFile;
        preferences.addPathToLastOpenProjects(Paths.get(file.getAbsolutePath()));
        projectUuid = wrapper.getProjectUuid();
        updateWindowTitle();

        /* load project settings */
        projectSettings = wrapper.getProjectSettings();
        return true;
    }

    private void saveTestCasesToProjectFile() {
        try {
            ConsoleLogger.info("saving project as " + file.getAbsolutePath());
            ProjectFile wrapper = new ProjectFile();
            wrapper.setItems(requests);
            wrapper.setEnvironments(environments);
            wrapper.setProxies(proxies);
            wrapper.setProjectUuid(projectUuid);
            wrapper.setDefaultEnvironmentUuid(defaultEnvironmentUuid);
            wrapper.setDefaultProxyUuid(defaultProxyUuid);
            wrapper.setAppVersion(Constants.VERSION_STRING);
            wrapper.setMinimumAppVersion(Constants.MINIMUM_VERSION_STRING);
            wrapper.setProjectSettings(projectSettings);
            final String json = gson.toJson(wrapper);
            FileUtils.writeStringToFile(file, json, StandardCharsets.UTF_8);
        } catch (Exception e) {
            ConsoleLogger.error("problem with saving project file", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not save data");
            alert.setContentText("Could not save data to file:\n" + file.getPath());
            alert.showAndWait();
            return;
        }
        preferences.addPathToLastOpenProjects(Paths.get(file.getAbsolutePath()));
        changeListener.reset();
    }

    @Override
    public void updateWindowTitle() {
        if (Main.primaryStage == null)
            return;
        String title;
        title = isSaved() ? TITLE_PREFIX + file.getName() : TITLE_PREFIX;
        Main.primaryStage.setTitle(title);
    }

    @Override
    public boolean isProjectFileCompatible() {
        return isProjectFileCompatible;
    }

    public ObservableList<Environment> getEnvironments() {
        return environments;
    }

    public void setEnvironments(ObservableList<Environment> environments) {
        this.environments = environments;
        changeListener.setChanged();
    }

    public ObservableList<Proxy> getProxies() {
        return proxies;
    }

    public void setProxies(ObservableList<Proxy> proxies) {
        this.proxies = proxies;
        changeListener.setChanged();
    }

    public String getProjectUuid() {
        return projectUuid;
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

    @Override
    public void setProjectSettings(ProjectSettings projectSettings) {
        this.projectSettings = projectSettings;
    }

    @Override
    public ProjectSettings getProjectSettings() {
        return projectSettings;
    }
}
