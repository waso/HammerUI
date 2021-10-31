/*
 * Copyright (C) 2020 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.controllers;

import com.wsojka.hammerui.enums.AppTheme;
import com.wsojka.hammerui.log.InMemoryLog;
import com.wsojka.hammerui.log.InMemoryLogFactory;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ExecutorLogViewController {

    @FXML
    private TextArea text;

    @FXML
    private AnchorPane rootPane;

    private InMemoryLog inMemoryLog = InMemoryLogFactory.getInstance();

    public void initialize() {
        text.setText(inMemoryLog.getValue().getValue());
    }

    public void initializeEvents() {
        inMemoryLog.getValue().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
            text.setText(newValue);
            text.positionCaret(text.getText().length());
        }));
    }

    @FXML
    public void close(ActionEvent event) {
        Stage stage = (Stage) text.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void saveAs(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("export execution log");
        fileChooser.setInitialFileName("execution_log.txt");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*"));
        File file = fileChooser.showSaveDialog(text.getScene().getWindow());
        if (file == null)
            return;
        try {
            FileUtils.write(file, text.getText(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not export execution log");
            alert.setContentText("Could not save data to file:\n" + file.getPath());
            alert.showAndWait();
        }
    }

    public void setTheme(AppTheme theme) {
        if (theme == AppTheme.Dark) {
            rootPane.getStyleClass().add(AppTheme.Dark.getStyleName());
        } else {
            rootPane.getStyleClass().remove(AppTheme.Dark.getStyleName());
        }
    }
}
