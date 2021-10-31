/*
 * Copyright (C) 2020 Waldemar Sojka, All Rights Reserved
 */

package com.wsojka.hammerui.log;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableStringValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import java.io.Serializable;

public class InMemoryLog implements Log, Serializable {

    private static final long serialVersionUID = 112341234535234332L;

    protected volatile int currentLogLevel;

    protected String name;

    private static final SimpleStringProperty value = new SimpleStringProperty("");

    private StringBuilder sb = new StringBuilder();

    public static final int LOG_TRACE = 1;
    public static final int LOG_DEBUG = 2;
    public static final int LOG_INFO = 3;
    public static final int LOG_WARN = 4;
    public static final int LOG_ERROR = 5;
    public static final int LOG_FATAL = 6;

    public static final int LOG_ALL = LOG_TRACE - 1;
    public static final int LOG_OFF = LOG_FATAL + 1;

    private boolean isEnabled(int logLevel) {
        return logLevel >= currentLogLevel;
    }

    protected void log(int type, Object message, Throwable t) {
        sb.setLength(0);
        sb.append(value.get());
        if (StringUtils.isNotBlank(value.get()))
            sb.append("\n");
        sb.append(String.valueOf(message));
        value.set(sb.toString());
    }

    public InMemoryLog(String name) {
        this.name = name;
        setLevel(InMemoryLog.LOG_DEBUG);
    }

    public void setLevel(int logLevel) {
        this.currentLogLevel = logLevel;
    }

    public ObservableStringValue getValue() {
        return value;
    }

    public void clearLog() {
        value.set("");
    }

    @Override
    public void debug(Object message) {
        if (isEnabled(InMemoryLog.LOG_DEBUG)) {
            log(InMemoryLog.LOG_DEBUG, message, null);
        }
    }

    @Override
    public void debug(Object message, Throwable t) {
        if (isEnabled(InMemoryLog.LOG_DEBUG)) {
            log(InMemoryLog.LOG_DEBUG, message, t);
        }
    }

    @Override
    public void error(Object message) {
        if (isEnabled(InMemoryLog.LOG_ERROR)) {
            log(InMemoryLog.LOG_ERROR, message, null);
        }
    }

    @Override
    public void error(Object message, Throwable t) {
        if (isEnabled(InMemoryLog.LOG_ERROR)) {
            log(InMemoryLog.LOG_ERROR, message, t);
        }
    }

    @Override
    public void fatal(Object message) {
        if (isEnabled(InMemoryLog.LOG_FATAL)) {
            log(InMemoryLog.LOG_FATAL, message, null);
        }
    }

    @Override
    public void fatal(Object message, Throwable t) {
        if (isEnabled(InMemoryLog.LOG_FATAL)) {
            log(InMemoryLog.LOG_FATAL, message, t);
        }
    }

    @Override
    public void info(Object message) {
        if (isEnabled(InMemoryLog.LOG_INFO)) {
            log(InMemoryLog.LOG_ERROR, message, null);
        }
    }

    @Override
    public void info(Object message, Throwable t) {
        if (isEnabled(InMemoryLog.LOG_INFO)) {
            log(InMemoryLog.LOG_INFO, message, t);
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return isEnabled(InMemoryLog.LOG_DEBUG);
    }

    @Override
    public boolean isErrorEnabled() {
        return isEnabled(InMemoryLog.LOG_ERROR);
    }

    @Override
    public boolean isFatalEnabled() {
        return isEnabled(InMemoryLog.LOG_FATAL);
    }

    @Override
    public boolean isInfoEnabled() {
        return isEnabled(InMemoryLog.LOG_INFO);
    }

    @Override
    public boolean isTraceEnabled() {
        return isEnabled(InMemoryLog.LOG_TRACE);
    }

    @Override
    public boolean isWarnEnabled() {
        return isEnabled(InMemoryLog.LOG_WARN);
    }

    @Override
    public void trace(Object message) {
        if (isEnabled(InMemoryLog.LOG_TRACE)) {
            log(InMemoryLog.LOG_TRACE, message, null);
        }
    }

    @Override
    public void trace(Object message, Throwable t) {
        if (isEnabled(InMemoryLog.LOG_TRACE)) {
            log(InMemoryLog.LOG_TRACE, message, t);
        }
    }

    @Override
    public void warn(Object message) {
        if (isEnabled(InMemoryLog.LOG_WARN)) {
            log(InMemoryLog.LOG_WARN, message, null);
        }
    }

    @Override
    public void warn(Object message, Throwable t) {
        if (isEnabled(InMemoryLog.LOG_WARN)) {
            log(InMemoryLog.LOG_WARN, message, t);
        }
    }
}
