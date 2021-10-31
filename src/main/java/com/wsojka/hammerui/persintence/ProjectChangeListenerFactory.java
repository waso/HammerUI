/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.persintence;

public class ProjectChangeListenerFactory {

    private static ProjectChangeListenerImpl instance;

    private ProjectChangeListenerFactory() {}

    public static ProjectChangeListener getInstance() {
        if (instance == null) {
            instance = new ProjectChangeListenerImpl();
        }
        return instance;
    }
}
