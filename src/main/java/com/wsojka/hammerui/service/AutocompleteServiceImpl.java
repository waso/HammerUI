/*
 * Copyright (C) 2019 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.service;

import com.google.common.collect.Lists;
import com.wsojka.hammerui.dto.UrlEntry;
import com.wsojka.hammerui.events.AutocompleteEntryAddedEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class AutocompleteServiceImpl implements AutocompleteService {

    private final List<UrlEntry> entries = Lists.newArrayList();

    private TabPane tabPane;

    public AutocompleteServiceImpl() {
        super();
    }

    @Override
    public void setTabPane(TabPane tabPane) {
        this.tabPane = tabPane;
    }

    @Override
    public void addEntry(UrlEntry entry) {
        if (entry == null)
            return;
        if (StringUtils.isBlank(entry.getUrl()))
            return;
        entries.remove(entry);
        entries.add(0, entry);
        if (tabPane != null) {
            for (Tab tab : tabPane.getTabs()) {
                Node tabNode = tab.getContent();
                AutocompleteEntryAddedEvent changeEvent = new AutocompleteEntryAddedEvent(this, tabNode, getEntries());
                tabNode.fireEvent(changeEvent);
            }
        }
    }

    @Override
    public ObservableList<UrlEntry> getEntries() {
        ObservableList<UrlEntry> copy = FXCollections.observableArrayList();
        copy.addAll(entries);
        return copy;
    }
}
