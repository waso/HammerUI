/*
 * Copyright (C) 2020 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.persintence;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import javafx.beans.property.SimpleBooleanProperty;

import java.lang.reflect.Type;

public class SimpleBooleanPropertySerializer implements JsonSerializer<SimpleBooleanProperty> {
    @Override
    public JsonElement serialize(SimpleBooleanProperty src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.get());
    }
}
