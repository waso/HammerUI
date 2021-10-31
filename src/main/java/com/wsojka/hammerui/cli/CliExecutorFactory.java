/*
 * Copyright (C) 2020 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.cli;

public class CliExecutorFactory {
    private static CliExecutor instance;

    private CliExecutorFactory() {}

    public static CliExecutor getInstance() {
        if (instance == null) {
            instance = new CliExecutor();
        }
        return instance;
    }
}
