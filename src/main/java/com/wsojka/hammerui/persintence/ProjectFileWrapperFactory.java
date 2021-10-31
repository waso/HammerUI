/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.persintence;

public class ProjectFileWrapperFactory {

    private static ProjectFileWrapperImpl instance;

    private ProjectFileWrapperFactory() {}

    public static ProjectFileWrapper getInstance() {
        if (instance == null) {
            instance = new ProjectFileWrapperImpl();
        }
        return instance;
    }

    public static ProjectFileWrapper getNewInstance() {
        instance = new ProjectFileWrapperImpl();
        return instance;
    }
}
