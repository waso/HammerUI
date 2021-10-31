/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.persintence;

public class ProjectChangeListenerImpl implements ProjectChangeListener {

    private static boolean changed = false;

    @Override
    public boolean isChanged() {
        return changed;
    }

    @Override
    public void setChanged() {
        changed = true;
    }

    @Override
    public void reset() {
        changed = false;
    }

}
