/*
 * Copyright (C) 2018 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.events;

import com.wsojka.hammerui.enums.OptionsTab;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

public class OptionsPaneSwitchTabEvent extends Event {

    private OptionsTab optionsTab;

    private String sourceId;

    public OptionsTab getOptionsTab() {
        return optionsTab;
    }

    public String getSourceId() {
        return sourceId;
    }

    public static final EventType<OptionsPaneSwitchTabEvent> SWITCH_OPTIONS_PANE =
            new EventType<>(Event.ANY, "SWITCH_OPTIONS_PANE");

    public OptionsPaneSwitchTabEvent() {
        super(SWITCH_OPTIONS_PANE);
    }

    public OptionsPaneSwitchTabEvent(Object source, EventTarget target, OptionsTab optionsTab, String sourceId) {
        super(source, target, SWITCH_OPTIONS_PANE);
        this.optionsTab = optionsTab;
        this.sourceId = sourceId;
    }
}
