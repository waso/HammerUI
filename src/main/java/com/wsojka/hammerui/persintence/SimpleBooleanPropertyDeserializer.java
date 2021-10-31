/*
 * Copyright (C) 2020 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.persintence;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import javafx.beans.property.SimpleBooleanProperty;

import java.lang.reflect.Type;

public class SimpleBooleanPropertyDeserializer implements JsonDeserializer<SimpleBooleanProperty> {
    @Override
    public SimpleBooleanProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        boolean value = json.getAsBoolean();
        return new SimpleBooleanProperty(value);
    }
}
