/*
 * Copyright (C) 2018 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.events;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

public class ItemRenamedEvent extends Event {

    private String itemUuid = "";

    private String newName = "";

    public String getItemUuid() {
        return itemUuid;
    }

    public String getNewName() {
        return newName;
    }

    public static final EventType<ItemRenamedEvent> ITEM_RENAMED =
            new EventType<>(Event.ANY, "ITEM_RENAMED");

    public ItemRenamedEvent() {
        super(ITEM_RENAMED);
    }

    public ItemRenamedEvent(Object source, EventTarget target, String itemUuid, String newName) {
        super(source, target, ITEM_RENAMED);
        this.itemUuid = itemUuid;
        this.newName = newName;
    }

}
