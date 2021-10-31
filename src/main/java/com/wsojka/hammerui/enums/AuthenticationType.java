/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.enums;

import com.google.common.collect.Lists;
import com.wsojka.hammerui.dto.authentication.*;
import com.wsojka.hammerui.persintence.RuntimeTypeAdapterFactory;

import java.util.List;
import java.util.stream.Collectors;

public enum AuthenticationType {

    NONE("No Auth", NoAuthentication.class, true),
    BASIC("Basic", BasicAuthentication.class, true),
    BEARER("Bearer", BearerAuthentication.class, true),
    DIGEST("Digest", DigestAuthentication.class, false),
    OAUTH_10("OAuth 1.0", OAuthOneAuthentication.class, false),
    OAUTH_20("OAuth 2.0", OAuthTwoAuthentication.class, false),
    AWS("AWS", AWSAuthentication.class, false);

    private Class controllerClass;
    private String name;
    private boolean supported;

    AuthenticationType(String name, Class controllerClass, boolean supported) {
        this.name = name;
        this.controllerClass = controllerClass;
        this.supported = supported;
    }

    public String getName() {
        return name;
    }

    public Class getControllerClass() {
        return controllerClass;
    }

    public boolean isSupported() {
        return supported;
    }

    public static AuthenticationType[] getSupportedValues() {
        List<AuthenticationType> supportedAuthsList = Lists.newArrayList(AuthenticationType.values())
                .stream()
                .filter(AuthenticationType::isSupported)
                .collect(Collectors.toList());
        AuthenticationType[] supportedAuths = new AuthenticationType[supportedAuthsList.size()];
        return supportedAuthsList.toArray(supportedAuths);
    }

    public static RuntimeTypeAdapterFactory<Authentication> getRuntimeTypeAdapterFactory() {
        RuntimeTypeAdapterFactory<Authentication> authenticationAdapterFactory = RuntimeTypeAdapterFactory.of(Authentication.class, "_type")
                .registerSubtype(AWSAuthentication.class, "AWSAuthentication")
                .registerSubtype(DigestAuthentication.class, "DigestAuthentication")
                .registerSubtype(OAuthTwoAuthentication.class, "OAuthTwoAuthentication")
                .registerSubtype(BasicAuthentication.class, "BasicAuthentication")
                .registerSubtype(OAuthOneAuthentication.class, "OAuthOneAuthentication")
                .registerSubtype(BearerAuthentication.class, "BearerAuthentication")
                .registerSubtype(NoAuthentication.class, "NoAuthentication");
        return authenticationAdapterFactory;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
