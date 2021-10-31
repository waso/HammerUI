/*
 * Copyright (C) 2021 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.events;

import com.wsojka.hammerui.enums.EditorTheme;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

import java.io.Serial;

public class EditorThemeChangeEvent extends Event {

    @Serial
    private static final long serialVersionUID = -1555609943038391543L;

    private EditorTheme themeName;

    public EditorTheme getThemeName() {
        return themeName;
    }

    public static final EventType<EditorThemeChangeEvent> EDITOR_THEME_CHANGE_EVENT_EVENT_TYPE =
            new EventType<>(Event.ANY, "EDITOR_THEME_CHANGE_EVENT_EVENT_TYPE");

    public EditorThemeChangeEvent() {
        super(EDITOR_THEME_CHANGE_EVENT_EVENT_TYPE);
    }

    public EditorThemeChangeEvent(Object source, EventTarget target, EditorTheme themeName) {
        super(source, target, EDITOR_THEME_CHANGE_EVENT_EVENT_TYPE);
        this.themeName = themeName;
    }

}
