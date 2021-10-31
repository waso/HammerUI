/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.enums.autocomplete;

public enum Authorization {

    BASIC("Basic"),
    BEARER("Bearer"),
    DIGEST("Digest"),
    HOBA("HOBA"),
    MUTUAL("Mutual"),
    AWS4_HMAC_SHA256("AWS4-HMAC-SHA256"),
    OAUTH("OAuth"),
    ;

    private String name;

    Authorization(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
