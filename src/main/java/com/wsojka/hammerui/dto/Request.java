/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.dto;

import com.wsojka.hammerui.dto.authentication.*;
import com.wsojka.hammerui.dto.payload.PostFormEntry;
import com.wsojka.hammerui.enums.HttpMethod;
import com.wsojka.hammerui.enums.PayloadContentType;
import com.wsojka.hammerui.persintence.RequestSettings;
import com.wsojka.hammerui.tests.ResponseTest;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class Request extends Item implements Serializable {

    @Serial
    private static final long serialVersionUID = 6735269437509036120L;

    /* default empty payload for POST requests */
    private static final String EMPTY_PAYLOAD = "";

    /* HTTP method type */
    private HttpMethod httpMethod = HttpMethod.GET;

    /* URL */
    private String url = "";

    /* custom user defined headers */
    private ObservableList<Header> headers = FXCollections.observableArrayList();

    /* request authentication */
    private Authentication authentication = new NoAuthentication();

    /* other authentications defined by the user */
    private ObservableList<Authentication> authentications = FXCollections.observableArrayList();

    /* POST payload content type */
    private PayloadContentType payloadContentType = PayloadContentType.TEXT; /* default POST form content type */

    /* application/x-www-form-urlencoded form entries */
    private ObservableList<PostFormEntry> applicationFormUrlEncodedEntries = FXCollections.observableArrayList();

    /* multipart/formdata form entries */
    private ObservableList<PostFormEntry> multipartFormDataEntries = FXCollections.observableArrayList();

    /* tests performed on responses */
    private ObservableList<ResponseTest> responseTestsList = FXCollections.observableArrayList();

    /* POST form file payload */
    private PostFormEntry postFormFile = new PostFormEntry();

    /* POST form string payload like JSON etc...*/
    private String postFormRawString = EMPTY_PAYLOAD;

    private RequestSettings requestSettings = new RequestSettings();

    public Request() {
        super();
    }

    public Request(String name) {
        super(name);
    }

    public ObservableList<PostFormEntry> getMultipartFormDataEntries() {
        return multipartFormDataEntries;
    }

    public void setMultipartFormDataEntries(ObservableList<PostFormEntry> entries) {
        this.multipartFormDataEntries = entries;
    }

    public ObservableList<PostFormEntry> getApplicationFormUrlEncodedEntries() {
        return applicationFormUrlEncodedEntries;
    }

    public void setApplicationFormUrlEncodedEntries(ObservableList<PostFormEntry> entries) {
        this.applicationFormUrlEncodedEntries = entries;
    }

    public PostFormEntry getPostFormFile() {
        return postFormFile;
    }

    public void setPostFormFile(PostFormEntry postFormFile) {
        this.postFormFile = postFormFile;
    }

    public String getPostFormRawString() {
        return postFormRawString;
    }

    public void setPostFormRawString(String postFormRawString) {
        if (postFormRawString == null)
            return;
        this.postFormRawString = postFormRawString;
    }

    public ObservableList<Header> getHeaders() {
        return headers;
    }

    public void setHeaders(ObservableList<Header> headers) {
        this.headers = headers;
    }


    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String   getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public PayloadContentType getPayloadContentType() {
        return payloadContentType;
    }

    public void setPayloadContentType(PayloadContentType payloadContentType) {
        this.payloadContentType = payloadContentType;
    }

    public ObservableList<Authentication> getAuthentications() {
        return authentications;
    }

    public ObservableList<ResponseTest> getResponseTestsList() {
        return responseTestsList;
    }

    public void setResponseTestsList(ObservableList<ResponseTest> responseTestsList) {
        this.responseTestsList = responseTestsList;
    }

    public RequestSettings getRequestSettings() {
        return requestSettings;
    }

    public void setRequestSettings(RequestSettings requestSettings) {
        this.requestSettings = requestSettings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Request request = (Request) o;
        return httpMethod == request.httpMethod &&
                Objects.equals(url, request.url) &&
                Objects.equals(headers, request.headers) &&
                Objects.equals(authentication, request.authentication) &&
                payloadContentType == request.payloadContentType &&
                Objects.equals(applicationFormUrlEncodedEntries, request.applicationFormUrlEncodedEntries) &&
                Objects.equals(multipartFormDataEntries, request.multipartFormDataEntries) &&
                Objects.equals(responseTestsList, request.responseTestsList) &&
                Objects.equals(postFormFile, request.postFormFile) &&
                Objects.equals(postFormRawString, request.postFormRawString) &&
                Objects.equals(requestSettings, request.requestSettings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), httpMethod, url, headers, authentication, payloadContentType, applicationFormUrlEncodedEntries, multipartFormDataEntries, responseTestsList, postFormFile, postFormRawString, requestSettings);
    }

    public String toDebugString() {
        return "Request{" +
                "httpMethod=" + httpMethod +
                ", url='" + url + '\'' +
                ", headers=" + headers +
                '}';
    }

    @Override
    public String toString() {
        return getName();
    }
}
