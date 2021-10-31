/*
 * Copyright (C) 2019 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.tests;

import com.google.common.collect.Lists;
import com.wsojka.hammerui.dto.ResponseDetails;
import com.wsojka.hammerui.utils.ConsoleLogger;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableStringValue;

import java.util.List;
import java.util.UUID;

public class ResponseTest {

    private String id;

    private boolean result;

    private TestType type = TestType.JSONPATH_EXISTS;

    private List<String> params = Lists.newArrayList();

    private transient StringProperty prop = new SimpleStringProperty();

    public ResponseTest(String id, TestType type, String[] params) {
        this.id = id;
        this.type = type;
        this.params = Lists.newArrayList(params);
        this.prop = new SimpleStringProperty(String.format(type.getToStringFormat(), this.params.toArray(new String[0])));
    }

    public ResponseTest() {
        id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public TestType getType() {
        return type;
    }

    public void setType(TestType type) {
        this.type = type;
    }

    public List<String> getParams() {
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params;
        if (this.params.isEmpty())
            this.prop = new SimpleStringProperty(String.format(type.getToStringFormat(), ""));
        else
            this.prop = new SimpleStringProperty(String.format(type.getToStringFormat(), this.params.toArray(new String[0])));
    }

    public boolean perform(ResponseDetails response) {
        if (params == null || params.isEmpty())
            return false;
        result = false;
        try {
            result = type.getValidator().apply(response, params);
        } catch (Exception e) {
            ConsoleLogger.error("exception: " + e);
        }

        String status = result ? "PASS" : "FAIL";
        prop.set(status + " - " + String.format(type.getToStringFormat(), this.params.toArray(new String[0])));
        ConsoleLogger.info("test status: " + status + " - " + String.format(type.getToStringFormat(), this.params.toArray(new String[0])));
        return result;
    }

    @Override
    public String toString() {
        return type.name();
    }

    public ObservableStringValue testNameProperty() {
        return prop;
    }

    public String getDescription() {
        return String.format(type.getToStringFormat(), params.toArray(new String[0]));
    }
}