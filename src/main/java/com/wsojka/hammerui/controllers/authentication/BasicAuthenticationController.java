/*
 * Copyright (C) 2018 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.controllers.authentication;

import com.wsojka.hammerui.dto.authentication.BasicAuthentication;
import com.wsojka.hammerui.enums.AppTheme;
import com.wsojka.hammerui.utils.ConsoleLogger;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class BasicAuthenticationController {

    private BasicAuthentication authentication;

    @FXML
    public TextField username;

    @FXML
    public TextField password;

    @FXML
    public AnchorPane anchorRoot;

    public void initialize() {
        ConsoleLogger.info("creating BasicAuthenticationController");
        username.textProperty().addListener((observable, oldVal, newVal) -> {
            ConsoleLogger.info("username changed to {}", newVal);
            authentication.setUsername(newVal);
        });
        password.textProperty().addListener((observable, oldVal, newVal) -> {
            ConsoleLogger.info("password changed to {}", newVal);
            authentication.setPassword(newVal);
        });
    }

    public BasicAuthentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(BasicAuthentication authentication) {
        this.authentication = authentication;
        username.setText(authentication.getUsername());
        password.setText(authentication.getPassword());
    }

    public void setTheme(AppTheme theme) {
        if (theme == AppTheme.Dark) {
            anchorRoot.getStyleClass().add(AppTheme.Dark.getStyleName());
        } else {
            anchorRoot.getStyleClass().remove(AppTheme.Dark.getStyleName());
        }
    }
}
