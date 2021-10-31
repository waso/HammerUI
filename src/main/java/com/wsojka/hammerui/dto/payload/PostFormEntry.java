/*
 * Copyright (C) 2018 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.dto.payload;

import com.wsojka.hammerui.enums.PostFormEntryType;

import java.util.Objects;

public class PostFormEntry {

    /* for ASCI data, fieldName contains param name, and fieldValue contains param value
     * for binary data, fieldName contains param name, and data field contains binary data
     */
    private PostFormEntryType type;
    private String fieldName = "text/html";
    private String fieldValue = "";

    public PostFormEntry() {}

    public PostFormEntry(PostFormEntryType type, String fieldName, String fieldValue) {
        this.type = type;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public PostFormEntryType getType() {
        return type;
    }

    public void setType(PostFormEntryType type) {
        this.type = type;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostFormEntry that = (PostFormEntry) o;
        return type == that.type &&
                Objects.equals(fieldName, that.fieldName) &&
                Objects.equals(fieldValue, that.fieldValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, fieldName, fieldValue);
    }
}
