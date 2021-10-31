/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.dto.authentication;

import com.wsojka.hammerui.enums.AppTheme;
import com.wsojka.hammerui.enums.AuthenticationType;
import javafx.scene.layout.Pane;

public abstract class Authentication {

    protected AuthenticationType authenticationType;

    abstract public void setAuthenticationDetailsPane(Pane authenticationDetailsPane, AppTheme theme);

    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }
}
