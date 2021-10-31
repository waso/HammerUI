/*
 * Copyright (C) 2020 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.service;

public class AutocompleteServiceFactory {

    private static final AutocompleteService instance = new AutocompleteServiceImpl();

    private AutocompleteServiceFactory() {
    }

    public static AutocompleteService getInstance() {
        return instance;
    }
}
