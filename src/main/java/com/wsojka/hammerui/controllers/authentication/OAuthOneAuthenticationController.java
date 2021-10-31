/*
 * Copyright (C) 2018 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.controllers.authentication;

import com.wsojka.hammerui.dto.authentication.OAuthOneAuthentication;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import com.wsojka.hammerui.utils.ConsoleLogger;

public class OAuthOneAuthenticationController {

    private OAuthOneAuthentication authentication;

    @FXML
    private TextField consumerKey;

    @FXML
    private TextField consumerSecret;

    @FXML
    private TextField accessToken;

    @FXML
    private TextField tokenSecret;

    public void initialize() {
        ConsoleLogger.info("creating OAuthOneAuthenticationController");
        consumerKey.textProperty().addListener((observable, oldVal, newVal) -> {
            ConsoleLogger.info("consumerKey changed to {}", newVal);
            authentication.setConsumerKey(newVal);
        });
        consumerSecret.textProperty().addListener((observable, oldVal, newVal) -> {
            ConsoleLogger.info("consumerSecret changed to {}", newVal);
            authentication.setConsumerSecret(newVal);
        });
        accessToken.textProperty().addListener((observable, oldVal, newVal) -> {
            ConsoleLogger.info("accessToken changed to {}", newVal);
            authentication.setAccessToken(newVal);
        });
        tokenSecret.textProperty().addListener((observable, oldVal, newVal) -> {
            ConsoleLogger.info("tokenSecret changed to {}", newVal);
            authentication.setTokenSecret(newVal);
        });
    }

    public OAuthOneAuthentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(OAuthOneAuthentication authentication) {
        this.authentication = authentication;
        consumerKey.setText(authentication.getConsumerKey());
        consumerSecret.setText(authentication.getConsumerSecret());
        accessToken.setText(authentication.getAccessToken());
        tokenSecret.setText(authentication.getTokenSecret());
    }
}
