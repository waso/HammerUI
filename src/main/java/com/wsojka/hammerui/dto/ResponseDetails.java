/*
 * Copyright (C) 2018 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.dto;

import com.google.common.collect.Lists;
import com.wsojka.hammerui.utils.ConsoleLogger;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class ResponseDetails {

    private String payload;

    private int returnCode;

    private long responseTimeInMs;

    private List<Header> headers = Lists.newArrayList();

    private String log;

    private long contentLength;

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public long getResponseTimeInMs() {
        return responseTimeInMs;
    }

    public String getResponseTimeSummary() {
        ConsoleLogger.info("response time: " + responseTimeInMs);
        if (responseTimeInMs < 1000)
            return String.format("%d ms", responseTimeInMs);
        else {
            double t = responseTimeInMs / 1000d;
            return String.format("%.1f s", t);
        }
    }

    public void setResponseTimeInMs(long responseTimeInMs) {
        this.responseTimeInMs = responseTimeInMs;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    @Override
    public String toString() {
        return "ResponseDetails{" +
                "payload='" + StringUtils.replace(payload, "\n", "") + '\'' +
                ", returnCode=" + returnCode +
                ", responseTimeInMs=" + responseTimeInMs +
                ", headers=" + headers +
                ", log='" + log + '\'' +
                ", contentLength=" + contentLength +
                '}';
    }
}
