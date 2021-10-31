/*
 * Copyright (C) 2020 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class LicenseFile implements Serializable {

    @Serial
    private static final long serialVersionUID = -672068590631904253L;

    private String licenseKey;

    private String organization;

    private String date;

    private String type;

    public String getLicenseKey() {
        return licenseKey;
    }

    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LicenseFile that = (LicenseFile) o;
        return Objects.equals(licenseKey, that.licenseKey) &&
                Objects.equals(organization, that.organization) &&
                Objects.equals(date, that.date) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(licenseKey, organization, date, type);
    }

    @Override
    public String toString() {
        return "LicenseFile{" +
                "licenseKey='" + licenseKey + '\'' +
                ", organization='" + organization + '\'' +
                ", date='" + date + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
