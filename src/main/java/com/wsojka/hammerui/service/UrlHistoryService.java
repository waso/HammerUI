/*
 * Copyright (C) 2020 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.service;

public interface UrlHistoryService {

    void addUrlEntry(String uuid, String url);

    String getPreviousUrl(String uuid);

    String getNextUrl(String uuid);
}
