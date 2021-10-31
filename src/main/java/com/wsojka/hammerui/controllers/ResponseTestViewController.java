/*
 * Copyright (C) 2019 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.controllers;

import com.google.common.collect.Lists;
import com.wsojka.hammerui.dto.ResponseDetails;
import com.wsojka.hammerui.tests.ResponseTest;
import com.wsojka.hammerui.tests.TestType;
import com.wsojka.hammerui.utils.ConsoleLogger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.List;

public class ResponseTestViewController {

    @FXML
    private ChoiceBox<TestType> testType;

    @FXML
    private Label firstLabel;

    @FXML
    private Label secondLabel;

    @FXML
    private TextField firstParam;

    @FXML
    private TextField secondParam;

    @FXML
    private Label testResult;

    private static final int MAX_LABELS_COUNT = 2;

    private Label[] labels;

    private TextField[] params;

    private ResponseDetails responseDetails;

    private boolean submitted;

    private ResponseTest responseTest;

    public void initialize() {
        labels = new Label[]{firstLabel, secondLabel};
        params = new TextField[]{firstParam, secondParam};
        testType.setItems(FXCollections.observableArrayList());
        testType.getItems().setAll(TestType.values());
        testType.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            for (int i = 0; i < MAX_LABELS_COUNT; i++) {
                if (i < newValue.getLabels().size()) {
                    labels[i].setVisible(true);
                    labels[i].setText(newValue.getLabels().get(i) + ":");
                    params[i].setVisible(true);
                } else {
                    labels[i].setVisible(false);
                    params[i].setVisible(false);
                }
            }
        });
        testType.getSelectionModel().selectFirst();
    }

    public void setResponseDetails(ResponseDetails responseDetails) {
        this.responseDetails = responseDetails;
    }

    public void validateTest(ActionEvent event) {
        if (responseDetails == null) {
            testResult.setText("no response");
            testResult.setTextFill(Color.RED);
            return;
        }
        List<String> arguments = Lists.newArrayList();
        for (int i = 0; i < MAX_LABELS_COUNT; i++) {
            if (params[i].isVisible())
                arguments.add(params[i].getText());
        }
        try {
            boolean result = testType.getValue().getValidator().apply(responseDetails, arguments);
            testResult.setText(result ? "PASS" : "FAIL");
            if (result)
                testResult.setTextFill(Color.GREEN);
            else
                testResult.setTextFill(Color.RED);
            ConsoleLogger.debug("Test " + testType.getValue().name() + " with params: " + arguments + " result: " + result);
        } catch (Exception e) {
            ConsoleLogger.error("error from test: " + e);
            testResult.setText(e.getMessage());
            testResult.setTextFill(Color.RED);
        }
    }

    public ResponseTest getResponseTest() {
        return responseTest;
    }

    public void setResponseTest(ResponseTest responseTest) {
        this.responseTest = responseTest;
        testType.setValue(this.responseTest.getType());
        this.testType.setValue(responseTest.getType());
        for (int i = 0; i < responseTest.getType().getLabels().size(); i++) {
            String value = "";
            if (responseTest.getParams().size() >= i + 1)
                value = responseTest.getParams().get(i);
            params[i].setText(value);
        }
    }

    public boolean isVisible() {
        return testType.isVisible();
    }

    @FXML
    public void cancel(ActionEvent event) {
        Stage stage = (Stage) testType.getScene().getWindow();
        stage.close();
        submitted = false;
    }

    @FXML
    public void save(ActionEvent event) {
        submitted = true;
        Stage stage = (Stage) testType.getScene().getWindow();
        stage.close();
    }

    public boolean wasSubmitted() {
        return submitted;
    }

    public ChoiceBox<TestType> getTestType() {
        return testType;
    }

    public TextField[] getParams() {
        return params;
    }
}
