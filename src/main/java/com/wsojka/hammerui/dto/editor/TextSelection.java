/*
 * Copyright (C) 2020 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.dto.editor;

public class TextSelection {

    private boolean found = false;

    private int startRow;

    private int startCol;

    private int endRow;

    private int endCol;

    private int index;

    public int getStartRow() {
        return startRow;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    public int getStartCol() {
        return startCol;
    }

    public void setStartCol(int startCol) {
        this.startCol = startCol;
    }

    public int getEndRow() {
        return endRow;
    }

    public void setEndRow(int endRow) {
        this.endRow = endRow;
    }

    public int getEndCol() {
        return endCol;
    }

    public void setEndCol(int endCol) {
        this.endCol = endCol;
    }

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return "TextSelection{" +
                "found=" + found +
                ", startRow=" + startRow +
                ", startCol=" + startCol +
                ", endRow=" + endRow +
                ", endCol=" + endCol +
                '}';
    }
}
