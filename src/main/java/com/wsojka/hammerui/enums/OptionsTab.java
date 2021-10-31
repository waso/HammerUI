/*
 * Copyright (C) 2018 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.enums;

public enum OptionsTab {
    HEADER(0),
    AUTHENTICATION(1),
    PAYLOAD(2),
    ;

    private int tabIndex;

    OptionsTab(int index) {
        this.tabIndex = index;
    }

    public int getIndex() {
        return this.tabIndex;
    }

    public static OptionsTab get(int index) {
        for (OptionsTab t : values()) {
            if (t.getIndex() == index)
                return t;
        }
        return OptionsTab.HEADER;
    }
}
