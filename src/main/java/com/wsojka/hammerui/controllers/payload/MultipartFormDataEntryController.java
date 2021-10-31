/*
 * Copyright (C) 2018 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.controllers.payload;

import com.wsojka.hammerui.enums.PostFormEntryType;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import com.wsojka.hammerui.utils.ConsoleLogger;

import java.io.File;
import java.nio.file.Paths;

public class MultipartFormDataEntryController {

    private PostFormEntryType entryType = PostFormEntryType.TEXT;
    private String entryName = "";
    private String entryValue = "";
    private boolean wasSubmitted;

    @FXML
    private ToggleButton fileButton;

    @FXML
    private ToggleButton textButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button okButton;

    @FXML
    private TabPane tabPane;

    @FXML
    private TextField textEntryName;

    @FXML
    private TextField textEntryValue;

    @FXML
    private TextField fileFieldName;

    @FXML
    private TextField filePath;

    public void initialize() {
        textEntryName.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (((ReadOnlyBooleanProperty) observable).asObject().getValue()) {
                Platform.runLater(() -> {
                    textEntryName.requestFocus();
                    textEntryName.positionCaret(textEntryName.getText().length());
                });
            }
        });
        textEntryValue.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (((ReadOnlyBooleanProperty) observable).asObject().getValue()) {
                Platform.runLater(() -> {
                    textEntryValue.requestFocus();
                    textEntryValue.positionCaret(textEntryValue.getText().length());
                });
            }
        });
        Platform.runLater(() -> {
            if (entryType == PostFormEntryType.TEXT) {
                textEntryName.setText(entryName);
                textButton.setSelected(false);
                textButton.fire();
                textEntryName.requestFocus();
                textEntryName.positionCaret(textEntryName.getText().length());
            } else {
                fileFieldName.setText(entryName);
                textButton.setSelected(false);
                fileButton.fire();
                fileFieldName.requestFocus();
                fileFieldName.positionCaret(fileFieldName.getText().length());
            }
        });
    }

    @FXML
    public void switchToText(ActionEvent event) {
        tabPane.getSelectionModel().select(0);
    }

    @FXML
    public void switchToFile(ActionEvent event) {
        tabPane.getSelectionModel().select(1);
    }

    @FXML
    public void cancelAction(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void okAction(ActionEvent event) {
        wasSubmitted = true;
        if (tabPane.getSelectionModel().isSelected(0)) { //TEXT entry
            entryType = PostFormEntryType.TEXT;
            entryName = textEntryName.getText();
            entryValue = textEntryValue.getText();
        } else if (tabPane.getSelectionModel().isSelected(1)) { //FIELD entry
            entryType = PostFormEntryType.FILE;
            entryName = fileFieldName.getText();
            entryValue = filePath.getText();
        }
        Stage stage = (Stage) okButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void selectFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        if (getEntryType() == PostFormEntryType.FILE && Paths.get(entryValue).toFile().exists()) {
            ConsoleLogger.info(Paths.get(entryValue).getParent().toFile().getAbsolutePath());
            fileChooser.setInitialDirectory(Paths.get(entryValue).getParent().toFile());
        }
        fileChooser.setTitle("Select file");
        File file = fileChooser.showOpenDialog(tabPane.getScene().getWindow());
        if (file != null) {
            filePath.setText(file.getAbsolutePath());
            entryValue = file.getAbsolutePath();
        }
    }

    public String getEntryName() {
        return entryName;
    }

    public void setEntryName(String entryName) {
        this.entryName = entryName;
        this.textEntryName.setText(entryName);
    }

    public String getEntryValue() {
        return entryValue;
    }

    public void setEntryValue(String entryValue) {
        this.entryValue = entryValue;
        this.textEntryValue.setText(entryValue);
    }

    public PostFormEntryType getEntryType() {
        return entryType;
    }

    public void setEntryType(PostFormEntryType entryType) {
        this.entryType = entryType;
    }

    public boolean wasSubmitted() {
        return wasSubmitted;
    }

    public void setFilePath(String path) {
        this.filePath.setText(path);
        entryValue = path;
    }
}
