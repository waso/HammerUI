/*
 * Copyright (C) 2018 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.controllers.authentication;

import com.wsojka.hammerui.dto.authentication.DigestAuthentication;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import com.wsojka.hammerui.utils.ConsoleLogger;

public class DigestAuthenticationController {

    private DigestAuthentication authentication;

    @FXML
    public TextField username;

    @FXML
    public TextField password;

    public void initialize() {
        ConsoleLogger.info("creating DigestAuthenticationController");
        username.textProperty().addListener((observable, oldVal, newVal) -> {
            ConsoleLogger.info("username changed to {}", newVal);
            authentication.setUsername(newVal);
        });
        password.textProperty().addListener((observable, oldVal, newVal) -> {
            ConsoleLogger.info("password changed to {}", newVal);
            authentication.setPassword(newVal);
        });
    }

    public DigestAuthentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(DigestAuthentication authentication) {
        this.authentication = authentication;
        username.setText(authentication.getUsername());
        password.setText(authentication.getPassword());
    }
}
