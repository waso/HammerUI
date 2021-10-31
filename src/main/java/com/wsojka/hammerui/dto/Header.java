/*
 * Copyright (C) 2018 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.dto;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.Objects;

public class Header {

    private String name;
    private String value;
    private SimpleBooleanProperty enabled;

    public Header() {
    }

    public Header(String name, String value) {
        this.name = name;
        this.value = value;
        this.enabled = new SimpleBooleanProperty(true);
    }

    public Header(String name, String value, Boolean property) {
        this.name = name;
        this.value = value;
        this.enabled = new SimpleBooleanProperty(property);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean getEnabled() {
        if (enabled == null)
            return true;
        return enabled.get();
    }

    public void setEnabled(Boolean enabled) {
        this.enabled.set(enabled);
    }

    public BooleanProperty enabledProperty() {
        return this.enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Header header = (Header) o;
        return Objects.equals(name, header.name) &&
                Objects.equals(value, header.value) &&
                Objects.equals(enabled, header.enabled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, enabled);
    }

    @Override
    public String toString() {
        return "Header{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
