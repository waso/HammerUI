/*
 * Copyright (C) 2020 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui;

import com.google.common.collect.Lists;
import com.wsojka.hammerui.dto.Header;
import com.wsojka.hammerui.dto.ResponseDetails;
import com.wsojka.hammerui.tests.TestType;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestTypeTests {

    private ResponseDetails response;

    @BeforeAll
    public void setUp() throws Exception {
        TestTypeTests tst = new TestTypeTests();

        String json = IOUtils.toString(tst.getClass().getResourceAsStream("/test_response.json"), StandardCharsets.UTF_8);
        response = new ResponseDetails();
        response.setPayload(json);
        List<Header> headers = Lists.newArrayList();
        headers.add(new Header("header_name", "header_value"));
        response.setHeaders(headers);
        response.setContentLength(headers.size());
        response.setReturnCode(200);
        response.setResponseTimeInMs(2000);
    }

    @Test
    public void jsonpathExistsTest() {
        TestType t = TestType.JSONPATH_EXISTS;
        boolean result = t.getValidator().apply(response, Arrays.asList("$.name.first"));
        assertTrue(result);
    }

    @Test
    public void jsonpathExistsTest2() {
        TestType t = TestType.JSONPATH_EXISTS;
        boolean result = t.getValidator().apply(response, Arrays.asList("$.blah.first"));
        assertFalse(result);
    }

    @Test
    public void jsonpathNotExistsTest() {
        TestType t = TestType.JSONPATH_NOT_EXIST;
        boolean result = t.getValidator().apply(response, Arrays.asList("$.blah.first"));
        assertTrue(result);
    }

    @Test
    public void jsonpathNotExistsTest2() {
        TestType t = TestType.JSONPATH_NOT_EXIST;
        boolean result = t.getValidator().apply(response, Arrays.asList("$.name.first"));
        assertFalse(result);
    }

    @Test
    public void jsonpathContains() {
        TestType t = TestType.JSONPATH_CONTAINS;
        assertTrue(t.getValidator().apply(response, Arrays.asList("$.name.first", "Ada")));
        assertTrue(t.getValidator().apply(response, Arrays.asList("$.index", "0")));
        assertTrue(t.getValidator().apply(response, Arrays.asList("$.age", "38")));
        assertTrue(t.getValidator().apply(response, Arrays.asList("$.isActive", "true")));
    }

    @Test
    public void jsonpathContains2() {
        TestType t = TestType.JSONPATH_CONTAINS;
        assertFalse(t.getValidator().apply(response, Arrays.asList("$.name.first", "ada")));
        assertFalse(t.getValidator().apply(response, Arrays.asList("$.isActive", "false")));
    }

    @Test
    public void jsonpathContains3() {
        TestType t = TestType.JSONPATH_CONTAINS_IGNORE_CASE;
        assertTrue(t.getValidator().apply(response, Arrays.asList("$.name.first", "Ada")));
        assertTrue(t.getValidator().apply(response, Arrays.asList("$.name.first", "ADA")));
    }

    @Test
    public void jsonpathContains4() {
        TestType t = TestType.JSONPATH_NOT_CONTAIN;
        assertTrue(t.getValidator().apply(response, Arrays.asList("$.name.first", "ada")));
        assertTrue(t.getValidator().apply(response, Arrays.asList("$.isActive", "false")));
    }

    @Test
    public void jsonpathCountEquals() {
        TestType t = TestType.JSONPATH_COUNT_EQUAL_TO;
        assertTrue(t.getValidator().apply(response, Arrays.asList("$.test[*].name", "3")));
        assertFalse(t.getValidator().apply(response, Arrays.asList("$.test[*].name", "2")));
        assertFalse(t.getValidator().apply(response, Arrays.asList("$.test[*].name", "4")));
    }

    @Test
    public void jsonpathCount2() {
        TestType t = TestType.JSONPATH_COUNT_LESS_THAN;
        assertTrue(t.getValidator().apply(response, Arrays.asList("$.test[*].name", "4")));
        assertFalse(t.getValidator().apply(response, Arrays.asList("$.test[*].name", "2")));
        assertFalse(t.getValidator().apply(response, Arrays.asList("$.test[*].name", "3")));
    }

    @Test
    public void jsonpathCount3() {
        TestType t = TestType.JSONPATH_COUNT_GREATER_THAN;
        assertTrue(t.getValidator().apply(response, Arrays.asList("$.test[*].name", "2")));
        assertFalse(t.getValidator().apply(response, Arrays.asList("$.test[*].name", "3")));
        assertFalse(t.getValidator().apply(response, Arrays.asList("$.test[*].name", "4")));
    }

    @Test
    public void jsonpathValueEquals() {
        TestType t = TestType.JSONPATH_VALUE_EQUAL_TO;
        assertTrue(t.getValidator().apply(response, Arrays.asList("$.balance", "$2,680.56")));
        assertTrue(t.getValidator().apply(response, Arrays.asList("$.regional", "ąśżźćłóę")));
    }

    @Test
    public void responseContainsTest() {
        TestType t = TestType.RESPONSE_CONTAINS;
        assertTrue(t.getValidator().apply(response, Arrays.asList("\"picture\": \"http://aaa.bbb/32x32\",")));
        assertTrue(t.getValidator().apply(response, Arrays.asList("\"test\": [")));
        assertFalse(t.getValidator().apply(response, Arrays.asList("foo")));
    }

    @Test
    public void responseContainsIgnoreCaseTest() {
        TestType t = TestType.RESPONSE_CONTAINS_IGNORE_CASE;
        assertTrue(t.getValidator().apply(response, Arrays.asList("\"PICTURE\": \"http://aaa.bbb/32x32\",")));
        assertTrue(t.getValidator().apply(response, Arrays.asList("\"EYECOLOR")));
        assertFalse(t.getValidator().apply(response, Arrays.asList("BAR")));
    }

    @Test
    public void responseNotContainsTest() {
        TestType t = TestType.RESPONSE_NOT_CONTAIN;
        assertTrue(t.getValidator().apply(response, Arrays.asList("foo")));
        assertFalse(t.getValidator().apply(response, Arrays.asList("name")));
    }

    @Test
    public void responseNotContainsIgnoreCaseTest() {
        TestType t = TestType.RESPONSE_NOT_CONTAIN_IGNORE_CASE;
        assertTrue(t.getValidator().apply(response, Arrays.asList("FOOX")));
        assertTrue(t.getValidator().apply(response, Arrays.asList("fooX")));
        assertFalse(t.getValidator().apply(response, Arrays.asList("name")));
        assertFalse(t.getValidator().apply(response, Arrays.asList("NAME")));
    }

    @Test
    public void responseHeaderExistsTest() {
        TestType t = TestType.HEADER_EXISTS;
        assertTrue(t.getValidator().apply(response, Arrays.asList("header_name")));
        assertTrue(t.getValidator().apply(response, Arrays.asList("HEADER_NAME")));
        assertFalse(t.getValidator().apply(response, Arrays.asList("foo")));
    }

    @Test
    public void responseHeaderNotExistTest() {
        TestType t = TestType.HEADER_NOT_EXIST;
        assertFalse(t.getValidator().apply(response, Arrays.asList("header_name")));
        assertFalse(t.getValidator().apply(response, Arrays.asList("HEADER_NAME")));
        assertTrue(t.getValidator().apply(response, Arrays.asList("foo")));
    }

    @Test
    public void responseHeaderContainsTest() {
        TestType t = TestType.HEADER_CONTAINS;
        assertTrue(t.getValidator().apply(response, Arrays.asList("header_value")));
        assertFalse(t.getValidator().apply(response, Arrays.asList("HEADER_value")));
        assertFalse(t.getValidator().apply(response, Arrays.asList("foo")));
    }

    @Test
    public void responseHeaderNotContainTest() {
        TestType t = TestType.HEADER_NOT_CONTAIN;
        assertFalse(t.getValidator().apply(response, Arrays.asList("header_value")));
        assertTrue(t.getValidator().apply(response, Arrays.asList("HEADER_value")));
        assertTrue(t.getValidator().apply(response, Arrays.asList("foo")));
    }

    @Test
    public void responseStatusCodeBetweenTest() {
        TestType t = TestType.STATUS_CODE_BETWEEN;
        assertTrue(t.getValidator().apply(response, Arrays.asList("199", "201")));
        assertTrue(t.getValidator().apply(response, Arrays.asList("200", "200")));
        assertFalse(t.getValidator().apply(response, Arrays.asList("400", "499")));
    }

    @Test
    public void responseStatusCodeNotBetweenTest() {
        TestType t = TestType.STATUS_CODE_NOT_BETWEEN;
        assertFalse(t.getValidator().apply(response, Arrays.asList("199", "201")));
        assertFalse(t.getValidator().apply(response, Arrays.asList("200", "200")));
        assertTrue(t.getValidator().apply(response, Arrays.asList("400", "499")));
    }

    @Test
    public void responseStatusCodeEqualToTest() {
        TestType t = TestType.STATUS_CODE_EQUAL_TO;
        assertTrue(t.getValidator().apply(response, Arrays.asList("200")));
        assertFalse(t.getValidator().apply(response, Arrays.asList("201")));
    }

    @Test
    public void responseStatusCodeNotEqualToTest() {
        TestType t = TestType.STATUS_CODE_NOT_EQUAL_TO;
        assertFalse(t.getValidator().apply(response, Arrays.asList("200")));
        assertTrue(t.getValidator().apply(response, Arrays.asList("201")));
    }

    @Test
    public void responseTimeLessThanTest() {
        TestType t = TestType.RESPONSE_TIME_LESS_THAN;
        assertTrue(t.getValidator().apply(response, Arrays.asList("4000")));
        assertFalse(t.getValidator().apply(response, Arrays.asList("2000")));
        assertFalse(t.getValidator().apply(response, Arrays.asList("1999")));
    }

    @Test
    public void responseTimeGreaterThanTest() {
        TestType t = TestType.RESPONSE_TIME_GREATER_THAN;
        assertFalse(t.getValidator().apply(response, Arrays.asList("4000")));
        assertFalse(t.getValidator().apply(response, Arrays.asList("2000")));
        assertTrue(t.getValidator().apply(response, Arrays.asList("1999")));
    }
}
