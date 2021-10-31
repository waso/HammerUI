/*
 * Copyright (C) 2020 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.persintence;

import com.wsojka.hammerui.Constants;

public class BaseSettings {

    private boolean followRedirects = false;

    protected int connectionTimeout = Constants.DEFAULT_CONNECTION_TIMEOUT;

    protected int socketReadTimeout = Constants.DEFAULT_SOCKET_READ_TIMEOUT;

    protected boolean ignoreSslErrors = false;

    protected boolean useTcpNodelay = false;

    public boolean isFollowRedirects() {
        return followRedirects;
    }

    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSocketReadTimeout() {
        return socketReadTimeout;
    }

    public void setSocketReadTimeout(int socketReadTimeout) {
        this.socketReadTimeout = socketReadTimeout;
    }

    public boolean isIgnoreSslErrors() {
        return ignoreSslErrors;
    }

    public void setIgnoreSslErrors(boolean ignoreSslErrors) {
        this.ignoreSslErrors = ignoreSslErrors;
    }

    public boolean isUseTcpNodelay() {
        return useTcpNodelay;
    }

    public void setUseTcpNodelay(boolean useTcpNodelay) {
        this.useTcpNodelay = useTcpNodelay;
    }

}
