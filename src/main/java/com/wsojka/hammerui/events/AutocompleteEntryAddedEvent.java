/*
 * Copyright (C) 2020 Waldemar Sojka, All Rights Reserved
 */

package com.wsojka.hammerui.events;

import com.wsojka.hammerui.dto.UrlEntry;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

public class AutocompleteEntryAddedEvent extends Event {

    private ObservableList<UrlEntry> entries;

    public ObservableList<UrlEntry> getEntries() {
        return entries;
    }

    public static final EventType<AutocompleteEntryAddedEvent> AUTOCOMPLETE_ENTRY_ADDED_EVENT_TYPE =
            new EventType<>(Event.ANY, "AUTOCOMPLETE_ENTRY_ADDED_EVENT_TYPE");


    public AutocompleteEntryAddedEvent() {
        super(AUTOCOMPLETE_ENTRY_ADDED_EVENT_TYPE);
    }

    public AutocompleteEntryAddedEvent(Object source, EventTarget target, ObservableList<UrlEntry> entries) {
        super(source, target, AUTOCOMPLETE_ENTRY_ADDED_EVENT_TYPE);
        this.entries = entries;
    }
}
