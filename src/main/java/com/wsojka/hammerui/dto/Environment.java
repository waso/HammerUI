/*
 * Copyright (C) 2018 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.dto;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Objects;
import java.util.UUID;


public final class Environment implements DeepCopy {

    private String name;

    private String uuid;

    private ObservableList<Parameter> parameters = FXCollections.observableArrayList();

    public Object copy() {
        Environment newEnv = new Environment();
        newEnv.setName(this.getName());
        newEnv.setUuid(this.getUuid());
        for (Parameter p : this.getParameters()) {
            Parameter param = new Parameter();
            param.setName(p.getName());
            param.setValue(p.getValue());
            newEnv.getParameters().add(param);
        }
        return newEnv;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ObservableList<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(ObservableList<Parameter> parameters) {
        this.parameters = parameters;
    }

    public String getUuid() {
        if (uuid == null)
            uuid = UUID.randomUUID().toString();
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Environment that = (Environment) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, uuid);
    }

    @Override
    public String toString() {
        return name;
    }
}
