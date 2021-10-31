/*
 * Copyright (C) 2018 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.events;

import com.wsojka.hammerui.dto.Request;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

public class RequestFailedEvent extends Event {

    private Request source;

    private Request request;

    private Throwable exception;

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public static final EventType<RequestFailedEvent> REQUEST_FAILED_EVENT =
            new EventType<>(Event.ANY, "REQUEST_FAILED_EVENT");


    public RequestFailedEvent() {
        super(REQUEST_FAILED_EVENT);
    }

    @Override
    public Request getSource() {
        return source;
    }

    public void setSource(Request source) {
        this.source = source;
    }

    public RequestFailedEvent(Request sourceRequest, Object source, EventTarget target, Request request, Throwable throwable) {
        super(source, target, REQUEST_FAILED_EVENT);
        this.request = request;
        this.exception = throwable;
        this.source = sourceRequest;
    }
}
