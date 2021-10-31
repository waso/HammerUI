/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.dto.authentication;

import com.wsojka.hammerui.controllers.authentication.NoAuthenticationController;
import com.wsojka.hammerui.enums.AppTheme;
import com.wsojka.hammerui.enums.AuthenticationType;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;

public class NoAuthentication extends Authentication implements Serializable {

    @Serial
    private static final long serialVersionUID = -1262951519787916698L;

    public NoAuthentication() {
        authenticationType = AuthenticationType.NONE;
    }

    public void setAuthenticationDetailsPane(Pane authenticationDetailsPane, AppTheme theme) {
        authenticationType = AuthenticationType.NONE;
        Pane newLoadedPane;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/authentication/NoAuthentication.fxml"));
            newLoadedPane = loader.load();
            NoAuthenticationController controller = loader.getController();
            controller.setAuthentication(this);
            controller.setTheme(theme);
        } catch (IOException e) {
            throw new RuntimeException("can't open BasicAuthentication.fxml");
        }
        if (authenticationDetailsPane == null)
            throw new RuntimeException("authentication details options missing");
        authenticationDetailsPane.getChildren().clear();
        authenticationDetailsPane.getChildren().add(newLoadedPane);
    }
}
