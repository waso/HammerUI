/*
 * Copyright (C) 2020 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui;

import com.wsojka.hammerui.utils.UIUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UIUtilsTests {

    // UIUtils.compareVersions logic:
    //
    // -1 -> project file version > app version
    //  0 -> project file version = app version
    //  1 -> project file version < app version

    @Test
    public void testLowerMajorVersionNumber() {
        int ret = UIUtils.compareVersions("0.1", 1, 2);
        assertEquals(1, ret);
    }

    @Test
    public void testLowerMajorVersionNumberTwo() {
        int ret = UIUtils.compareVersions("0.3", 1, 2);
        assertEquals(1, ret);
    }

    @Test
    public void testLowerMinorVersionNumber() {
        int ret = UIUtils.compareVersions("1.1", 1, 2);
        assertEquals(1, ret);
    }

    @Test
    public void testTheSameVersion() {
        int ret = UIUtils.compareVersions("1.1", 1, 1);
        assertEquals(0, ret);
    }

    @Test
    public void testHigherMinorVersion() {
        int ret = UIUtils.compareVersions("1.3", 1, 2);
        assertEquals(-1, ret);
    }

    @Test
    public void testHigherMajorVersionOne() {
        int ret = UIUtils.compareVersions("2.1", 1, 2);
        assertEquals(-1, ret);
    }

    @Test
    public void testHigherMajorVersionTwo() {
        int ret = UIUtils.compareVersions("2.2", 1, 2);
        assertEquals(-1, ret);
    }

    @Test
    public void testHigherMajorVersionThree() {
        int ret = UIUtils.compareVersions("2.3", 1, 2);
        assertEquals(-1, ret);
    }
}
