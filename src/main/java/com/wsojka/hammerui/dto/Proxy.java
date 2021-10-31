/*
 * Copyright (C) 2018 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.dto;

import com.wsojka.hammerui.enums.ManualProxySettingsType;
import com.wsojka.hammerui.enums.ProxySettingsType;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

public final class Proxy implements DeepCopy {

    private String uuid;

    private String name = "";

    private ProxySettingsType proxySettingsType = ProxySettingsType.NONE;

    private ManualProxySettingsType manualProxySettingsType = ManualProxySettingsType.HTTP;

    private boolean pacScriptEnabled;

    private String pacScript = "";

    private String hostName = "";

    private int portNumber;

    private boolean proxyAuthenticationEnabled;

    private String proxyUsername = "";

    private String proxyPassword = "";

    public Proxy copy() {
        Proxy newProxy = new Proxy();
        newProxy.setName(this.getName());
        newProxy.setUuid(this.getUuid());
        newProxy.setProxySettingsType(this.getProxySettingsType());
        newProxy.setManualProxySettingsType(this.getManualProxySettingsType());
        newProxy.setPacScript(this.getPacScript());
        newProxy.setPacScriptEnabled(this.isPacScriptEnabled());
        newProxy.setHostName(this.getHostName());
        newProxy.setPortNumber(this.getPortNumber());
        newProxy.setProxyUsername(this.getProxyUsername());
        newProxy.setProxyPassword(this.getProxyPassword());
        newProxy.setProxyAuthenticationEnabled(this.isProxyAuthenticationEnabled());
        return newProxy;
    }

    public String getPacScript() {
        return pacScript;
    }

    public void setPacScript(String pacScript) {
        this.pacScript = pacScript;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public String getProxyUsername() {
        return new String(Base64.getDecoder().decode(proxyUsername.getBytes(StandardCharsets.UTF_8)));
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = Base64.getEncoder().encodeToString(proxyUsername.getBytes(StandardCharsets.UTF_8));
    }

    public String getProxyPassword() {
        return new String(Base64.getDecoder().decode(proxyPassword.getBytes(StandardCharsets.UTF_8)));
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = Base64.getEncoder().encodeToString(proxyPassword.getBytes(StandardCharsets.UTF_8));
    }

    public boolean isProxyAuthenticationEnabled() {
        return proxyAuthenticationEnabled;
    }

    public void setProxyAuthenticationEnabled(boolean proxyAuthenticationEnabled) {
        this.proxyAuthenticationEnabled = proxyAuthenticationEnabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProxySettingsType getProxySettingsType() {
        return proxySettingsType;
    }

    public void setProxySettingsType(ProxySettingsType proxySettingsType) {
        this.proxySettingsType = proxySettingsType;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public ManualProxySettingsType getManualProxySettingsType() {
        return manualProxySettingsType;
    }

    public void setManualProxySettingsType(ManualProxySettingsType manualProxySettingsType) {
        this.manualProxySettingsType = manualProxySettingsType;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isPacScriptEnabled() {
        return pacScriptEnabled;
    }

    public void setPacScriptEnabled(boolean pacScriptEnabled) {
        this.pacScriptEnabled = pacScriptEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Proxy proxy = (Proxy) o;
        return Objects.equals(uuid, proxy.uuid) &&
                Objects.equals(name, proxy.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name);
    }
}
