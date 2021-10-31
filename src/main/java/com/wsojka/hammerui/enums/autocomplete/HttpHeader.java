/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.enums.autocomplete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum HttpHeader {
    ACCEPT("Accept", Arrays.stream(MimeType.values()).map(MimeType::getName).collect(Collectors.toList())),
    ACCEPT_CHARSET("Accept-Charset", Arrays.stream(CharacterSet.values()).map(CharacterSet::getName).collect(Collectors.toList())),
    ACCEPT_ENCODING("Accept-Encoding", Arrays.stream(AcceptEncoding.values()).map(AcceptEncoding::getName).collect(Collectors.toList())),
    ACCEPT_LANGUAGE("Accept-Language", new ArrayList<>()),
    ACCESS_CONTROL_REQUEST_HEADERS("Access-Control-Request-Headers", new ArrayList<>()),
    ACCESS_CONTROL_REQUEST_METHOD("Access-Control-Request-Method", new ArrayList<>()),
    AUTHORIZATION("Authorization", Arrays.stream(Authorization.values()).map(Authorization::getName).collect(Collectors.toList())),
    CACHE_CONTROL("Cache-Control", Arrays.stream(CacheControl.values()).map(CacheControl::getName).collect(Collectors.toList())),
    CONNECTION("Connection", Arrays.asList("keep-alive", "close")),
    COOKIE("Cookie", new ArrayList<>()),
    DO_NOT_TRACK("DNT", Arrays.asList("0", "1")),
    IF_MATCH("If-Match", new ArrayList<>()),
    IF_MODIFIED_SINCE("If-Modified-Since", new ArrayList<>()),
    IF_NONE_MATCH("If-None-Match", new ArrayList<>()),
    IF_RANGE("If-Range", new ArrayList<>()),
    IF_UNMODIFIED_SINCE("If-Unmodified-Since", new ArrayList<>()),
    ORIGIN("Origin", new ArrayList<>()),
    REFERER("Referer", new ArrayList<>()),
    USER_AGENT("User-Agent", new ArrayList<>()),
    CONTENT_TYPE("Content-Type", Arrays.stream(MimeType.values()).map(MimeType::getName).collect(Collectors.toList())),
    ;


    private String name;
    private List<String> values;

    HttpHeader(String name, List<String> values) {
        this.name = name;
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public List<String> getValues() {
        return values;
    }
}
