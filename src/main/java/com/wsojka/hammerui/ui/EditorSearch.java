/*
 * Copyright (C) 2020 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.ui;

import com.google.common.collect.Lists;
import com.wsojka.hammerui.dto.editor.TextSelection;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditorSearch {

    List<Integer> occurrences = Lists.newArrayList();

    List<Integer> newLines = Lists.newArrayList();

    public TextSelection getPreviousPos(int cursorLine, int cursorColumn, int searchTextSize) {
        TextSelection selection = new TextSelection();
        if (cursorLine < 0 || cursorColumn < 0)
            return selection;
        int previousOccurrence = -1;
        int currentPosChar = findCurrentPos(cursorLine, cursorColumn);


        /* find previous occurrence starting from current cursor position */
        for (int i = 0; i < occurrences.size(); i++) {
            if (occurrences.get(i) >= currentPosChar) {
                break;
            } else {
                selection.setIndex(i + 1);
                previousOccurrence = occurrences.get(i);
            }
        }

        /* translate next occurence index into row/column coordinates */
        if (previousOccurrence == -1) {
            selection.setFound(false);
        } else {
            selection.setFound(true);
            /* handle occurrence in first line */
            if (newLines.isEmpty()) {
                selection.setStartRow(0);
                selection.setEndRow(0);
                selection.setStartCol(previousOccurrence);
                selection.setEndCol(previousOccurrence + searchTextSize);
                return selection;
            }
            /* handle occurrence in last line */
            if (newLines.get(newLines.size() - 1) < previousOccurrence) {
                selection.setStartRow(newLines.size());
                selection.setEndRow(newLines.size());
                int startCol = previousOccurrence - newLines.get(newLines.size() - 1) - 1;
                selection.setStartCol(startCol);
                selection.setEndCol(startCol + searchTextSize);
                return selection;
            }
            for (int i = 0; i < newLines.size(); i++) {
                if (newLines.get(i) > previousOccurrence) {
                    int startCol;
                    selection.setStartRow(i);
                    selection.setEndRow(i);
                    if (i == 0)
                        startCol = previousOccurrence;
                    else
                        startCol = previousOccurrence - newLines.get(i - 1) - 1;
                    int endCol = startCol + searchTextSize;
                    selection.setStartCol(startCol);
                    selection.setEndCol(endCol);
                    break;
                }
            }
        }
        return selection;
    }

    public TextSelection getNextPos(int cursorLine, int cursorColumn, int searchTextSize) {
        TextSelection selection = new TextSelection();
        if (cursorLine < 0 || cursorColumn < 0)
            return selection;
        int nextOccurrence = -1;
        int currentPosChar = findCurrentPos(cursorLine, cursorColumn);

        /* find next occurrence starting from current cursor position */
        for (int i = 0; i < occurrences.size(); i++) {
            if (occurrences.get(i) > currentPosChar) {
                nextOccurrence = occurrences.get(i);
                selection.setIndex(i + 1);
                break;
            }
        }

        /* translate next occurence index into row/column coordinates */
        if (nextOccurrence == -1) {
            selection.setFound(false);
        } else {
            selection.setFound(true);
            /* handle occurrence in first line */
            if (newLines.isEmpty()) {
                selection.setStartRow(0);
                selection.setEndRow(0);
                selection.setStartCol(nextOccurrence);
                selection.setEndCol(nextOccurrence + searchTextSize);
                return selection;
            }
            /* handle occurrence in last line */
            if (newLines.get(newLines.size() - 1) < nextOccurrence) {
                selection.setStartRow(newLines.size());
                selection.setEndRow(newLines.size());
                int startCol = nextOccurrence - newLines.get(newLines.size() - 1) - 1;
                selection.setStartCol(startCol);
                selection.setEndCol(startCol + searchTextSize);
                return selection;
            }
            for (int i = 0; i < newLines.size(); i++) {
                if (newLines.get(i) > nextOccurrence) {
                    int startCol;
                    selection.setStartRow(i);
                    selection.setEndRow(i);
                    if (i == 0)
                        startCol = nextOccurrence;
                    else
                        startCol = nextOccurrence - newLines.get(i - 1) - 1;
                    int endCol = startCol + searchTextSize;
                    selection.setStartCol(startCol);
                    selection.setEndCol(endCol);
                    break;
                }
            }
        }
        return selection;
    }

    public void calculateNewLines(String response) {
        if (response == null || response.length() == 0)
            return;
        newLines.clear();
        Matcher m = Pattern.compile("(?=(\n))").matcher(response);
        while (m.find()) {
            newLines.add(m.start());
        }
    }

    public int generatePositions(String response, String searchText) {
        if (searchText == null || searchText.length() == 0)
            return 0;
        occurrences.clear();
        Matcher m = Pattern.compile("(?=(" + Pattern.quote(searchText) + "))", Pattern.CASE_INSENSITIVE).matcher(response);
        while (m.find()) {
            occurrences.add(m.start());
        }
        return occurrences.size();
    }

    private int findCurrentPos(int cursorLine, int cursorColumn) {
        int currentPosChar = -1;
        if (newLines.isEmpty() || cursorLine == 0) {
            currentPosChar = cursorColumn;
        } else {
            currentPosChar = newLines.get(cursorLine - 1) + cursorColumn;
            currentPosChar++;
        }
        return currentPosChar;
    }
}
