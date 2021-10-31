/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.dto.authentication;

import com.wsojka.hammerui.controllers.authentication.BearerAuthenticationController;
import com.wsojka.hammerui.enums.AppTheme;
import com.wsojka.hammerui.enums.AuthenticationType;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;

public class BearerAuthentication extends Authentication implements Serializable {

    @Serial
    private static final long serialVersionUID = -2334264487341107053L;

    private String token;

    public BearerAuthentication() {
        authenticationType = AuthenticationType.BEARER;
    }

    public void setAuthenticationDetailsPane(Pane authenticationDetailsPane, AppTheme theme) {
        authenticationType = AuthenticationType.BEARER;
        Pane newLoadedPane;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/authentication/BearerAuthentication.fxml"));
            newLoadedPane = loader.load();
            BearerAuthenticationController controller = loader.getController();
            controller.setAuthentication(this);
            controller.setTheme(theme);
        } catch (IOException e) {
            throw new RuntimeException("can't open BasicAuthentication.fxml");
        }
        authenticationDetailsPane.getChildren().clear();
        authenticationDetailsPane.getChildren().add(newLoadedPane);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
