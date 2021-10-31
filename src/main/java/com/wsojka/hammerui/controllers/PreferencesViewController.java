/*
 * Copyright (C) 2020 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.controllers;

import com.wsojka.hammerui.enums.AppTheme;
import com.wsojka.hammerui.enums.EditorTheme;
import com.wsojka.hammerui.events.EditorThemeChangeEvent;
import com.wsojka.hammerui.persintence.ApplicationSettings;
import com.wsojka.hammerui.persintence.ProjectSettings;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class PreferencesViewController {

    private enum SettingsTabs {APPLICATION_SETTINGS, PROJECT_SETTINGS}

    private ApplicationSettings applicationSettings;

    private ProjectSettings projectSettings;

    private boolean submitted = false;

    private TabPane tabPane;

    private Runnable applyMethod;

    private AnchorPane root;

    @FXML
    private Button applicationSettingsBtn;

    @FXML
    private Button projectSettingsBtn;

    @FXML
    private Button cancelButton;

    @FXML
    private Button okButton;

    @FXML
    private TabPane settingsTabs;

    @FXML
    private CheckBox appAutoSave;

    @FXML
    private CheckBox appOpenLastProjectAtStartup;

    @FXML
    private TextField appUserAgent;

    @FXML
    private ChoiceBox<AppTheme> appTheme;

    @FXML
    private ChoiceBox<EditorTheme> editorTheme;

    @FXML
    private CheckBox projectFollowRedirections;

    @FXML
    private TextField projectConnectionTimeout;

    @FXML
    private TextField projectSocketReadTimeout;

    @FXML
    private CheckBox projectIgnoreSslErrors;

    @FXML
    private CheckBox projectUseTcpNodelay;

    @FXML
    private AnchorPane anchorRoot;

    public ApplicationSettings getApplicationSettings() {
        return applicationSettings;
    }

    public TabPane getTabPane() {
        return tabPane;
    }

    public void setTabPane(TabPane tabPane) {
        this.tabPane = tabPane;
    }

    public void setApplyMethod(Runnable applyMethod) {
        this.applyMethod = applyMethod;
    }

    public Runnable getApplyMethod() {
        return applyMethod;
    }

    public void initialize() {
    }

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
        appAutoSave.setSelected(this.applicationSettings.isAutoSaveProject());
        appOpenLastProjectAtStartup.setSelected(this.applicationSettings.isOpenLastProjectAtStartup());
        appUserAgent.setText(this.applicationSettings.getUserAgentString());

        appTheme.setItems(FXCollections.observableArrayList(AppTheme.values()));
        appTheme.getSelectionModel().select(this.applicationSettings.getAppTheme());

        editorTheme.setItems(FXCollections.observableArrayList(EditorTheme.values()));
        editorTheme.getSelectionModel().select(this.applicationSettings.getEditorTheme());
    }

    public ProjectSettings getProjectSettings() {
        return projectSettings;
    }

    public void setProjectSettings(ProjectSettings projectSettings) {
        this.projectSettings = projectSettings;
        projectFollowRedirections.setSelected(this.projectSettings.isFollowRedirects());
        projectConnectionTimeout.setText(String.valueOf(this.projectSettings.getConnectionTimeout()));
        projectSocketReadTimeout.setText(String.valueOf(this.projectSettings.getSocketReadTimeout()));
        projectIgnoreSslErrors.setSelected(this.projectSettings.isIgnoreSslErrors());
        projectUseTcpNodelay.setSelected(this.projectSettings.isUseTcpNodelay());
    }

    @FXML
    public void apply(ActionEvent event) {
        submitted = true;

        Platform.runLater(() -> {
            for (Tab tab : tabPane.getTabs()) {
                Node tabNode = tab.getContent();
                EditorThemeChangeEvent themeChangeEvent = new EditorThemeChangeEvent(this, tabNode, editorTheme.getValue());
                tabNode.fireEvent(themeChangeEvent);
            }
        });
        /* set editor theme in all open requests */


        /* set application theme */
        root.getStyleClass().remove(AppTheme.Dark.getStyleName());
        anchorRoot.getStyleClass().remove(AppTheme.Dark.getStyleName());

        if (appTheme.getValue() == AppTheme.Dark) {
            root.getStyleClass().add(AppTheme.Dark.getStyleName());
            anchorRoot.getStyleClass().add(AppTheme.Dark.getStyleName());
        }

        /* set application settings */
        applicationSettings.setAutoSaveProject(appAutoSave.isSelected());
        applicationSettings.setOpenLastProjectAtStartup(appOpenLastProjectAtStartup.isSelected());
        applicationSettings.setUserAgentString(appUserAgent.getText());
        applicationSettings.setAppTheme(appTheme.getValue());
        applicationSettings.setEditorTheme(editorTheme.getValue());

        /* set project settings */
        projectSettings.setFollowRedirects(projectFollowRedirections.isSelected());
        try {
            projectSettings.setConnectionTimeout(Integer.parseInt(projectConnectionTimeout.getText()));
        } catch (NumberFormatException ignore){}
        try {
            projectSettings.setSocketReadTimeout(Integer.parseInt(projectSocketReadTimeout.getText()));
        } catch (NumberFormatException ignore){}
        projectSettings.setIgnoreSslErrors(projectIgnoreSslErrors.isSelected());
        projectSettings.setUseTcpNodelay(projectUseTcpNodelay.isSelected());

        if (applyMethod != null)
            applyMethod.run();
    }

    @FXML
    public void submit(ActionEvent event) {
        Stage stage = (Stage) okButton.getScene().getWindow();
        stage.close();
        submitted = true;
        apply(event);
    }

    @FXML
    public void cancelDialog(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
        submitted = false;
    }

    @FXML
    public void showApplicationSettings(ActionEvent event) {
        settingsTabs.getSelectionModel().select(SettingsTabs.APPLICATION_SETTINGS.ordinal());
        applicationSettingsBtn.requestFocus();
        applicationSettingsBtn.setStyle("-fx-underline: true;");
        projectSettingsBtn.setStyle("-fx-underline: false;");
    }

    @FXML
    public void showProjectSettings(ActionEvent event) {
        settingsTabs.getSelectionModel().select(SettingsTabs.PROJECT_SETTINGS.ordinal());
        projectSettingsBtn.requestFocus();
        applicationSettingsBtn.setStyle("-fx-underline: false;");
        projectSettingsBtn.setStyle("-fx-underline: true;");
    }

    @FXML
    public void resetSettings(ActionEvent event) {
        if (settingsTabs.getSelectionModel().getSelectedIndex() == SettingsTabs.APPLICATION_SETTINGS.ordinal()) {
            ApplicationSettings empty = new ApplicationSettings();
            setApplicationSettings(empty);
        } else if(settingsTabs.getSelectionModel().getSelectedIndex() == SettingsTabs.PROJECT_SETTINGS.ordinal()) {
            ProjectSettings empty = new ProjectSettings();
            setProjectSettings(empty);
        }
    }

    public boolean wasSubmitted() {
        return submitted;
    }

    public void setRoot(AnchorPane root) {
        this.root = root;
    }

    public void setTheme(AppTheme theme) {
        if (theme == AppTheme.Dark) {
            anchorRoot.getStyleClass().add(AppTheme.Dark.getStyleName());
        } else {
            anchorRoot.getStyleClass().remove(AppTheme.Dark.getStyleName());
        }
    }
}
