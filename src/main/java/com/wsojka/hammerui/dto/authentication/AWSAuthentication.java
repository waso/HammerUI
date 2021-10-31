/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.dto.authentication;

import com.wsojka.hammerui.controllers.authentication.AWSAuthenticationController;
import com.wsojka.hammerui.enums.AppTheme;
import com.wsojka.hammerui.enums.AuthenticationType;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;

public class AWSAuthentication extends Authentication implements Serializable {

    @Serial
    private static final long serialVersionUID = -8472177596196708116L;

    private String secretKey;
    private String accessKey;
    private String region = "us-east-1"; //TODO: implement
    private String service = "iam"; //TODO: implement

    public void setAuthenticationDetailsPane(Pane authenticationDetailsPane, AppTheme theme) {
        authenticationType = AuthenticationType.AWS;
        Pane newLoadedPane;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/authentication/AWSAuthentication.fxml"));
            newLoadedPane = loader.load();
            AWSAuthenticationController controller = loader.getController();
            controller.setAuthentication(this);
            //TODO: add theme css
        } catch (IOException e) {
            throw new RuntimeException("can't open BasicAuthentication.fxml");
        }
        authenticationDetailsPane.getChildren().clear();
        authenticationDetailsPane.getChildren().add(newLoadedPane);
    }

    public AWSAuthentication() {
        authenticationType = AuthenticationType.AWS;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }
}
