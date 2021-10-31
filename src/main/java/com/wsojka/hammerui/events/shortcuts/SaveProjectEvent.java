/*
 * Copyright (C) 2019 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.events.shortcuts;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

public class SaveProjectEvent extends Event {

    public static final EventType<SaveProjectEvent> SAVE_PROJECT_EVENT_TYPE =
            new EventType<>(Event.ANY, "SAVE_PROJECT_EVENT_TYPE");

    public SaveProjectEvent() {
        super(SAVE_PROJECT_EVENT_TYPE);
    }

    public SaveProjectEvent(Object source, EventTarget target) {
        super(source, target, SAVE_PROJECT_EVENT_TYPE);
    }
}
