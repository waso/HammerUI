/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.persintence;

import com.google.common.collect.Lists;
import com.wsojka.hammerui.Main;
import com.wsojka.hammerui.dto.LicenseFile;
import com.wsojka.hammerui.utils.ConsoleLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

class PreferencesWrapperImpl implements PreferencesWrapper {

    private Preferences prefs;
    private static final String PROJECT_PATHS_PREFIX = "LastProject";
    private static final String PROJECT_SELECTED_REQUEST = "RequestSelected";
    private static final String PROJECT_SELECTED_REQUEST_TAB = "RequestSelectedTab";
    private static final String PROJECT_OPEN_REQUESTS = "OpenRequests";
    private static final String LOCALE = "Locale";
    private static final String APPLICATION_SETTINGS = "ApplicationSettings";
    private static final String LICENSE = "License";

    public PreferencesWrapperImpl() {
        prefs = Preferences.userRoot().node(Main.class.getName());
    }

    @Override
    public List<Path> getLastOpenProjects() {
        List<Path> projects = new ArrayList<>();
        try {
            for (int i = 1; i <= 10; i++) {
                String path = prefs.get(PROJECT_PATHS_PREFIX + i, "");
                if (!path.isEmpty())
                    projects.add(Paths.get(path));
            }
        } catch (Throwable e) {
            ConsoleLogger.error("problem with java preferences", e);
        }
        return projects;
    }

    @Override
    public void addPathToLastOpenProjects(Path path) {
        try {
            List<Path> paths = this.getLastOpenProjects();
            paths = paths.stream().filter(p -> !p.equals(path)).collect(Collectors.toList());
            if (paths.size() > 0)
                paths.add(0, path);
            else
                paths.add(path);

            for (int i = 1; i <= paths.size(); i++) {
                prefs.put(PROJECT_PATHS_PREFIX + i, paths.get(i - 1).toString());
            }
        } catch (Throwable e) {
            ConsoleLogger.error("problem with java preferences", e);
        }
        flushPreferences();
    }

    @Override
    public boolean showAutocompleteSuggestions() {
        return true;
    }

    public List<String> getOpenRequests(String projectUuid) {
        List<String> openRequests = Lists.newArrayList();
        ConsoleLogger.info("getting list of open requests for project {} ", projectUuid);
        try {
            Preferences pref = prefs.node(PROJECT_OPEN_REQUESTS);
            byte[] bytes = pref.getByteArray(projectUuid, new byte[]{});
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream sii = new ObjectInputStream(bis);
            openRequests = (List<String>) sii.readObject();
        } catch (Throwable e) {
            ConsoleLogger.error("list of open requests not retrieved");
        }
        return openRequests;
    }

    public void setOpenRequests(String projectUuid, List<String> requests) {
        ConsoleLogger.info("setting open requests list for project {} to {}", projectUuid, requests);
        try {
            Preferences pref = prefs.node(PROJECT_OPEN_REQUESTS);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(requests);
            byte[] bytes = bos.toByteArray();
            pref.putByteArray(projectUuid, bytes);
            flushPreferences();
        } catch (Throwable e) {
            ConsoleLogger.error("problem with serializing list of open requests");
        }
    }

    public String getSelectedRequest(String projectUuid) {
        String uuid = "";
        ConsoleLogger.info("getting selected request for project {} ", projectUuid);
        try {
            Preferences pref = prefs.node(PROJECT_SELECTED_REQUEST);
            uuid = pref.get(projectUuid, "");
        } catch (Throwable e) {
            ConsoleLogger.error("problem with project preferences");
        }
        return uuid;
    }

    public void setSelectedRequest(String projectUuid, String requestId) {
        ConsoleLogger.info("setting selected request for project {} to {}", projectUuid, requestId);
        try {
            Preferences pref = prefs.node(PROJECT_SELECTED_REQUEST);
            pref.put(projectUuid, requestId);
        } catch (Throwable e) {
            ConsoleLogger.error("problem with java preferences", e);
        }
        flushPreferences();
    }

    public void setSelectedRequestOptionsTab(String requestId, int optionsTabIndex) {
        ConsoleLogger.info("setting request options tab {} for request {}", optionsTabIndex, requestId);
        try {
            Preferences pref = prefs.node(PROJECT_SELECTED_REQUEST_TAB);
            pref.putInt(requestId, optionsTabIndex);
        } catch (Throwable e) {
            ConsoleLogger.error("problem with java preferences", e);
        }
        flushPreferences();
    }

    public int getSelectedRequestOptionsTab(String requestId) {
        int index = 0;
        ConsoleLogger.info("getting selected options tab for request {}", requestId);
        try {
            Preferences pref = prefs.node(PROJECT_SELECTED_REQUEST_TAB);
            index = pref.getInt(requestId, 0);
        } catch (Throwable e) {
            ConsoleLogger.error("problem with project preferences");
        }
        return index;
    }

    private void flushPreferences() {
        try {
            prefs.flush();
        } catch (Throwable e) {
            ConsoleLogger.error("problem with java preferences", e);
        }
    }


    @Override
    public void setLocale(Locale locale) {
        ConsoleLogger.info("setting locale {}", locale);
        try {
            Preferences pref = prefs.node(LOCALE);
            pref.put(LOCALE, locale.getLanguage());
        } catch (Throwable e) {
            ConsoleLogger.error("problem with java preferences", e);
        }
        flushPreferences();
    }

    @Override
    public Locale getLocale() {
        Locale locale = new Locale("en", "EN");
        try {
            Preferences pref = prefs.node(LOCALE);
            locale = Locale.forLanguageTag(pref.get(LOCALE, "en_EN"));
        } catch (Throwable e) {
            ConsoleLogger.error("problem with project preferences");
        }
        return locale;
    }

    @Override
    public void setApplicationSettings(ApplicationSettings settings) {
        ConsoleLogger.info("setting application settings to {}", settings);
        try {
            Preferences pref = prefs.node(APPLICATION_SETTINGS);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(settings);
            byte[] bytes = bos.toByteArray();
            pref.putByteArray(APPLICATION_SETTINGS, bytes);
            flushPreferences();
        } catch (Throwable e) {
            ConsoleLogger.error("problem with serializing application settings");
        }
    }

    @Override
    public ApplicationSettings getApplicationSettings() {
        ApplicationSettings applicationSettings = new ApplicationSettings();
        ConsoleLogger.debug("getting application settings ");
        try {
            Preferences pref = prefs.node(APPLICATION_SETTINGS);
            byte[] bytes = pref.getByteArray(APPLICATION_SETTINGS, new byte[]{});
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream sii = new ObjectInputStream(bis);
            applicationSettings = (ApplicationSettings) sii.readObject();
        } catch (Throwable e) {
            ConsoleLogger.error("problem with application settings");
        }
        return applicationSettings;
    }

    @Override
    public void setLicenseFile(LicenseFile licenseFile) {
        ConsoleLogger.info("setting license {}", licenseFile);
        try {
            Preferences pref = prefs.node(LICENSE);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(licenseFile);
            byte[] bytes = bos.toByteArray();
            pref.putByteArray(LICENSE, bytes);
            flushPreferences();
        } catch (Throwable e) {
            ConsoleLogger.error("problem with serializing license");
        }
    }

    @Override
    public LicenseFile getLicenseFile() {
        ConsoleLogger.info("getting license details");
        try {
            Preferences pref = prefs.node(LICENSE);
            byte[] bytes = pref.getByteArray(LICENSE, new byte[]{});
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream sii = new ObjectInputStream(bis);
            return (LicenseFile) sii.readObject();
        } catch (Throwable e) {
            ConsoleLogger.error("problem with application settings");
        }
        return null;
    }
}
