/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.controllers;

import com.wsojka.hammerui.enums.AppTheme;
import com.wsojka.hammerui.enums.autocomplete.HttpHeader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HeadersDialogController {

    private String name = "";

    private String value = "";

    private boolean submitted = false;

    @FXML
    private AnchorPane root;

    @FXML
    private ComboBox<String> headerName;

    @FXML
    private ComboBox<String> headerValue;

    @FXML
    private Button addButton;

    @FXML
    private Button cancelButton;

    private ObservableList<String> headerNames = FXCollections.observableArrayList();

    private SortedMap<String, SortedSet<String>> headerValues = new TreeMap<>();

    public void initialize() {
        headerNames.addAll(Stream.of(HttpHeader.values()).map(HttpHeader::getName).collect(Collectors.toList()));

        initializeComboboxField(headerName, null);

        initializeComboboxField(headerValue, headerName);

        headerName.setItems(headerNames);

        for (HttpHeader h : HttpHeader.values()) {
            SortedSet<String> linkedEntries = new TreeSet<>();
            linkedEntries.addAll(h.getValues());
            headerValues.put(h.getName(), linkedEntries);
        }
        headerName.requestFocus();
        headerName.setValue(name);
        headerValue.setValue(value);
    }

    @FXML
    public void addHeader(ActionEvent event) {
        name = headerName.getEditor().getText();
        value = headerValue.getEditor().getText();
        Stage stage = (Stage) addButton.getScene().getWindow();
        stage.close();
        submitted = true;
    }

    @FXML
    public void cancelDialog(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    String getHeaderName() {
        return name;
    }

    String getHeaderValue() {
        return value;
    }

    void setHeaderName(String name) {
        this.name = name;
        headerName.getEditor().setText(this.name);
    }

    void setHeaderValue(String value) {
        this.value = value;
        headerValue.getEditor().setText(this.value);
    }

    boolean wasSubmitted() {
        return submitted;
    }

    private void initializeComboboxField(ComboBox<String> field, ComboBox<String> linkedField) {
        field.setOnKeyReleased(event -> {
            KeyCombination shiftDel = new KeyCodeCombination(KeyCode.DELETE, KeyCombination.CONTROL_DOWN);
            KeyCombination shiftBack = new KeyCodeCombination(KeyCode.BACK_SPACE, KeyCombination.CONTROL_DOWN);

            if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN) {
                if (!field.isShowing())
                    field.show();
                event.consume();
                return;
            }
            if (!event.isShiftDown() && event.getCode() == KeyCode.HOME) {
                field.getEditor().positionCaret(0);
                event.consume();
                return;
            } else if (!event.isShiftDown() && event.getCode() == KeyCode.END) {
                field.getEditor().positionCaret(field.getEditor().getLength());
                event.consume();
                return;
            } else if (!event.isShiftDown() && event.getCode() == KeyCode.PAGE_DOWN ||
                    !event.isShiftDown() && event.getCode() == KeyCode.PAGE_UP ||
                    event.getCode() == KeyCode.ENTER) {
                event.consume();
                return;
            } else if (event.getCode() == KeyCode.ESCAPE ||
                    event.getCode() == KeyCode.TAB) {
                if (field.isShowing())
                    field.hide();
                event.consume();
                return;
            } else if (!event.isShiftDown() && event.getCode() == KeyCode.LEFT && field.isShowing()) {
                int pos = field.getEditor().getCaretPosition() - 1;
                field.getEditor().positionCaret(Math.max(0, pos));
                event.consume();
                return;
            } else if (!event.isShiftDown() && event.getCode() == KeyCode.RIGHT && field.isShowing()) {
                int pos = field.getEditor().getCaretPosition() + 1;
                field.getEditor().positionCaret(Math.min(pos, field.getEditor().getText().length()));
                event.consume();
                return;
            } else if (shiftDel.match(event) || shiftBack.match(event)) {
                if (field.getEditor().getText() == null || field.getEditor().getText().equals(""))
                    field.getSelectionModel().select(0);
            } else if (event.isControlDown() || event.isAltDown() || event.isShiftDown() ||
                    event.getCode() == KeyCode.SHIFT || event.getCode() == KeyCode.CONTROL || event.getCode() == KeyCode.ALT) {
                return;
            }

            /* filter out elements based on current editor text value */
            ObservableList<String> filtered = FXCollections.observableArrayList();
            if (linkedField != null) {
                Optional<Map.Entry<String, SortedSet<String>>> vals = headerValues.entrySet()
                        .stream()
                        .filter(e -> StringUtils.equalsIgnoreCase(linkedField.getEditor().getText(), e.getKey()))
                        .findFirst();
                if (vals.isPresent()) {
                    List<String> values = new ArrayList<>(vals.get().getValue());
                    filtered = FXCollections.observableArrayList(values
                            .stream()
                            .filter(e -> StringUtils.containsIgnoreCase(e, field.getEditor().getText()))
                            .collect(Collectors.toList()));
                }
            } else {
                filtered = FXCollections.observableList(headerNames
                        .stream()
                        .filter(e -> StringUtils.containsIgnoreCase(e, field.getEditor().getText()))
                        .collect(Collectors.toList()));
            }

            setFieldItems(field, filtered);

            if (filtered.size() > 0) {
                field.setVisibleRowCount(Math.min(10, filtered.size()));
                field.hide();
                field.show();
                if (StringUtils.isEmpty(field.getEditor().getText()))
                    field.getSelectionModel().clearSelection();
            } else {
                field.hide();
            }
        });

        ComboBoxListViewSkin<String> skin = new ComboBoxListViewSkin<>(field);
        skin.getPopupContent().addEventFilter(KeyEvent.ANY, event -> {
            if (!event.isShiftDown() && event.getCode() == KeyCode.HOME) {
                field.getEditor().positionCaret(0);
                event.consume();
            } else if (!event.isShiftDown() && event.getCode() == KeyCode.END) {
                field.getEditor().positionCaret(field.getEditor().getLength());
                event.consume();
            }
        });
        field.setSkin(skin);
        if (linkedField != null) {
            linkedField.valueProperty().addListener((observable, oldValue, newValue) -> {
                Optional<Map.Entry<String, SortedSet<String>>> vals = headerValues.entrySet()
                        .stream()
                        .filter(e -> StringUtils.equalsIgnoreCase(linkedField.getEditor().getText(), e.getKey()))
                        .findFirst();
                if (vals.isPresent()) {
                    List<String> values = new ArrayList<>(vals.get().getValue());
                    ObservableList<String> filtered = FXCollections.observableArrayList(values
                            .stream()
                            .filter(e -> StringUtils.containsIgnoreCase(e, field.getEditor().getText()))
                            .collect(Collectors.toList()));

                    setFieldItems(field, filtered);
                }
            });
        }
    }

    private void setFieldItems(ComboBox<String> field, ObservableList<String> filtered) {
        if (filtered.isEmpty() && field.isShowing()) {
            field.hide();
            return;
        }
        String text = "";
        if (field.getEditor().getText() != null)
            text = field.getEditor().getText();
        int caretPos = field.getEditor().getCaretPosition();
        field.setItems(filtered);
        field.getEditor().setText(text);
        field.getEditor().positionCaret(caretPos);
    }

    public void setTheme(AppTheme theme) {
        if (theme == AppTheme.Dark) {
            root.getStyleClass().add(AppTheme.Dark.getStyleName());
        } else {
            root.getStyleClass().remove(AppTheme.Dark.getStyleName());
        }
    }
}
