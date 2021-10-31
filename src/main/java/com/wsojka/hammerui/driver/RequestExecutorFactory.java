/*
 * Copyright (C) 2018 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.driver;

public class RequestExecutorFactory {

    private static RequestExecutor instance;

    private RequestExecutorFactory() {}

    public static RequestExecutor getInstance() {
        if (instance == null) {
            instance = new RequestExecutor();
        }
        return instance;
    }
}
