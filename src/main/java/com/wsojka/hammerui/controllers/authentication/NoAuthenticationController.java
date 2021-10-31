/*
 * Copyright (C) 2018 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.controllers.authentication;

import com.wsojka.hammerui.dto.authentication.NoAuthentication;
import com.wsojka.hammerui.enums.AppTheme;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

public class NoAuthenticationController {

    @FXML
    public AnchorPane anchorRoot;

    private NoAuthentication authentication;

    public void initialize() {}

    public NoAuthentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(NoAuthentication authentication) {
        this.authentication = authentication;
    }

    public void setTheme(AppTheme theme) {
        if (theme == AppTheme.Dark) {
            anchorRoot.getStyleClass().add(AppTheme.Dark.getStyleName());
        } else {
            anchorRoot.getStyleClass().remove(AppTheme.Dark.getStyleName());
        }
    }
}
