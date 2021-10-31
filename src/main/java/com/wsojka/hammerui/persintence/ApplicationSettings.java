/*
 * Copyright (C) 2020 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.persintence;

import com.wsojka.hammerui.Constants;
import com.wsojka.hammerui.enums.AppTheme;
import com.wsojka.hammerui.enums.EditorTheme;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class ApplicationSettings implements Serializable {

    @Serial
    private static final long serialVersionUID = -3019539072346891117L;

    private boolean autoSaveProject = false;

    private boolean openLastProjectAtStartup = true;

    private String userAgentString = Constants.APP_NAME + " " + Constants.VERSION_STRING;

    private AppTheme appTheme = AppTheme.Light;

    private EditorTheme editorTheme = EditorTheme.Eclipse;

    public boolean isAutoSaveProject() {
        return autoSaveProject;
    }

    public void setAutoSaveProject(boolean autoSaveProject) {
        this.autoSaveProject = autoSaveProject;
    }

    public boolean isOpenLastProjectAtStartup() {
        return openLastProjectAtStartup;
    }

    public void setOpenLastProjectAtStartup(boolean openLastProjectAtStartup) {
        this.openLastProjectAtStartup = openLastProjectAtStartup;
    }

    public String getUserAgentString() {
        return userAgentString;
    }

    public void setUserAgentString(String userAgentString) {
        this.userAgentString = userAgentString;
    }

    public AppTheme getAppTheme() {
        return appTheme;
    }

    public void setAppTheme(AppTheme appTheme) {
        this.appTheme = appTheme;
    }

    public EditorTheme getEditorTheme() {
        return editorTheme;
    }

    public void setEditorTheme(EditorTheme editorTheme) {
        this.editorTheme = editorTheme;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationSettings that = (ApplicationSettings) o;
        return autoSaveProject == that.autoSaveProject && openLastProjectAtStartup == that.openLastProjectAtStartup && Objects.equals(userAgentString, that.userAgentString) && appTheme == that.appTheme && editorTheme == that.editorTheme;
    }

    @Override
    public int hashCode() {
        return Objects.hash(autoSaveProject, openLastProjectAtStartup, userAgentString, appTheme, editorTheme);
    }

    @Override
    public String toString() {
        return "ApplicationSettings{" +
                "autoSaveProject=" + autoSaveProject +
                ", openLastProjectAtStartup=" + openLastProjectAtStartup +
                ", userAgentString='" + userAgentString + '\'' +
                ", appTheme=" + appTheme +
                ", editorTheme=" + editorTheme +
                '}';
    }
}
