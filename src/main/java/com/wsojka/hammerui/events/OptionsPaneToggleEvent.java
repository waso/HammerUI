/*
 * Copyright (C) 2018 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.events;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

public class OptionsPaneToggleEvent extends Event {


    private boolean disablePane = false;

    private double[] lastDividerPositions;

    public boolean isDisablePane() {
        return disablePane;
    }

    public double[] getLastDividerPositions() {
        return lastDividerPositions;
    }

    public static final EventType<OptionsPaneToggleEvent> TOGGLE_OPTIONS_PANE =
            new EventType<>(Event.ANY, "TOGGLE_OPTIONS_PANE");


    public OptionsPaneToggleEvent() {
        super(TOGGLE_OPTIONS_PANE);
    }

    public OptionsPaneToggleEvent(Object source, EventTarget target, boolean disablePane, double[] lastDividerPositions) {
        super(source, target, TOGGLE_OPTIONS_PANE);
        this.disablePane = disablePane;
        this.lastDividerPositions = lastDividerPositions;
    }

}
