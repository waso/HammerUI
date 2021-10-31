/*
 * Copyright (C) 2019 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.persintence;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import javafx.collections.ObservableList;

import java.lang.reflect.ParameterizedType;

import static com.wsojka.hammerui.persintence.ObservableListTypeAdapter.getObservableListTypeAdapter;

public final class ObservableListTypeAdapterFactory
        implements TypeAdapterFactory {

    private static final TypeAdapterFactory observableListTypeAdapterFactory = new ObservableListTypeAdapterFactory();

    private ObservableListTypeAdapterFactory() {
    }

    public static TypeAdapterFactory getObservableListTypeAdapterFactory() {
        return observableListTypeAdapterFactory;
    }

    @Override
    public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> typeToken) {
        if (ObservableList.class.isAssignableFrom(typeToken.getRawType())) {
            final ParameterizedType parameterizedType = (ParameterizedType) typeToken.getType();
            final Class<?> elementClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
            final TypeAdapter<?> elementTypeAdapter = gson.getAdapter(elementClass);
            @SuppressWarnings("unchecked") final TypeAdapter<T> objectObservableListTypeAdapter = (TypeAdapter<T>) getObservableListTypeAdapter(elementTypeAdapter);
            return objectObservableListTypeAdapter;
        }
        return null;
    }

}
