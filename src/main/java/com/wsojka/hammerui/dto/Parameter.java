/*
 * Copyright (C) 2018 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.dto;

import java.util.Objects;

public class Parameter {

    private String name;
    private String value;

    public Parameter() {
    }

    public Parameter(String name, String value) {
        this.name = name;
        this.value = value;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Parameter parameter = (Parameter) o;
        return Objects.equals(name, parameter.name) &&
                Objects.equals(value, parameter.value);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, value);
    }
}
