/*
 * Copyright (C) 2020 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.service;

import com.wsojka.hammerui.dto.UrlEntry;
import javafx.collections.ObservableList;
import javafx.scene.control.TabPane;

public interface AutocompleteService {

    void setTabPane(TabPane tabPane);

    void addEntry(UrlEntry entry);

    ObservableList<UrlEntry> getEntries();
}
