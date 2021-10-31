/*
 * Copyright (C) 2020 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.controllers;

import com.wsojka.hammerui.enums.AppTheme;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ValueViewController {

    @FXML
    private TextField value;

    @FXML
    private AnchorPane root;

    boolean submitted;

    @FXML
    public void cancel(ActionEvent event) {
        Stage stage = (Stage) value.getScene().getWindow();
        stage.close();
        submitted = false;
    }

    @FXML
    public void save(ActionEvent event) {
        submitted = true;
        Stage stage = (Stage) value.getScene().getWindow();
        stage.close();
    }

    public void initialize() {
        value.setText("");
        Platform.runLater(() -> {
            value.requestFocus();
        });
    }

    public String getValue() {
        return value.getText();
    }

    public void setValue(String value) {
        this.value.setText(value);
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public void setTitle(String title) {
        Stage stage = (Stage) value.getScene().getWindow();
        stage.setTitle(title);
    }

    public void setTheme(AppTheme theme) {
        if (theme == AppTheme.Dark) {
            root.getStyleClass().add(AppTheme.Dark.getStyleName());
        } else {
            root.getStyleClass().remove(AppTheme.Dark.getStyleName());
        }
    }
}
