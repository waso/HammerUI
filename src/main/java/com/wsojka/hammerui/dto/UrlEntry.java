/*
 * Copyright (C) 2020 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.dto;

import com.wsojka.hammerui.enums.HttpMethod;

import java.util.Objects;

public class UrlEntry {

    private String url;

    private HttpMethod httpMethod;

    private int statusCode = -1;

    public UrlEntry(String url, HttpMethod httpMethod) {
        this.url = url;
        this.httpMethod = httpMethod;
    }

    public UrlEntry(String url, HttpMethod httpMethod, int statusCode) {
        this.url = url;
        this.httpMethod = httpMethod;
        this.statusCode = statusCode;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UrlEntry urlEntry = (UrlEntry) o;
        return Objects.equals(url, urlEntry.url) &&
                httpMethod == urlEntry.httpMethod;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, httpMethod);
    }

    @Override
    public String toString() {
        return url;
    }
}
