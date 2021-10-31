/*
 * Copyright (C) 2018 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui;

import com.wsojka.hammerui.utils.ConsoleLogger;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Constants {

    public static int VERSION_MAJOR_NUMBER = 1;

    public static int VERSION_MINOR_NUMBER = 0;

    static {
        try {
            InputStream in = Main.class.getResourceAsStream("/build_version.txt");
            String version = IOUtils.toString(in, StandardCharsets.UTF_8);
            String[] numbers = StringUtils.splitByWholeSeparator(version.replace("-SNAPSHOT", ""), ".");
            VERSION_MAJOR_NUMBER = Integer.parseInt(numbers[0]);
            VERSION_MINOR_NUMBER = Integer.parseInt(numbers[1]);
            ConsoleLogger.info("starting HammerUI " + version);
        } catch (Exception e) {
            //ignore
        }
    }

    public static final String APP_NAME = "hammerui";

    public static final String VERSION_STRING = VERSION_MAJOR_NUMBER + "." + VERSION_MINOR_NUMBER;

    public static final int MINIMUM_VERSION_MAJOR_NUMBER = 0;

    public static final int MINIMUM_VERSION_MINOR_NUMBER = 1;

    public static final String MINIMUM_VERSION_STRING = MINIMUM_VERSION_MAJOR_NUMBER + "." + MINIMUM_VERSION_MINOR_NUMBER;

    public static final String PARAM_OPENING_TAG = "{";

    public static final String PARAM_CLOSING_TAG = "}";

    public static final int DEFAULT_CONNECTION_TIMEOUT = 60_000;

    public static final int DEFAULT_SOCKET_READ_TIMEOUT = 60_000;
}
