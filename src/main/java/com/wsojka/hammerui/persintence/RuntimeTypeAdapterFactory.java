/*
 * Copyright (C) 2019 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.persintence;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RuntimeTypeAdapterFactory<T> implements TypeAdapterFactory {
    private final Class<?> type;
    private final String fieldName;
    private final Map<String, Class<?>> labelToSubtype = new LinkedHashMap<>();
    private final Map<Class<?>, String> subtypeToLabel = new LinkedHashMap<>();

    private RuntimeTypeAdapterFactory(Class<?> type, String fieldName) {
        if (fieldName == null || type == null) {
            throw new NullPointerException();
        }
        this.type = type;
        this.fieldName = fieldName;
    }

    public static <T> RuntimeTypeAdapterFactory<T> of(Class<T> baseType, String typeFieldName) {
        return new RuntimeTypeAdapterFactory<T>(baseType, typeFieldName);
    }

    public static <T> RuntimeTypeAdapterFactory<T> of(Class<T> baseType) {
        return new RuntimeTypeAdapterFactory<T>(baseType, "_type");
    }

    public RuntimeTypeAdapterFactory<T> registerSubtype(Class<? extends T> type, String label) {
        if (type == null || label == null) {
            throw new NullPointerException();
        }
        if (subtypeToLabel.containsKey(type) || labelToSubtype.containsKey(label)) {
            throw new IllegalArgumentException("types and labels are not unique");
        }
        labelToSubtype.put(label, type);
        subtypeToLabel.put(type, label);
        return this;
    }

    public <R> TypeAdapter<R> create(Gson gson, TypeToken<R> type) {
        if (type.getRawType() != this.type) {
            return null;
        }

        final Map<String, TypeAdapter<?>> labelToDelegate
                = new LinkedHashMap<>();
        final Map<Class<?>, TypeAdapter<?>> subtypeToDelegate
                = new LinkedHashMap<>();
        for (Map.Entry<String, Class<?>> entry : labelToSubtype.entrySet()) {
            TypeAdapter<?> delegate = gson.getDelegateAdapter(this, TypeToken.get(entry.getValue()));
            labelToDelegate.put(entry.getKey(), delegate);
            subtypeToDelegate.put(entry.getValue(), delegate);
        }

        return new TypeAdapter<R>() {
            @Override
            public R read(JsonReader in) throws IOException {
                JsonElement jsonElement = Streams.parse(in);
                JsonElement labelJsonElement = jsonElement.getAsJsonObject().remove(fieldName);
                if (labelJsonElement == null) {
                    throw new JsonParseException("cannot deserialize " + RuntimeTypeAdapterFactory.this.type
                            + " because it does not define a field named " + fieldName);
                }
                String label = labelJsonElement.getAsString();
                @SuppressWarnings("unchecked")
                TypeAdapter<R> delegate = (TypeAdapter<R>) labelToDelegate.get(label);
                if (delegate == null) {
                    throw new JsonParseException("cannot deserialize " + RuntimeTypeAdapterFactory.this.type + " subtype named "
                            + label + "; did you forget to register a subtype?");
                }
                return delegate.fromJsonTree(jsonElement);
            }

            @Override
            public void write(JsonWriter out, R value) throws IOException {
                Class<?> srcType = value.getClass();
                String label = subtypeToLabel.get(srcType);
                @SuppressWarnings("unchecked")
                TypeAdapter<R> delegate = (TypeAdapter<R>) subtypeToDelegate.get(srcType);
                if (delegate == null) {
                    throw new JsonParseException("cannot serialize " + srcType.getName()
                            + "; did you forget to register a subtype?");
                }
                JsonObject jsonObject = delegate.toJsonTree(value).getAsJsonObject();
                if (jsonObject.has(fieldName)) {
                    throw new JsonParseException("cannot serialize " + srcType.getName()
                            + " because it already defines a field named " + fieldName);
                }
                JsonObject clone = new JsonObject();
                clone.add(fieldName, new JsonPrimitive(label));
                for (Map.Entry<String, JsonElement> e : jsonObject.entrySet()) {
                    clone.add(e.getKey(), e.getValue());
                }
                Streams.write(clone, out);
            }
        }.nullSafe();
    }
}