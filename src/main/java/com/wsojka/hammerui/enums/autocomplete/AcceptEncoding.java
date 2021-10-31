/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.enums.autocomplete;

public enum AcceptEncoding {

    GZIP("gzip"),
    COMPRESS("compress"),
    DEFLATE("deflate"),
    BR("br"),
    IDENTITY("identity"),
    ;

    private String name;

    AcceptEncoding(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
