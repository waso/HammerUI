/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.dto.authentication;

import com.wsojka.hammerui.controllers.authentication.DigestAuthenticationController;
import com.wsojka.hammerui.enums.AppTheme;
import com.wsojka.hammerui.enums.AuthenticationType;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;

public class DigestAuthentication extends Authentication implements Serializable {

    @Serial
    private static final long serialVersionUID = 2108725040553350015L;

    private String username;
    private String password;

    public DigestAuthentication() {
        authenticationType = AuthenticationType.DIGEST;
    }

    public void setAuthenticationDetailsPane(Pane authenticationDetailsPane, AppTheme theme) {
        authenticationType = AuthenticationType.DIGEST;
        Pane newLoadedPane;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/authentication/DigestAuthentication.fxml"));
            newLoadedPane = loader.load();
            DigestAuthenticationController controller = loader.getController();
            controller.setAuthentication(this);
        } catch (IOException e) {
            throw new RuntimeException("can't open BasicAuthentication.fxml");
        }
        authenticationDetailsPane.getChildren().clear();
        authenticationDetailsPane.getChildren().add(newLoadedPane);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
