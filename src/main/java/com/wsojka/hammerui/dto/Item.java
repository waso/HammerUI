/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.dto;

import java.util.UUID;

public abstract class Item {

    private String id;
    private String name;
    private String parent;
    private String previous;

    public Item() {
        id = UUID.randomUUID().toString();
    }

    public Item(String name) {
        this.name = name;
        id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    /**
     * Checks if given node should be first child on the list
     * on given level. First child "previous" field is always
     * empty
     * @return
     */
    public boolean isFirstChild() {
        return this.previous.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        if (id != null ? !id.equals(item.id) : item.id != null) return false;
        if (name != null ? !name.equals(item.name) : item.name != null) return false;
        if (parent != null ? !parent.equals(item.parent) : item.parent != null) return false;
        return previous != null ? previous.equals(item.previous) : item.previous == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        result = 31 * result + (previous != null ? previous.hashCode() : 0);
        return result;
    }
}
