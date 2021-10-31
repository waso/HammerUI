/*
 * Copyright (C) 2018 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.controllers.authentication;

import com.wsojka.hammerui.dto.authentication.BearerAuthentication;
import com.wsojka.hammerui.enums.AppTheme;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import com.wsojka.hammerui.utils.ConsoleLogger;
import javafx.scene.layout.AnchorPane;

public class BearerAuthenticationController {

    private BearerAuthentication authentication;

    @FXML
    public TextField token;

    @FXML
    public AnchorPane anchorRoot;

    public void initialize() {
        ConsoleLogger.info("creating BearerAuthenticationController");
        token.textProperty().addListener((observable, oldVal, newVal) -> {
            ConsoleLogger.info("username changed to {}", newVal);
            authentication.setToken(newVal);
        });
    }

    public BearerAuthentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(BearerAuthentication authentication) {
        this.authentication = authentication;
        token.setText(authentication.getToken());
    }

    public void setTheme(AppTheme theme) {
        if (theme == AppTheme.Dark) {
            anchorRoot.getStyleClass().add(AppTheme.Dark.getStyleName());
        } else {
            anchorRoot.getStyleClass().remove(AppTheme.Dark.getStyleName());
        }
    }
}
