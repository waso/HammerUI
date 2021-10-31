/*
 * Copyright (C) 2018 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.enums;

public enum ResponseContentType {
    RESPONSE(0),
    HEADERS(1),
    TESTS(2);

    private int tabIndex;

    ResponseContentType(int tabIndex) {
        this.tabIndex = tabIndex;
    }

    public int getTabIndex() {
        return tabIndex;
    }
}
