/*
 * Copyright (C) 2020 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.controllers;

import com.google.gson.Gson;
import com.wsojka.hammerui.dto.LicenseFile;
import com.wsojka.hammerui.persintence.PreferencesWrapper;
import com.wsojka.hammerui.persintence.PreferencesWrapperFactory;
import com.wsojka.hammerui.utils.ConsoleLogger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

public class AboutViewController {

    private PreferencesWrapper preferences = PreferencesWrapperFactory.getInstance();

    private ResourceBundle bundle;

    @FXML
    private Text buildVersion;

    @FXML
    private Text buildTime;

    @FXML
    private Text licenseText;

    @FXML
    private Text licenseDate;

    @FXML
    private AnchorPane pane;

    public void initialize() {
        bundle = ResourceBundle.getBundle("bundles.MainView", preferences.getLocale());
        String version = "";
        String date = "";
        try {
            InputStream is = getClass().getResourceAsStream("/build_version.txt");
            version = IOUtils.toString(is, StandardCharsets.UTF_8);
            is = getClass().getResourceAsStream("/build_date.txt");
            date = IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
        }
        buildVersion.setText(version);
        buildTime.setText(date);

        //read license details if available
        LicenseFile licenseFile = preferences.getLicenseFile();
        if (licenseFile != null) {
            licenseText.setText(licenseFile.getOrganization());
            licenseDate.setText(licenseFile.getDate());
        }
    }

    @FXML
    public void loadLicense(ActionEvent event) {
        ConsoleLogger.info("loading license file");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(bundle.getString("help_menu_about_load_license_label"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        File keyFile = fileChooser.showOpenDialog(buildVersion.getScene().getWindow());
        if (keyFile == null)
            return;
        String license = null;
        try {
            license = FileUtils.readFileToString(keyFile, StandardCharsets.UTF_8);
        } catch (Exception e) {
            ConsoleLogger.error("error while opening license key file: " + keyFile.getAbsolutePath(), e);
        }
        if (StringUtils.isEmpty(license)) {
            ConsoleLogger.error("invalid license key file: " + keyFile.getAbsolutePath());
            return;
        }

        //load license details
        LicenseFile licenseFile;
        Gson gson = new Gson();
        try {
            licenseFile = gson.fromJson(license, LicenseFile.class);
        } catch (Exception e) {
            ConsoleLogger.error("problem with loading license file", e);
            throw new RuntimeException(e);
        }
        if (licenseFile != null) {
            licenseText.setText(licenseFile.getOrganization());
            licenseDate.setText(licenseFile.getDate());
            preferences.setLicenseFile(licenseFile);
        }
    }

    @FXML
    public void close(ActionEvent event) {
        Stage stage = (Stage) pane.getScene().getWindow();
        stage.close();
    }
}