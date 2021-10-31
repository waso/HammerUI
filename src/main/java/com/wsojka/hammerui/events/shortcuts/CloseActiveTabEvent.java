/*
 * Copyright (C) 2021 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.events.shortcuts;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

public class CloseActiveTabEvent extends Event {

    public static final EventType<CloseActiveTabEvent> CLOSE_ACTIVE_TAB_EVENT_EVENT_TYPE =
            new EventType<>(Event.ANY, "CLOSE_ACTIVE_TAB_EVENT_EVENT_TYPE");

    public CloseActiveTabEvent() {
        super(CLOSE_ACTIVE_TAB_EVENT_EVENT_TYPE);
    }

    public CloseActiveTabEvent(Object source, EventTarget target) {
        super(source, target, CLOSE_ACTIVE_TAB_EVENT_EVENT_TYPE);
    }
}
