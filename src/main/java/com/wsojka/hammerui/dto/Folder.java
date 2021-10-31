/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.dto;

import java.io.Serial;
import java.io.Serializable;

public class Folder extends Item implements Serializable {

    @Serial
    private static final long serialVersionUID = 1562826963951136936L;

    public Folder() {
        super();
    }

    public Folder(String name) {
        super(name);
    }
}
