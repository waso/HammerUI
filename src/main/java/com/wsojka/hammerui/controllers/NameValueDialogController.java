/*
 * Copyright (C) 2018 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.controllers;

import com.wsojka.hammerui.enums.AppTheme;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class NameValueDialogController {

    private String name = "";

    private String value = "";

    private boolean submitted = false;

    @FXML
    private TextField nameText;

    @FXML
    private TextField valueText;

    @FXML
    private Button addButton;

    @FXML
    private Button cancelButton;

    @FXML
    private AnchorPane root;

    public void initialize() {
        nameText.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (((ReadOnlyBooleanProperty) observable).asObject().getValue()) {
                Platform.runLater(() -> {
                    nameText.requestFocus();
                    nameText.positionCaret(nameText.getText().length());
                });
            }
        });
        valueText.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (((ReadOnlyBooleanProperty) observable).asObject().getValue()) {
                Platform.runLater(() -> {
                    valueText.requestFocus();
                    valueText.positionCaret(valueText.getText().length());
                });
            }
        });
        Platform.runLater(() -> {
            nameText.requestFocus();
            nameText.positionCaret(nameText.getText().length());
        });
    }

    @FXML
    public void submit(ActionEvent event) {
        name = nameText.getText();
        value = valueText.getText();
        Stage stage = (Stage) addButton.getScene().getWindow();
        stage.close();
        submitted = true;
    }

    @FXML
    public void cancelDialog(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    String getNameText() {
        return name;
    }

    String getValueText() {
        return value;
    }

    void setNameText(String name) {
        this.name = name;
        this.nameText.setText(name);
    }

    void setValueText(String value) {
        this.value = value;
        this.valueText.setText(value);
    }

    boolean wasSubmitted() {
        return submitted;
    }

    public void setTheme(AppTheme theme) {
        if (theme == AppTheme.Dark) {
            root.getStyleClass().add(AppTheme.Dark.getStyleName());
        } else {
            root.getStyleClass().remove(AppTheme.Dark.getStyleName());
        }
    }
}
