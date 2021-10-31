/*
 * Copyright (C) 2021 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.enums;

public enum AppTheme {
    Light(""),
    Dark("dark_theme"),
    ;

    private String styleName;

    AppTheme(String styleName) {
        this.styleName = styleName;
    }

    public String getStyleName() {
        return styleName;
    }
}
