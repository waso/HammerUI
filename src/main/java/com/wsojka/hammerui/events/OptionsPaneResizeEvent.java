/*
 * Copyright (C) 2018 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.events;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

public class OptionsPaneResizeEvent extends Event{

    private double[] lastDividerPositions;

    public double[] getLastDividerPositions() {
        return lastDividerPositions;
    }

    public static final EventType<OptionsPaneResizeEvent> RESIZE_OPTIONS_PANE =
            new EventType<>(Event.ANY, "RESIZE_OPTIONS_PANE");


    public OptionsPaneResizeEvent() {
        super(RESIZE_OPTIONS_PANE);
    }

    public OptionsPaneResizeEvent(Object source, EventTarget target, double[] lastDividerPositions) {
        super(source, target, RESIZE_OPTIONS_PANE);
        this.lastDividerPositions = lastDividerPositions;
    }
}
