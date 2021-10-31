/*
 * Copyright (C) 2019 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.tests;

import com.google.common.collect.Lists;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import com.wsojka.hammerui.dto.ResponseDetails;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import static com.jayway.jsonpath.JsonPath.using;

public enum TestType {

    JSONPATH_EXISTS("jsonpath exists", Collections.singletonList("jsonpath"), (responseDetails, strings) -> {
        if (strings == null || strings.size() == 0 || StringUtils.isBlank(strings.get(0)) || responseDetails == null) {
            return false;
        }
        String jsonpath = strings.get(0);
        List<String> pathList = Lists.newArrayList();
        try {
            Configuration conf = Configuration.builder().options(Option.AS_PATH_LIST).build();
            pathList = using(conf).parse(responseDetails.getPayload()).read(jsonpath);
        } catch (PathNotFoundException ignore) {
        }
        return pathList != null && pathList.size() > 0;
    }, "jsonpath %s exists"),

    JSONPATH_NOT_EXIST("jsonpath does not exist", Collections.singletonList("jsonpath"), (responseDetails, strings) -> {
        if (strings == null || strings.size() == 0 || StringUtils.isBlank(strings.get(0)) || responseDetails == null) {
            return false;
        }
        String jsonpath = strings.get(0);
        List<String> pathList = Lists.newArrayListWithCapacity(0);
        try {
            Configuration conf = Configuration.builder().options(Option.AS_PATH_LIST).build();
            pathList = using(conf).parse(responseDetails.getPayload()).read(jsonpath);
        } catch (PathNotFoundException ignore) {
        }
        return pathList == null || pathList.size() == 0;
    }, "jsonpath %s does not exist"),

    JSONPATH_CONTAINS("jsonpath contains", Arrays.asList("jsonpath", "text"), (responseDetails, strings) -> {
        if (strings == null || strings.size() == 0 || StringUtils.isBlank(strings.get(0)) || StringUtils.isEmpty(strings.get(1)) || responseDetails == null) {
            return false;
        }
        String jsonpath = strings.get(0);
        String expectedValue = strings.get(1);
        List<Object> vals = Lists.newArrayListWithCapacity(0);
        try {
            Configuration conf = Configuration.builder().options(Option.ALWAYS_RETURN_LIST).build();
            vals = using(conf).parse(responseDetails.getPayload()).read(jsonpath);
        } catch (PathNotFoundException ignore) {
        }
        for (Object val : vals) {
            if (StringUtils.contains(String.valueOf(val), expectedValue))
                return true;
        }
        return false;
    }, "jsonpath %s contains %s"),

    JSONPATH_CONTAINS_IGNORE_CASE("jsonpath contains ignore case", Arrays.asList("jsonpath", "text"), (responseDetails, strings) -> {
        if (strings == null || strings.size() == 0 || StringUtils.isBlank(strings.get(0)) || StringUtils.isEmpty(strings.get(1)) || responseDetails == null) {
            return false;
        }
        String jsonpath = strings.get(0);
        String expectedValue = strings.get(1);
        List<Object> vals = Lists.newArrayListWithCapacity(0);
        try {
            Configuration conf = Configuration.builder().options(Option.ALWAYS_RETURN_LIST).build();
            vals = using(conf).parse(responseDetails.getPayload()).read(jsonpath);
        } catch (PathNotFoundException ignore) {
        }
        for (Object val : vals) {
            if (StringUtils.containsIgnoreCase(String.valueOf(val), expectedValue))
                return true;
        }
        return false;
    }, "jsonpath %s contains %s (ignore case)"),

    JSONPATH_NOT_CONTAIN("jsonpath does not contain", Arrays.asList("jsonpath", "text"), (responseDetails, strings) -> {
        if (strings == null || strings.size() == 0 || StringUtils.isBlank(strings.get(0)) || StringUtils.isEmpty(strings.get(1)) || responseDetails == null) {
            return false;
        }
        String jsonpath = strings.get(0);
        String expectedValue = strings.get(1);
        List<Object> vals = Lists.newArrayListWithCapacity(0);
        try {
            Configuration conf = Configuration.builder().options(Option.ALWAYS_RETURN_LIST).build();
            vals = using(conf).parse(responseDetails.getPayload()).read(jsonpath);
        } catch (PathNotFoundException ignore) {
        }
        boolean found = false;
        for (Object val : vals) {
            if (StringUtils.contains(String.valueOf(val), expectedValue)) {
                found = true;
                break;
            }
        }
        return !found;
    }, "jsonpath %s does not contain %s"),

    JSONPATH_COUNT_EQUAL_TO("jsonpath count equal", Arrays.asList("jsonpath", "count"), (responseDetails, strings) -> {
        if (strings == null || strings.size() == 0 || StringUtils.isBlank(strings.get(0)) || StringUtils.isEmpty(strings.get(1)) || responseDetails == null) {
            return false;
        }
        String jsonpath = strings.get(0);
        int expectedCount = Integer.parseInt(strings.get(1));
        List<Object> vals = Lists.newArrayListWithCapacity(0);
        try {
            Configuration conf = Configuration.builder().options(Option.ALWAYS_RETURN_LIST).build();
            vals = using(conf).parse(responseDetails.getPayload()).read(jsonpath);
        } catch (PathNotFoundException ignore) {
        }
        return expectedCount == vals.size();
    }, "jsonpath %s count equal to %s"),

    JSONPATH_COUNT_LESS_THAN("jsonpath count less than", Arrays.asList("jsonpath", "count"), (responseDetails, strings) -> {
        if (strings == null || strings.size() == 0 || StringUtils.isBlank(strings.get(0)) || StringUtils.isEmpty(strings.get(1)) || responseDetails == null) {
            return false;
        }
        String jsonpath = strings.get(0);
        int expectedCount = Integer.parseInt(strings.get(1));
        List<Object> vals = Lists.newArrayListWithCapacity(0);
        try {
            Configuration conf = Configuration.builder().options(Option.ALWAYS_RETURN_LIST).build();
            vals = using(conf).parse(responseDetails.getPayload()).read(jsonpath);
        } catch (PathNotFoundException ignore) {
        }
        return vals.size() < expectedCount;
    }, "jsonpath %s count less than %s"),

    JSONPATH_COUNT_GREATER_THAN("jsonpath count greater than", Arrays.asList("jsonpath", "count"), (responseDetails, strings) -> {
        if (strings == null || strings.size() == 0 || StringUtils.isBlank(strings.get(0)) || StringUtils.isEmpty(strings.get(1)) || responseDetails == null) {
            return false;
        }
        String jsonpath = strings.get(0);
        int expectedCount = Integer.parseInt(strings.get(1));
        List<Object> vals = Lists.newArrayListWithCapacity(0);
        try {
            Configuration conf = Configuration.builder().options(Option.ALWAYS_RETURN_LIST).build();
            vals = using(conf).parse(responseDetails.getPayload()).read(jsonpath);
        } catch (PathNotFoundException ignore) {
        }
        return vals.size() > expectedCount;
    }, "jsonpath %s count greater than %s"),

    JSONPATH_VALUE_EQUAL_TO("jsonpath value equal", Arrays.asList("jsonpath", "value"), (responseDetails, strings) -> {
        if (strings == null || strings.size() == 0 || StringUtils.isBlank(strings.get(0)) || StringUtils.isEmpty(strings.get(1)) || responseDetails == null) {
            return false;
        }
        String jsonpath = strings.get(0);
        String expectedValue = strings.get(1);
        List<Object> vals = Lists.newArrayListWithCapacity(0);
        try {
            Configuration conf = Configuration.builder().options(Option.ALWAYS_RETURN_LIST).build();
            vals = using(conf).parse(responseDetails.getPayload()).read(jsonpath);
        } catch (PathNotFoundException ignore) {
        }
        for (Object val : vals) {
            if (StringUtils.equals(String.valueOf(val), expectedValue))
                return true;
        }
        return false;
    }, "jsonpath %s value equal to %s"),

    RESPONSE_CONTAINS("response contains", Collections.singletonList("value"), (responseDetails, strings) -> {
        if (responseDetails == null || responseDetails.getPayload() == null || StringUtils.isBlank(responseDetails.getPayload()) ||
                strings == null || strings.size() == 0 || StringUtils.isEmpty(strings.get(0))) {
            return false;
        }
        return StringUtils.contains(responseDetails.getPayload(), strings.get(0));
    }, "response contains %s"),

    RESPONSE_CONTAINS_IGNORE_CASE("response contains (ignore case)", Collections.singletonList("value"), (responseDetails, strings) -> {
        if (responseDetails == null || responseDetails.getPayload() == null || StringUtils.isBlank(responseDetails.getPayload()) ||
                strings == null || strings.size() == 0 || StringUtils.isEmpty(strings.get(0))) {
            return false;
        }
        return StringUtils.containsIgnoreCase(responseDetails.getPayload(), strings.get(0));
    }, "response contains (ignore case) %s"),

    RESPONSE_NOT_CONTAIN("response does not contain", Collections.singletonList("value"), (responseDetails, strings) -> {
        if (responseDetails == null || responseDetails.getPayload() == null || StringUtils.isBlank(responseDetails.getPayload()) ||
                strings == null || strings.size() == 0 || StringUtils.isEmpty(strings.get(0))) {
            return false;
        }
        return !StringUtils.contains(responseDetails.getPayload(), strings.get(0));
    }, "response does not contain %s"),

    RESPONSE_NOT_CONTAIN_IGNORE_CASE("response does not contain (ignore case)", Collections.singletonList("value"), (responseDetails, strings) -> {
        if (responseDetails == null || responseDetails.getPayload() == null || StringUtils.isBlank(responseDetails.getPayload()) ||
                strings == null || strings.size() == 0 || StringUtils.isEmpty(strings.get(0))) {
            return false;
        }
        return !StringUtils.containsIgnoreCase(responseDetails.getPayload(), strings.get(0));
    }, "response does not contain (ignore case) %s"),

    HEADER_EXISTS("header exists", Collections.singletonList("header name"), (responseDetails, strings) -> {
        if (responseDetails == null || responseDetails.getHeaders() == null || responseDetails.getHeaders().size() == 0 ||
                strings == null || strings.size() == 0 || StringUtils.isEmpty(strings.get(0))) {
            return false;
        }
        String headerName = strings.get(0);
        return responseDetails.getHeaders().parallelStream().anyMatch(h -> StringUtils.equalsIgnoreCase(h.getName(), headerName));
    }, "header %s exists"),

    HEADER_NOT_EXIST("header does not exist", Collections.singletonList("header name"), (responseDetails, strings) -> {
        if (responseDetails == null || responseDetails.getHeaders() == null || responseDetails.getHeaders().size() == 0 ||
                strings == null || strings.size() == 0 || StringUtils.isEmpty(strings.get(0))) {
            return false;
        }
        String headerName = strings.get(0);
        return responseDetails.getHeaders().parallelStream().noneMatch(h -> StringUtils.equalsIgnoreCase(h.getName(), headerName));
    }, "header %s does not exist"),

    HEADER_CONTAINS("header contains", Collections.singletonList("header value"), (responseDetails, strings) -> {
        if (responseDetails == null || responseDetails.getHeaders() == null || responseDetails.getHeaders().size() == 0 ||
                strings == null || strings.size() == 0 || StringUtils.isEmpty(strings.get(0))) {
            return false;
        }
        String headerValue = strings.get(0);
        return responseDetails.getHeaders().parallelStream().anyMatch(h -> StringUtils.contains(h.getValue(), headerValue));
    }, "header contains %s"),

    HEADER_NOT_CONTAIN("header does not contain", Collections.singletonList("header value"), (responseDetails, strings) -> {
        if (responseDetails == null || responseDetails.getHeaders() == null || responseDetails.getHeaders().size() == 0 ||
                strings == null || strings.size() == 0 || StringUtils.isEmpty(strings.get(0))) {
            return false;
        }
        String headerValue = strings.get(0);
        return responseDetails.getHeaders().parallelStream().noneMatch(h -> StringUtils.contains(h.getValue(), headerValue));
    }, "header does not contain %s"),

    STATUS_CODE_BETWEEN("status code between", Arrays.asList("min", "max"), (responseDetails, strings) -> {
        if (responseDetails == null || responseDetails.getHeaders() == null || responseDetails.getHeaders().size() == 0 ||
                strings == null || strings.size() == 0 || StringUtils.isEmpty(strings.get(0)) || StringUtils.isEmpty(strings.get(1))) {
            return false;
        }
        int min = Integer.parseInt(strings.get(0));
        int max = Integer.parseInt(strings.get(1));
        return responseDetails.getReturnCode() >= min && responseDetails.getReturnCode() <= max;
    }, "status code between %s and %s"),

    STATUS_CODE_NOT_BETWEEN("status code not between", Arrays.asList("min", "max"), (responseDetails, strings) -> {
        if (responseDetails == null || responseDetails.getHeaders() == null || responseDetails.getHeaders().size() == 0 ||
                strings == null || strings.size() == 0 || StringUtils.isEmpty(strings.get(0)) || StringUtils.isEmpty(strings.get(1))) {
            return false;
        }
        int min = Integer.parseInt(strings.get(0));
        int max = Integer.parseInt(strings.get(1));
        return !(responseDetails.getReturnCode() >= min && responseDetails.getReturnCode() <= max);
    }, "status code not between %s and %s"),

    STATUS_CODE_EQUAL_TO("status code equal", Collections.singletonList("status code"), (responseDetails, strings) -> {
        if (responseDetails == null || responseDetails.getHeaders() == null || responseDetails.getHeaders().size() == 0 ||
                strings == null || strings.size() == 0 || StringUtils.isEmpty(strings.get(0))) {
            return false;
        }
        int expectedStatusCode = Integer.parseInt(strings.get(0));
        return responseDetails.getReturnCode() == expectedStatusCode;
    }, "status code equal to %s"),

    STATUS_CODE_NOT_EQUAL_TO("status code different than", Collections.singletonList("status code"), (responseDetails, strings) -> {
        if (responseDetails == null || responseDetails.getHeaders() == null || responseDetails.getHeaders().size() == 0 ||
                strings == null || strings.size() == 0 || StringUtils.isEmpty(strings.get(0))) {
            return false;
        }
        int expectedStatusCode = Integer.parseInt(strings.get(0));
        return responseDetails.getReturnCode() != expectedStatusCode;
    }, "status code different than %s"),

    RESPONSE_TIME_LESS_THAN("response time less than", Collections.singletonList("time (ms)"), (responseDetails, strings) -> {
        if (responseDetails == null || responseDetails.getHeaders() == null || responseDetails.getHeaders().size() == 0 ||
                strings == null || strings.size() == 0 || StringUtils.isEmpty(strings.get(0))) {
            return false;
        }
        long expectedResponseTime = Long.parseLong(strings.get(0));
        return responseDetails.getResponseTimeInMs() < expectedResponseTime;
    }, "response time less than %s ms"),

    RESPONSE_TIME_GREATER_THAN("response time greater than", Collections.singletonList("time (ms)"), (responseDetails, strings) -> {
        if (responseDetails == null || responseDetails.getHeaders() == null || responseDetails.getHeaders().size() == 0 ||
                strings == null || strings.size() == 0 || StringUtils.isEmpty(strings.get(0))) {
            return false;
        }
        long expectedResponseTime = Long.parseLong(strings.get(0));
        return responseDetails.getResponseTimeInMs() > expectedResponseTime;
    }, "response time greater than %s ms"),
    ;

    private String description;
    private List<String> labels;
    private BiFunction<ResponseDetails, List<String>, Boolean> validator;
    private String toStringFormat;

    TestType(String description, List<String> labels, BiFunction<ResponseDetails, List<String>, Boolean> validator, String toStringFormat) {
        this.description = description;
        this.labels = labels;
        this.validator = validator;
        this.toStringFormat = toStringFormat;
    }

    public String getDescription() {
        return description;
    }

    public BiFunction<ResponseDetails, List<String>, Boolean> getValidator() {
        return validator;
    }

    public List<String> getLabels() {
        return labels;
    }

    public String getToStringFormat() {
        return toStringFormat;
    }

    @Override
    public String toString() {
        return this.description;
    }
}
