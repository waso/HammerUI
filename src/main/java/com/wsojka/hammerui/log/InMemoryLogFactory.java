/*
 * Copyright (C) 2020 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.log;

public class InMemoryLogFactory {

    private static InMemoryLog instance;

    private InMemoryLogFactory() {}

    public static InMemoryLog getInstance() {
        if (instance == null) {
            instance = new InMemoryLog("");
        }
        return instance;
    }
}
