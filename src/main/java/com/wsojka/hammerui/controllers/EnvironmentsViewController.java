/*
 * Copyright (C) 2018 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.controllers;

import com.wsojka.hammerui.dto.Environment;
import com.wsojka.hammerui.dto.Parameter;
import com.wsojka.hammerui.enums.AppTheme;
import com.wsojka.hammerui.persintence.PreferencesWrapper;
import com.wsojka.hammerui.persintence.PreferencesWrapperFactory;
import com.wsojka.hammerui.utils.ConsoleLogger;
import com.wsojka.hammerui.utils.UIUtils;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

public class EnvironmentsViewController {

    private ObservableList<Environment> environments = FXCollections.observableArrayList();

    private ObservableList<Environment> savedEnvironments = FXCollections.observableArrayList();

    private static final PreferencesWrapper preferences = PreferencesWrapperFactory.getInstance();

    private ResourceBundle bundle = ResourceBundle.getBundle("bundles.MainView", preferences.getLocale());

    private Environment selectedEnvironment;

    private boolean submitted = false;

    private BooleanProperty changed = new SimpleBooleanProperty(false);

    private enum UserActionMode {ADD, EDIT, CLONE}

    @FXML
    private Button cancelButton;

    @FXML
    public Button submitButton;

    @FXML
    public Button applyButton;

    @FXML
    private ListView<Environment> environmentsList;

    @FXML
    private TableView<Parameter> paramsTable;

    @FXML
    private TextField environmentName;

    @FXML
    private Button addParamButton;

    @FXML
    private Button deleteParamButton;

    @FXML
    private Button cloneParamButton;

    @FXML
    private AnchorPane root;

    public void initialize() {
        Platform.runLater(() -> {
            changed.addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    ConsoleLogger.info("changed = true");
                    applyButton.setDisable(false);
                    submitted = false;
                } else {
                    ConsoleLogger.info("changed = false");
                    applyButton.setDisable(true);
                }
            });
            environmentName.textProperty().addListener((observable, oldValue, newValue) -> {
                if (selectedEnvironment == null)
                    return;
                if (newValue == null || selectedEnvironment.getName() == null ||
                        newValue.equals(selectedEnvironment.getName()))
                    return;
                ConsoleLogger.info("changing environment name from: {} to: {}", oldValue, newValue);
                selectedEnvironment.setName(newValue);
                environmentsList.refresh();
                changed.set(true);
            });
            environmentsList.setItems(environments);
            environmentsList
                    .getSelectionModel()
                    .selectedItemProperty()
                    .addListener((observable, oldValue, newValue) -> {
                        ConsoleLogger.info("selected environment {}", newValue);
                        selectedEnvironment = newValue;
                        loadEnvironmentDetails();
                    });
            paramsTableConfiguration();
            environments.addListener((ListChangeListener<Environment>) (c -> {
                changed.set(true);
                ConsoleLogger.info("environments list changed");
            }));
            /* open EnvironmentsView window and load details of the environment selected in the main window dropdown */
            if (selectedEnvironment != null) {
                environmentsList.getSelectionModel().select(selectedEnvironment);
                loadEnvironmentDetails();
            } else {
                environmentName.setDisable(true);
                paramsTable.setDisable(true);
            }
            environmentsList.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Environment item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(item.getName());
                    }
                }
            });
        });
    }

    private void paramsTableConfiguration() {
        paramsTable.setEditable(false);
        paramsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        paramsTable.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            Node source = event.getPickResult().getIntersectedNode();
            while (source != null && !(source instanceof TableRow)) {
                source = source.getParent();
            }
            if (source == null || ((TableRow) source).isEmpty()) {
                paramsTable.getSelectionModel().clearSelection();
            }
        });
        paramsTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER)
                editParameterAction(null);
            else if (event.getCode() == KeyCode.DELETE)
                editParameterAction(null);
        });

        TableColumn<Parameter, String> paramNameCol = new TableColumn<>("name");
        paramNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        paramNameCol.setEditable(false);
        paramNameCol.setCellFactory(tc -> {
            TableCell<Parameter, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };
            cell.setOnMouseClicked(e -> {
                ConsoleLogger.debug("clicked..." + e.getClickCount() + " times on index " + paramsTable.getSelectionModel().getSelectedIndex());
                if (e.getClickCount() >= 2) {
                    editParameterAction(null);
                }
            });
            return cell;
        });

        TableColumn<Parameter, String> paramValueCol = new TableColumn<>("value");
        paramValueCol.setCellValueFactory(
                new PropertyValueFactory<>("value")
        );
        paramValueCol.setEditable(false);
        paramValueCol.setCellFactory(tc -> {
            TableCell<Parameter, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };
            cell.setOnMouseClicked(e -> {
                ConsoleLogger.debug("clicked..." + e.getClickCount() + " times on index " + paramsTable.getSelectionModel().getSelectedIndex());
                if (e.getClickCount() >= 2) {
                    editParameterAction(null);
                }
            });
            return cell;
        });

        paramsTable.getColumns().clear();
        paramsTable.getColumns().addAll(Arrays.asList(paramNameCol, paramValueCol));
        paramsTable.setFixedCellSize(25);
        paramsTable.setPlaceholder(new Label(""));
        Pane header = (Pane) paramsTable.lookup("TableHeaderRow");
        if (header.isVisible()) {
            header.setMaxHeight(0);
            header.setMinHeight(0);
            header.setPrefHeight(0);
            header.setVisible(false);
        }
    }

    private void loadEnvironmentDetails() {
        if (selectedEnvironment == null)
            return;
        paramsTable.setDisable(false);
        paramsTable.setItems(selectedEnvironment.getParameters());
        environmentName.setDisable(false);
        environmentName.setText(selectedEnvironment.getName());
        selectedEnvironment.getParameters().addListener((ListChangeListener<Parameter>) (c -> {
            changed.set(true);
            ConsoleLogger.info("parameters list changed");
        }));
        addParamButton.setDisable(false);
        deleteParamButton.setDisable(false);
        cloneParamButton.setDisable(false);
    }

    private void clearEnvironmentDetails() {
        if (!environments.isEmpty())
            return;
        paramsTable.getItems().clear();
        paramsTable.setDisable(true);
        environmentName.setText("");
        environmentName.setDisable(true);
        addParamButton.setDisable(true);
        deleteParamButton.setDisable(true);
        cloneParamButton.setDisable(true);
    }

    @FXML
    public void addEnvironment(ActionEvent event) {
        ConsoleLogger.info("adding new environment");
        String name = UIUtils.getValueFromUser("name", bundle.getString("add_new_environment_title"));
        if (name == null || name.trim().isEmpty())
            return;
        ConsoleLogger.info("adding new environment {}", name);
        Environment env = new Environment();
        env.setName(name);
        env.setUuid(UUID.randomUUID().toString());
        ObservableList<Parameter> params = FXCollections.observableArrayList();
        env.setParameters(params);
        environments.add(env);
        environmentsList.setItems(environments);
        /* select newly added environment */
        environmentsList.getSelectionModel().select(env);
    }

    @FXML
    public void deleteEnvironment(ActionEvent event) {
        if (environmentsList.getItems().size() == 0 || environmentsList.getSelectionModel().getSelectedIndex() < 0)
            return;
        Environment env = environmentsList.getSelectionModel().getSelectedItem();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete");
        alert.setHeaderText(null);
        alert.setContentText(String.format("Delete %s ?", env.getName()));
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/css/name_value_dialog.css").toExternalForm());
        if (preferences.getApplicationSettings().getAppTheme() == AppTheme.Dark) {
            dialogPane.getStyleClass().add(AppTheme.Dark.getStyleName());
        } else {
            dialogPane.getStyleClass().remove(AppTheme.Dark.getStyleName());
        }

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            ConsoleLogger.info("deleting environment {}", env.getName());
            environments.remove(env);
        }
        if (environments.isEmpty())
            clearEnvironmentDetails();
    }

    @FXML
    public void cloneEnvironment(ActionEvent event) {
        if (environmentsList.getItems().isEmpty() || environmentsList.getSelectionModel().getSelectedIndex() < 0)
            return;
        ConsoleLogger.info("cloning environment " + selectedEnvironment.getName());
        Environment env = new Environment();
        env.setName(selectedEnvironment.getName() + " copy");
        env.setUuid(UUID.randomUUID().toString());
        ObservableList<Parameter> params = FXCollections.observableArrayList();
        selectedEnvironment.getParameters().forEach(p -> params.add(new Parameter(p.getName(), p.getValue())));
        env.setParameters(params);
        environments.add(env);
        environmentsList.setItems(environments);
        /* select newly added environment */
        environmentsList.getSelectionModel().select(env);
    }

    @FXML
    public void cancelDialog(ActionEvent event) {
        ConsoleLogger.info("canceling environments dialog");
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
        submitted = false;
    }

    @FXML
    public void applyDialog(ActionEvent event) {
        ConsoleLogger.info("applying changes of environments list");
        savedEnvironments.clear();
        savedEnvironments.addAll(environments);
        submitted = true;
        changed.set(false);
    }

    @FXML
    public void submitDialog(ActionEvent event) {
        Stage stage = (Stage) submitButton.getScene().getWindow();
        stage.close();
        submitted = true;
    }

    @FXML
    public void addParameterAction(ActionEvent event) {
        editParam(event, UserActionMode.ADD, "Add Parameter");
    }

    @FXML
    public void cloneParameterAction(ActionEvent event) {
        editParam(event, UserActionMode.CLONE, "Add Parameter");
    }

    @FXML
    public void editParameterAction(ActionEvent event) {
        editParam(event, UserActionMode.EDIT, "Edit Parameter");
    }

    @FXML
    private void deleteParameterAction(ActionEvent event) {
        if (paramsTable.getItems().size() == 0 || paramsTable.getSelectionModel().getSelectedIndex() < 0)
            return;
        Parameter p = paramsTable.getSelectionModel().getSelectedItem();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete");
        alert.setHeaderText(null);
        alert.setContentText(String.format("Delete parameter %s ?", p.getName()));
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/css/name_value_dialog.css").toExternalForm());
        if (preferences.getApplicationSettings().getAppTheme() == AppTheme.Dark) {
            dialogPane.getStyleClass().add(AppTheme.Dark.getStyleName());
        } else {
            dialogPane.getStyleClass().remove(AppTheme.Dark.getStyleName());
        }

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            ConsoleLogger.info("deleting parameter {}", p.getName());
            paramsTable.getItems().remove(p);
        }
    }

    public boolean wasSubmitted() {
        return submitted;
    }

    public ObservableList<Environment> getEnvironments() {
        return environments;
    }

    public void setEnvironments(ObservableList<Environment> environments) {
        this.environments = environments;
        environmentsList.setItems(environments);
    }

    public void setSavedEnvironments(ObservableList<Environment> savedEnvironments) {
        this.savedEnvironments = savedEnvironments;
    }

    private void editParam(ActionEvent event, UserActionMode mode, String title) {
        Parameter p = new Parameter("", "");
        if (mode == UserActionMode.EDIT) {
            if (paramsTable.getItems().size() == 0 || paramsTable.getSelectionModel().getSelectedIndex() < 0)
                return;
            p = paramsTable.getSelectionModel().getSelectedItem();
        } else if (mode == UserActionMode.CLONE) {
            if (paramsTable.getItems().size() == 0 || paramsTable.getSelectionModel().getSelectedIndex() < 0)
                return;
            p.setName(paramsTable.getSelectionModel().getSelectedItem().getName());
            p.setValue(paramsTable.getSelectionModel().getSelectedItem().getValue());
            selectedEnvironment.getParameters().add(p);
            paramsTable.setItems(selectedEnvironment.getParameters());
            paramsTable.refresh();
            return;
        }
        Stage stage = new Stage();
        Parent root;
        FXMLLoader loader = new FXMLLoader(NameValueDialogController.class.getResource("/fxml/NameValueDialog.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("problem with NameValueDialog", e);
        }
        NameValueDialogController controller = loader.getController();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        controller.setNameText(p.getName());
        controller.setValueText(p.getValue());
        controller.setTheme(preferences.getApplicationSettings().getAppTheme());
        stage.addEventHandler(KeyEvent.KEY_RELEASED, (KeyEvent ev) -> {
            if (KeyCode.ESCAPE == ev.getCode())
                stage.close();
        });
        stage.showAndWait();
        if (controller.wasSubmitted() && controller.getNameText() != null && !controller.getNameText().trim().isEmpty()) {
            if (mode == UserActionMode.ADD) {
                ConsoleLogger.debug("adding new parameter: {} = {}", controller.getNameText(), controller.getValueText());
                p = new Parameter(controller.getNameText(), controller.getValueText());
                selectedEnvironment.getParameters().add(p);
                paramsTable.setItems(selectedEnvironment.getParameters());
                paramsTable.refresh();
            } else if (mode == UserActionMode.EDIT) {
                ConsoleLogger.debug("editing parameter: {} = {}", controller.getNameText(), controller.getValueText());
                p.setName(controller.getNameText());
                p.setValue(controller.getValueText());
                paramsTable.refresh();
            }
            changed.set(true);
        }
    }

    public void setDefaultEnvironment(Environment defaultSelect) {
        if (defaultSelect == null)
            return;
        selectedEnvironment = defaultSelect;
    }

    public Environment getSelectedEnvironment() {
        return selectedEnvironment;
    }

    public void setTheme(AppTheme theme) {
        if (theme == AppTheme.Dark) {
            root.getStyleClass().add(AppTheme.Dark.getStyleName());
        } else {
            root.getStyleClass().remove(AppTheme.Dark.getStyleName());
        }
    }
}
