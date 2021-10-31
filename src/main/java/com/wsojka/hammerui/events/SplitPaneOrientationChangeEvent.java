/*
 * Copyright (C) 2020 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.events;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.Orientation;

public class SplitPaneOrientationChangeEvent extends Event {

    private Orientation orientation;

    public Orientation getOrientation() {
        return orientation;
    }

    public static final EventType<SplitPaneOrientationChangeEvent> SPLIT_PANE_ORIENTATION_CHANGE_EVENT_EVENT_TYPE =
            new EventType<>(Event.ANY, "SPLIT_PANE_ORIENTATION_CHANGE_EVENT_EVENT_TYPE");


    public SplitPaneOrientationChangeEvent() {
        super(SPLIT_PANE_ORIENTATION_CHANGE_EVENT_EVENT_TYPE);
    }

    public SplitPaneOrientationChangeEvent(Object source, EventTarget target, Orientation orientation) {
        super(source, target, SPLIT_PANE_ORIENTATION_CHANGE_EVENT_EVENT_TYPE);
        this.orientation = orientation;
    }
}
