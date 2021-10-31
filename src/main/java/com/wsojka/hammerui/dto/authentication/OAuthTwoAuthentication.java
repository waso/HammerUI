/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.dto.authentication;

import com.wsojka.hammerui.controllers.authentication.OAuthTwoAuthenticationController;
import com.wsojka.hammerui.enums.AppTheme;
import com.wsojka.hammerui.enums.AuthenticationType;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;

public class OAuthTwoAuthentication extends Authentication implements Serializable {

    @Serial
    private static final long serialVersionUID = -6309580832090464640L;

    private String token;

    public OAuthTwoAuthentication() {
        authenticationType = AuthenticationType.OAUTH_20;
    }

    public void setAuthenticationDetailsPane(Pane authenticationDetailsPane, AppTheme theme) {
        authenticationType = AuthenticationType.OAUTH_20;
        Pane newLoadedPane;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/authentication/OAuthTwoAuthentication.fxml"));
            newLoadedPane = loader.load();
            OAuthTwoAuthenticationController controller = loader.getController();
            controller.setAuthentication(this);
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
