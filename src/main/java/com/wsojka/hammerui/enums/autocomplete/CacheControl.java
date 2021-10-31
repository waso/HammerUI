/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.enums.autocomplete;

public enum CacheControl {

    NO_CACHE("no-cache"),
    NO_STORE("no-store"),
    NO_TRANSFORM("no-transform"),
    ONLY_IF_CACHED("only-if-cached"),
    MAX_AGE("max-age="),
    MAX_STALE("max-stale="),
    MIN_FRESH("min-fresh="),
    ;

    private String name;

    CacheControl(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
