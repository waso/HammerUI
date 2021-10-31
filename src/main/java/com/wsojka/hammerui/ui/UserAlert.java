/*
 * Copyright (C) 2019 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.ui;

import javafx.scene.control.Alert;

public class UserAlert {

    private String title;

    private String contentText;

    private Alert.AlertType type;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public Alert.AlertType getType() {
        return type;
    }

    public void setType(Alert.AlertType type) {
        this.type = type;
    }
}
