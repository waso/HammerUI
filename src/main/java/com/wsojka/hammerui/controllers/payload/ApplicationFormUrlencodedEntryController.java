/*
 * Copyright (C) 2018 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.controllers.payload;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ApplicationFormUrlencodedEntryController {

    private String entryName = "";
    private String entryValue = "";
    private boolean wasSubmitted;

    @FXML
    private Button addButton;

    @FXML
    private Button cancelButton;

    @FXML
    private TextField textEntryName;

    @FXML
    private TextField textEntryValue;

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
            textEntryName.requestFocus();
            textEntryName.positionCaret(textEntryName.getText().length());
        });
    }

    @FXML
    public void addEntry(ActionEvent event) {
        wasSubmitted = true;
        entryName = textEntryName.getText();
        entryValue = textEntryValue.getText();
        Stage stage = (Stage) addButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void cancelDialog(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public String getEntryName() {
        return entryName;
    }

    public void setEntryName(String entryName) {
        this.entryName = entryName;
        this.textEntryName.setText(this.entryName);
    }

    public String getEntryValue() {
        return entryValue;
    }

    public void setEntryValue(String entryValue) {
        this.entryValue = entryValue;
        this.textEntryValue.setText(this.entryValue);
    }

    public boolean wasSubmitted() {
        return wasSubmitted;
    }
}
