/*
 * Copyright (C) 2018 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.events;

import com.wsojka.hammerui.dto.Request;
import com.wsojka.hammerui.dto.ResponseDetails;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

public class ResponseEvent extends Event {

    private Request source;

    private Request request;

    private ResponseDetails responseDetails;

    public ResponseDetails getResponseDetails() {
        return responseDetails;
    }

    public void setResponseDetails(ResponseDetails responseDetails) {
        this.responseDetails = responseDetails;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public static final EventType<ResponseEvent> RESPONSE_EVENT =
            new EventType<>(Event.ANY, "RESPONSE_EVENT");


    public ResponseEvent() {
        super(RESPONSE_EVENT);
    }

    @Override
    public Request getSource() {
        return source;
    }

    public void setSource(Request source) {
        this.source = source;
    }

    public ResponseEvent(Request sourceRequest, Object source, EventTarget target, Request request, ResponseDetails responseDetails) {
        super(source, target, RESPONSE_EVENT);
        this.request = request;
        this.responseDetails = responseDetails;
        this.source = sourceRequest;
    }
}
