/*
 * Copyright (C) 2018 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.utils;

import com.wsojka.hammerui.enums.LogLevel;
import org.apache.commons.lang3.StringUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConsoleLogger {

    public static LogLevel LOG_LEVEL = LogLevel.INFO;

    public static void info(String message) {
        printMessage(message, LogLevel.INFO);
    }

    public static void info(String message, Object... objects) {
        message = buildMessage(message, objects);
        printMessage(message, LogLevel.INFO);
    }

    public static void debug(String message) {
        printMessage(message, LogLevel.DEBUG);
    }

    public static void debug(String message, Object... objects) {
        message = buildMessage(message, objects);
        printMessage(message, LogLevel.DEBUG);
    }

    public static void error(String message) {
        printMessage(message, LogLevel.ERROR);
    }

    public static void error(String message, Object... objects) {
        message = buildMessage(message, objects);
        printMessage(message, LogLevel.ERROR);
    }

    private static void printMessage(String message, LogLevel logLevel) {
        if (logLevel.ordinal() < LOG_LEVEL.ordinal())
            return;
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println(date + " HammerUI [" + logLevel + "] - " + message);
    }

    private static String buildMessage(String message, Object... objects) {
        for (Object object : objects) {
            message = StringUtils.replaceOnce(message, "{}", String.valueOf(object));
        }
        return message;
    }
}
