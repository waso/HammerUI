/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.persintence;

/**
 * Global state to know if project has been changed/updated since the
 * last save to a file.
 */
public interface ProjectChangeListener {

    boolean isChanged();

    void setChanged();

    void reset();
}
