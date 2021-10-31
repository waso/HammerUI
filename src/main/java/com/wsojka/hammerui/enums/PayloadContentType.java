/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.enums;

public enum PayloadContentType {
    TEXT(0, "text"),
    FILE(1, "file"),
    X_WWW_FORM_URLENCODED(2, "x-www-form-urlencoded"),
    FORM_DATA(3, "form-data"),
    ;

    private int tabIndex;
    private String name;

    PayloadContentType(int tabIndex, String name) {
        this.tabIndex = tabIndex;
        this.name = name;
    }

    public int getTabIndex() {
        return tabIndex;
    }

    @Override
    public String toString() {
        return name;
    }
}
