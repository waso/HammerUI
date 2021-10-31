/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.persintence;

public class PreferencesWrapperFactory {

    private static PreferencesWrapperImpl instance;

    private PreferencesWrapperFactory() {}

    public static PreferencesWrapper getInstance() {
        if (instance == null) {
            instance = new PreferencesWrapperImpl();
        }
        return instance;
    }
}