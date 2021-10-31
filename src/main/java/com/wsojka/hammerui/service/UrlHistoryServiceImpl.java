/*
 * Copyright (C) 2020 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wsojka.hammerui.utils.ConsoleLogger;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class UrlHistoryServiceImpl implements UrlHistoryService {

    private Map<String, List<String>> entries;

    private Map<String, Integer> pointers;

    public UrlHistoryServiceImpl() {
        entries = Maps.newHashMap();
        pointers = Maps.newHashMap();
    }

    public Map<String, List<String>> getEntries() {
        return entries;
    }

    public Map<String, Integer> getPointers() {
        return pointers;
    }

    private String getLastEntry(String uuid) {
        if (uuid == null)
            return null;
        if (!entries.containsKey(uuid))
            return null;
        if (!pointers.containsKey(uuid))
            return null;
        List<String> uuidEntries = entries.get(uuid);
        int ptr = pointers.get(uuid);
        if (ptr >= uuidEntries.size())
            return null;
        return uuidEntries.get(ptr);

    }
    @Override
    public void addUrlEntry(String uuid, String url) {
        if (uuid == null || url == null || url.trim().length() == 0)
            return;
        if (StringUtils.equals(url, getLastEntry(uuid)))
            return;
        int ptr;
        if (pointers.containsKey(uuid)) {
            ptr = pointers.get(uuid);
            ptr++;
        } else
            ptr = 0;
        pointers.put(uuid, ptr);

        List<String> uuidEntries = entries.getOrDefault(uuid, Lists.newArrayList());

        if (ptr < uuidEntries.size()) {
            uuidEntries.subList(ptr, uuidEntries.size()).clear();
        }
        uuidEntries.add(url);
        entries.put(uuid, uuidEntries);
    }

    @Override
    public String getPreviousUrl(String uuid) {
        if (uuid == null)
            return null;
        if (!entries.containsKey(uuid))
            return null;
        List<String> uuidEntries = entries.get(uuid);
        if (uuidEntries.isEmpty())
            return null;
        int ptr;
        if (pointers.containsKey(uuid)) {
            ptr = pointers.get(uuid);
        } else
            return null;
        ptr--;
        if (ptr < 0 || ptr > uuidEntries.size())
            return null;
        pointers.put(uuid, ptr);
        return uuidEntries.get(ptr);
    }

    @Override
    public String getNextUrl(String uuid) {
        if (uuid == null)
            return null;
        if (!entries.containsKey(uuid))
            return null;
        List<String> uuidEntries = entries.get(uuid);
        if (uuidEntries.isEmpty())
            return null;
        if (!pointers.containsKey(uuid))
            return null;
        int ptr = pointers.get(uuid);
        ptr++;
        if (ptr >= uuidEntries.size())
            return null;
        pointers.put(uuid, ptr);
        return uuidEntries.get(ptr);
    }
}
