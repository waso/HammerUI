/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.persintence;

import com.wsojka.hammerui.dto.LicenseFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public interface PreferencesWrapper {

    List<Path> getLastOpenProjects();

    void addPathToLastOpenProjects(Path path);

    boolean showAutocompleteSuggestions();

    List<String> getOpenRequests(String projectUuid);

    void setOpenRequests(String projectUuid, List<String> requests);

    String getSelectedRequest(String projectUuid);

    void setSelectedRequest(String projectUuid, String requestId);

    void setSelectedRequestOptionsTab(String requestId, int optionsTabIndex);

    int getSelectedRequestOptionsTab(String requestId);

    void setLocale(Locale locale);

    Locale getLocale();

    void setApplicationSettings(ApplicationSettings settings);

    ApplicationSettings getApplicationSettings();

    void setLicenseFile(LicenseFile licenseFile);

    LicenseFile getLicenseFile();
}
