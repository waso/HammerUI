/*
 * Copyright (C) 2018 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.controllers.authentication;

import com.wsojka.hammerui.dto.authentication.OAuthTwoAuthentication;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import com.wsojka.hammerui.utils.ConsoleLogger;

public class OAuthTwoAuthenticationController {

    private OAuthTwoAuthentication authentication;

    @FXML
    private TextField token;

    public void initialize() {
        ConsoleLogger.info("creating OAuthTwoAuthenticationController");
        token.textProperty().addListener((observable, oldVal, newVal) -> {
            ConsoleLogger.info("username changed to {}", newVal);
            authentication.setToken(newVal);
        });
    }

    public OAuthTwoAuthentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(OAuthTwoAuthentication authentication) {
        this.authentication = authentication;
        token.setText(authentication.getToken());
    }
}
