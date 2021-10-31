/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.controllers.authentication;

import com.wsojka.hammerui.dto.authentication.AWSAuthentication;
import com.wsojka.hammerui.utils.ConsoleLogger;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class AWSAuthenticationController {

    private AWSAuthentication authentication;

    @FXML
    public TextField accessKey;

    @FXML
    public TextField secretKey;

    @FXML
    public TextField region;

    @FXML
    public TextField service;

    public AWSAuthentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(AWSAuthentication authentication) {
        this.authentication = authentication;
        accessKey.setText(authentication.getAccessKey());
        secretKey.setText(authentication.getSecretKey());
        region.setText(authentication.getRegion());
        service.setText(authentication.getService());
    }

    public void initialize() {
        ConsoleLogger.info("creating AWSAuthenticationController");
        accessKey.textProperty().addListener((observable, oldVal, newVal) -> {
            ConsoleLogger.info("username changed to {}", newVal);
            authentication.setAccessKey(newVal);
        });
        secretKey.textProperty().addListener((observable, oldVal, newVal) -> {
            ConsoleLogger.info("password changed to {}", newVal);
            authentication.setSecretKey(newVal);
        });
        region.textProperty().addListener((observable, oldVal, newVal) -> {
            ConsoleLogger.info("region changed to {}", newVal);
            authentication.setRegion(newVal);
        });
        service.textProperty().addListener((observable, oldVal, newVal) -> {
            ConsoleLogger.info("service changed to {}", newVal);
            authentication.setService(newVal);
        });
    }
}
