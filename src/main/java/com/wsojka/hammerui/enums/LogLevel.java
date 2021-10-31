/*
 * Copyright (C) 2019 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.enums;

import org.apache.commons.lang3.StringUtils;

public enum LogLevel {
    DEBUG,
    INFO,
    ERROR,
    NONE;

    public static LogLevel getByName(String name) {
        for (LogLevel level : values()) {
            if (StringUtils.equalsIgnoreCase(name, level.name()))
                    return level;
        }
        return null;
    }
}
