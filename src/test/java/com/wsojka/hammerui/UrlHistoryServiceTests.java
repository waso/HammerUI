/*
 * Copyright (C) 2020 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui;

import com.wsojka.hammerui.service.UrlHistoryService;
import com.wsojka.hammerui.service.UrlHistoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UrlHistoryServiceTests {

    @Test
    public void happyPathTest() {
        UrlHistoryService service = new UrlHistoryServiceImpl();
        service.addUrlEntry("1", "aaa");
        service.addUrlEntry("1", "bbb");
        service.addUrlEntry("1", "ccc");
        service.addUrlEntry("2", "ddd");
        assertEquals("bbb", service.getPreviousUrl("1"));
        assertEquals("aaa", service.getPreviousUrl("1"));
        assertNull(service.getPreviousUrl("1"));
        assertEquals("bbb", service.getNextUrl("1"));
        assertEquals("ccc", service.getNextUrl("1"));
        assertNull(service.getNextUrl("1"));
    }

    @Test
    public void undoRedoTest() {
        UrlHistoryService service = new UrlHistoryServiceImpl();
        service.addUrlEntry("1", "aaa");
        service.addUrlEntry("1", "bbb");
        service.addUrlEntry("1", "ccc");
        service.addUrlEntry("1", "ddd");
        service.addUrlEntry("1", "eee");
        assertEquals("ddd", service.getPreviousUrl("1"));
        assertEquals("ccc", service.getPreviousUrl("1"));
        service.addUrlEntry("1", "xxx");
        service.addUrlEntry("1", "yyy");
        service.addUrlEntry("1", "zzz");
        assertEquals("yyy", service.getPreviousUrl("1"));
        assertEquals("xxx", service.getPreviousUrl("1"));
        assertEquals("ccc", service.getPreviousUrl("1"));
        assertEquals("bbb", service.getPreviousUrl("1"));
        assertEquals("aaa", service.getPreviousUrl("1"));
        assertNull(service.getPreviousUrl("1"));
        assertEquals("bbb", service.getNextUrl("1"));
        assertEquals("ccc", service.getNextUrl("1"));
        assertEquals("xxx", service.getNextUrl("1"));
        assertEquals("yyy", service.getNextUrl("1"));
        assertEquals("zzz", service.getNextUrl("1"));
        assertNull(service.getNextUrl("1"));
    }

    @Test
    public void nullParamsTest() {
        UrlHistoryServiceImpl service = new UrlHistoryServiceImpl();
        service.addUrlEntry(null, null);
        assertTrue(service.getEntries().isEmpty());
        assertTrue(service.getPointers().isEmpty());
    }

    @Test
    public void nullUuidTest() {
        UrlHistoryServiceImpl service = new UrlHistoryServiceImpl();
        service.addUrlEntry("1", null);
        assertTrue(service.getEntries().isEmpty());
        assertTrue(service.getPointers().isEmpty());
    }

    @Test
    public void nullUrlTest() {
        UrlHistoryServiceImpl service = new UrlHistoryServiceImpl();
        service.addUrlEntry(null, "aaa");
        assertTrue(service.getEntries().isEmpty());
        assertTrue(service.getPointers().isEmpty());
        assertNull(service.getNextUrl(null));
        assertNull(service.getPreviousUrl(null));
    }

    @Test
    public void ignoreDuplicateEntryTest() {
        UrlHistoryService service = new UrlHistoryServiceImpl();
        service.addUrlEntry("1", "aaa");
        service.addUrlEntry("1", "aaa");
        service.addUrlEntry("1", "bbb");
        service.addUrlEntry("1", "bbb");
        service.addUrlEntry("1", "ccc");
        assertEquals("bbb", service.getPreviousUrl("1"));
        assertEquals("aaa", service.getPreviousUrl("1"));
    }
}
