/*
 * Copyright (C) 2020 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.service;

public class UrlHistoryServiceFactory {

    private static final UrlHistoryService instance = new UrlHistoryServiceImpl();

    private UrlHistoryServiceFactory() {
    }

    public static UrlHistoryService getInstance() {
        return instance;
    }
}
