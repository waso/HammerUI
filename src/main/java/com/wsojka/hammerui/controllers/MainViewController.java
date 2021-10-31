/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.controllers;

import com.google.common.collect.Lists;
import com.wsojka.hammerui.dto.*;
import com.wsojka.hammerui.enums.AppTheme;
import com.wsojka.hammerui.events.*;
import com.wsojka.hammerui.events.shortcuts.CloseActiveTabEvent;
import com.wsojka.hammerui.events.shortcuts.SaveProjectEvent;
import com.wsojka.hammerui.persintence.*;
import com.wsojka.hammerui.service.AutocompleteService;
import com.wsojka.hammerui.service.AutocompleteServiceFactory;
import com.wsojka.hammerui.utils.ConsoleLogger;
import com.wsojka.hammerui.utils.UIUtils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class MainViewController {

    private ProjectFileWrapper project = ProjectFileWrapperFactory.getInstance();
    private PreferencesWrapper preferences = PreferencesWrapperFactory.getInstance();
    private ProjectChangeListener changeListener = ProjectChangeListenerFactory.getInstance();
    private AutocompleteService autocompleteService = AutocompleteServiceFactory.getInstance();
    private boolean treeViewVisible = true;
    private Node paneComponent;
    private double[] paneSplitterPositions = new double[2];
    private TreeItem<Item> rootItem;
    private Environment defaultEnvironment = null;
    private Proxy defaultProxy = null;
    private ApplicationSettings applicationSettings;
    private ResourceBundle bundle;
    private Orientation splitPaneOrientation = Orientation.VERTICAL;

    private static boolean optionsPaneDisabled = false;
    private static double[] lastDividerPositions;
    static {
        lastDividerPositions = new double[]{0.3};
    }

    @FXML
    private TreeView<Item> projectTree;

    @FXML
    private TabPane tabPane;

    @FXML
    private AnchorPane root;

    @FXML
    private Menu openRecent;

    @FXML
    private SplitPane hiddenSplitter;

    @FXML
    private MenuButton editEnvironments;

    @FXML
    private MenuButton editProxies;

    @FXML
    private MenuItem hideProjectTreeMenuItem;

    @FXML
    private MenuItem switchOrientationMenuItem;

    @FXML
    private MenuItem hideRequestDetailsMenuItem;

    @FXML
    private MenuBar menuBar;

    public TreeItem<Item> getRootItem() {
        return rootItem;
    }

    private ListChangeListener<? super Item> closeTabAfterDeletingItemListener = (c) -> {
        while (c.next()) {
            if (c.getRemovedSize() > 0) {
                ConsoleLogger.debug("{} item(s) removed", c.getRemovedSize());
                for (Item item : c.getRemoved()) {
                    if (tabPane.getTabs().removeIf(tab -> tab.getId().equals(item.getId()))) {
                        ConsoleLogger.info("closed tab for removed item \"{}\" ({})", item.getName(), item.getId());
                        List<String> openRequests = tabPane.getTabs()
                                .stream()
                                .map(Tab::getId)
                                .filter(e -> !e.equals(item.getId()))
                                .collect(Collectors.toList());
                        preferences.setOpenRequests(project.getProjectUuid(), openRequests);
                    }
                }
            }
            if (c.getAddedSize() > 0) {
                ConsoleLogger.debug("{} item(s) added", c.getAddedSize());
            }
            projectTree.refresh();
        }
    };

    private ChangeListener<? super Tab> focusTreeItemListener = (observable, oldValue, newValue) -> {
        if (observable.getValue() == null) { /* this happens when last tab is being closed */
            preferences.setSelectedRequest(project.getProjectUuid(), "");
            return;
        }
        TreeItem<Item> treeItem = UIUtils.getTreeItemById(projectTree.getRoot(), observable.getValue().getId());
        if (treeItem != null) {
            projectTree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            projectTree.getSelectionModel().select(treeItem);
            preferences.setSelectedRequest(project.getProjectUuid(), treeItem.getValue().getId());
        } else {
            ConsoleLogger.info("Item {} not found in the project tree", observable.getValue().getId());
        }
    };

    private ChangeListener<? super Boolean> openEditEnvironmentsListener = (observableValue, aBoolean, t1) -> {
        if (project.getEnvironments().size() == 0 && editEnvironments.isShowing()) {
            editEnvironments.hide();
            // show edit environments window
            openEnvironmentsEditor();
        }
    };

    private ChangeListener<? super Boolean> openEditProxiesListener = (observableValue, aBoolean, t1) -> {
        if (project.getProxies().size() == 0 && editProxies.isShowing()) {
            editProxies.hide();
            // show edit environments window
            openProxyEditor();
        }
    };

    private boolean noneEnvironmentSelected = false;

    private boolean noneProxySelected = false;

    public void initialize() {
        bundle = ResourceBundle.getBundle("bundles.MainView", preferences.getLocale());
        ConsoleLogger.debug("initializing MainView");
        Folder rootFolder = new Folder("root");
        rootFolder.setId(""); /* this must be empty or tree structure will be broken */
        rootItem = new TreeItem<>(rootFolder);
        projectTree.setRoot(rootItem);
        projectTree.setShowRoot(false);
        rootItem.setExpanded(true);
        projectTree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        projectTree.setCellFactory((TreeView<Item> p) -> new ProjectTreeCellImpl(project.getTreeItems(), root));
        projectTree.setStyle("-fx-font-size: 12;-fx-font-family: \"Verdana\";");
        openRecent.getItems().clear();
        List<Path> recentProjects = preferences.getLastOpenProjects();
        recentProjects.forEach(this::addRecentProjectEntry);

        /* re-create project tree if project file was loaded */
        if (project.isSaved()) {
            recreateProjectTree(projectTree.getRoot());
        }

        /* add listener that is closing tab for tree item that user is deleting */
        project.getTreeItems().addListener(closeTabAfterDeletingItemListener);

        /* add all URLs to the global autocomplete list */
        project.getTreeItems().forEach(item -> {
            if (item instanceof Request) {
                Request r = (Request) item;
                UrlEntry urlEntry = new UrlEntry(r.getUrl(), r.getHttpMethod());
                autocompleteService.addEntry(urlEntry);
            }
        });

        /* focus tree item based on currently selected tab */
        tabPane.getSelectionModel().selectedItemProperty().addListener(focusTreeItemListener);

        /* ser reorder policy so user can move tabs with mouse */
        tabPane.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
        tabPane.getTabs().addListener((ListChangeListener)c -> {
            if (c != null && StringUtils.equalsIgnoreCase(c.getClass().getSimpleName(), "SimplePermutationChange")) {
                List<String> openRequests = tabPane.getTabs()
                        .stream()
                        .map(Tab::getId)
                        .collect(Collectors.toList());
                preferences.setOpenRequests(project.getProjectUuid(), openRequests);
            }
        });

        noneEnvironmentSelected = false;
        noneProxySelected = false;

        /* setup environments editor */
        defaultEnvironment = findEnvironmentByUuid(project.getDefaultEnvironmentUuid());
        editEnvironments.showingProperty().addListener(openEditEnvironmentsListener);
        rebuildEnvironmentsList();

        /* setup proxy editor */
        defaultProxy = findProxyByUuid(project.getDefaultProxyUuid());
        editProxies.showingProperty().addListener(openEditProxiesListener);
        rebuildProxyList();

        /* event handler for renaming tab after request has been renamed by the user */
        EventHandler<ItemRenamedEvent> iItemRenamedEventHandler = event -> tabPane.getTabs().forEach(tab -> {
            if (tab.getId().equals(event.getItemUuid())) {
                ConsoleLogger.info("received ItemRenamedEvent for uuid {} with new name \"{}\"", event.getItemUuid(), event.getNewName());
                tab.setText(event.getNewName());
            }
        });
        root.addEventHandler(ItemRenamedEvent.ITEM_RENAMED, iItemRenamedEventHandler);

        /* handle Ctrl+S (save project) key shortcut */
        EventHandler<SaveProjectEvent> saveProjectEventHandler = event -> {
            UIUtils.updateItemsInternalIds(rootItem);
            project.saveProject();
        };
        root.addEventHandler(SaveProjectEvent.SAVE_PROJECT_EVENT_TYPE, saveProjectEventHandler);

        /* handle Ctrl+W (close active tab) key shortcut */
        EventHandler<CloseActiveTabEvent> closeActiveTabEventHandler = event -> {
            closeCurrentTab(null);
        };
        root.addEventHandler(CloseActiveTabEvent.CLOSE_ACTIVE_TAB_EVENT_EVENT_TYPE, closeActiveTabEventHandler);

        Platform.runLater(() -> {
            String selectedItem = preferences.getSelectedRequest(project.getProjectUuid());
            List<String> requestsToOpen = preferences.getOpenRequests(project.getProjectUuid());
            for (String request : requestsToOpen) {
                Optional<Item> req = project.getTreeItems().parallelStream().filter(p -> p.getId().equals(request)).findAny();
                if (req.isPresent()) {
                    try {
                        loadNewTab((Request) req.get(), false);
                    } catch (IOException e) {
                        ConsoleLogger.error("problem with opening request ID: " + request);
                    }
                }
            }
            Optional<Tab> tabToSelect = tabPane.getTabs().stream().filter(tab -> tab.getId().equals(selectedItem)).findFirst();
            tabToSelect.ifPresent(tab -> tabPane.getSelectionModel().select(tab));
        });

        project.updateWindowTitle();

        /* load application settings */
        applicationSettings = preferences.getApplicationSettings();
        autocompleteService.setTabPane(tabPane);

        /* load UI style */
        root.getStyleClass().remove(AppTheme.Dark.getStyleName());
        menuBar.getStyleClass().remove(AppTheme.Dark.getStyleName());
        if (applicationSettings.getAppTheme() == AppTheme.Dark) {
            root.getStyleClass().add(AppTheme.Dark.getStyleName());
            menuBar.getStyleClass().add(AppTheme.Dark.getStyleName());
        }
    }

    private void rebuildEnvironmentsList() {
        editEnvironments.getItems().clear();
        if (project.getEnvironments().isEmpty()) {
            editEnvironments.setText(bundle.getString("edit_environments_label"));
            return;
        }

        MenuItem editEnvsAction = new MenuItem(bundle.getString("edit_environments_label"));
        editEnvsAction.setOnAction(actionEvent -> openEnvironmentsEditor());

        MenuItem noSelection = new MenuItem("none");
        noSelection.setOnAction(actionEvent -> {
            ConsoleLogger.info("none environment selected");
            defaultEnvironment = null;
            noneEnvironmentSelected = true;
            editEnvironments.setText("none");
            project.setDefaultEnvironmentUuid(null);
        });

        editEnvironments.getItems().add(editEnvsAction);
        editEnvironments.getItems().add(noSelection);
        editEnvironments.getItems().add(new SeparatorMenuItem());

        project.getEnvironments().forEach(env -> {
            MenuItem item = new MenuItem(env.getName());
            item.setMnemonicParsing(false);
            item.setOnAction(actionEvent -> {
                ConsoleLogger.info(env.getName() + " clicked (" + env.getUuid() + ")");
                setSelectedEnvironment(env);
            });
            editEnvironments.getItems().add(item);
        });

        if (defaultEnvironment == null && !noneEnvironmentSelected) {
            editEnvironments.setText(bundle.getString("edit_environments_label"));
            project.setDefaultEnvironmentUuid(null);
        } else if (defaultEnvironment != null) {
            editEnvironments.setText(defaultEnvironment.getName());
            noneEnvironmentSelected = false;
            project.setDefaultEnvironmentUuid(defaultEnvironment.getUuid());
        } else {
            editEnvironments.setText("none");
            project.setDefaultEnvironmentUuid(null);
        }
        ConsoleLogger.info("selected environment {}", defaultEnvironment);
    }

    private void setSelectedEnvironment(Environment env) {
        if (env == null) {
            noneEnvironmentSelected = true;
            project.setDefaultEnvironmentUuid(null);
        } else {
            editEnvironments.setText(env.getName());
            defaultEnvironment = env;
            noneEnvironmentSelected = false;
            project.setDefaultEnvironmentUuid(env.getUuid());
        }
    }

    private void rebuildProxyList() {
        editProxies.getItems().clear();
        if (project.getProxies().isEmpty()) {
            editProxies.setText(bundle.getString("edit_proxy_label"));
            return;
        }
        MenuItem editProxyAction = new MenuItem(bundle.getString("edit_proxy_label"));
        editProxyAction.setOnAction(actionEvent -> openProxyEditor());

        MenuItem noSelection = new MenuItem("none");
        noSelection.setOnAction(actionEvent -> {
            ConsoleLogger.info("none proxy selected");
            defaultProxy = null;
            noneProxySelected = true;
            editProxies.setText("none");
            project.setDefaultProxyUuid(null);
        });

        editProxies.getItems().add(editProxyAction);
        editProxies.getItems().add(noSelection);
        editProxies.getItems().add(new SeparatorMenuItem());

        project.getProxies().forEach(proxy -> {
            MenuItem item = new MenuItem(proxy.getName());
            item.setMnemonicParsing(false);
            item.setOnAction(actionEvent -> {
                ConsoleLogger.info(proxy.getName() + " clicked (" + proxy.getUuid() + ")");
                setSelectedProxy(proxy);
            });
            editProxies.getItems().add(item);
        });
        if (defaultProxy == null & !noneProxySelected) {
            editProxies.setText(bundle.getString("edit_proxy_label"));
            project.setDefaultProxyUuid(null);
        } else if (defaultProxy != null) {
            editProxies.setText(defaultProxy.getName());
            noneProxySelected = false;
            project.setDefaultProxyUuid(defaultProxy.getUuid());
        } else {
            editProxies.setText("none");
            project.setDefaultProxyUuid(null);
        }
        ConsoleLogger.info("selected proxy {}", defaultProxy);
    }

    private void setSelectedProxy(Proxy proxy) {
        if (proxy == null) {
            noneProxySelected = true;
        } else {
            editProxies.setText(proxy.getName());
            defaultProxy = proxy;
            noneProxySelected = false;
            project.setDefaultProxyUuid(proxy.getUuid());
        }
    }

    /**
     * Opens Environments editor and sets defaultEnvironment after user submits changes
     */
    private void openEnvironmentsEditor() {
        ConsoleLogger.info("starting Edit environments window...");
        Stage stage = new Stage();
        Parent root;
        FXMLLoader loader = new FXMLLoader(EnvironmentsViewController.class.getResource("/fxml/EnvironmentsView.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("problem with EnvironmentsView", e);
        }
        EnvironmentsViewController controller = loader.getController();
        controller.setEnvironments(copy(project.getEnvironments()));
        controller.setSavedEnvironments(project.getEnvironments());
        controller.setTheme(preferences.getApplicationSettings().getAppTheme());
        if (defaultEnvironment != null) {
            controller.setDefaultEnvironment(defaultEnvironment);
        }
        stage.setScene(new Scene(root));
        stage.setTitle(bundle.getString("edit_environments_label"));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
        ConsoleLogger.info("environments windows submitted: {}", controller.wasSubmitted());
        if (controller.wasSubmitted()) {
            project.setEnvironments(controller.getEnvironments());
            /* select environment by UUID, other fields like name may change */
            if (defaultEnvironment != null) {
                defaultEnvironment = findEnvironmentByUuid(defaultEnvironment.getUuid());
            }
            if (controller.getSelectedEnvironment() != null) {
                defaultEnvironment = controller.getSelectedEnvironment();
            }
            rebuildEnvironmentsList();
        }
    }

    /**
     * Opens Proxy editor and sets defaultProxy when user submits changes
     */
    private void openProxyEditor() {
        ConsoleLogger.info("starting Edit Proxies window");
        Stage stage = new Stage();
        Parent root;
        FXMLLoader loader = new FXMLLoader(ProxiesViewController.class.getResource("/fxml/ProxiesView.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("problem with ProxiesView", e);
        }
        ProxiesViewController controller = loader.getController();
        controller.setProxies(copy(project.getProxies()));
        controller.setSavedProxies(project.getProxies());
        controller.setTheme(preferences.getApplicationSettings().getAppTheme());
        if (defaultProxy != null) {
            controller.setDefaultProxy(defaultProxy);
        }
        stage.setScene(new Scene(root));
        stage.setTitle(bundle.getString("edit_proxy_label"));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
        ConsoleLogger.info("proxies windows submitted: {}", controller.wasSubmitted());
        if (controller.wasSubmitted()) {
            project.setProxies(controller.getProxies());
            if (defaultProxy != null) {
                defaultProxy = findProxyByUuid(defaultProxy.getUuid());
            }
            if (controller.getSelectedProxy() != null) {
                defaultProxy = controller.getSelectedProxy();
            }
            rebuildProxyList();
        }
    }

    /**
     * Finds Environment with given UUID, because other fields like name may change.
     * Return null if not found.
     *
     * @param uuid environments uuid
     * @return instance of environment or null
     */
    private Environment findEnvironmentByUuid(String uuid) {
        if (StringUtils.isEmpty(uuid))
            return null;
        Optional<Environment> env = project.getEnvironments().parallelStream().filter(e -> e.getUuid().equals(uuid)).findFirst();
        return env.orElse(null);
    }

    /**
     * Finds Proxy with given UUID, because other fields like name may change.
     * Return null if not found.
     *
     * @param uuid proxy uuid
     * @return instance of Proxy or null
     */
    private Proxy findProxyByUuid(String uuid) {
        if (StringUtils.isEmpty(uuid))
            return null;
        Optional<Proxy> env = project.getProxies().parallelStream().filter(e -> e.getUuid().equals(uuid)).findFirst();
        return env.orElse(null);
    }

    @FXML
    private void addNewRequestAction(ActionEvent event) {
        Request request = (Request) UIUtils.createNewItem(projectTree.getRoot(), Request.class);
        if (request == null)
            return;
        project.getTreeItems().add(request);
        TreeItem<Item> requestItem = new TreeItem<>(request);
        requestItem.setExpanded(false);
        projectTree.getRoot().getChildren().add(requestItem);
    }

    @FXML
    private void addNewFolderAction(ActionEvent event) {
        Folder folder = (Folder) UIUtils.createNewItem(projectTree.getRoot(), Folder.class);
        if (folder == null)
            return;
        project.getTreeItems().add(folder);
        TreeItem<Item> folderItem = new TreeItem<>(folder);
        folderItem.setExpanded(false);
        projectTree.getRoot().getChildren().add(folderItem);
    }

    @FXML
    private void handleRequestDoubleClickedEvent(MouseEvent event) throws IOException {
        if (event.getClickCount() < 2)
            return;
        TreeItem<Item> t = projectTree.getSelectionModel().getSelectedItem();
        if (t == null)
            return;
        ConsoleLogger.debug(String.format("clicked (%d times) on '%s'", event.getClickCount(), t.getValue().getName()));
        if (t.getValue() instanceof Request)
            loadNewTab((Request) t.getValue(), true);
    }

    @FXML
    private void handleRequestKeyPressed(KeyEvent ke) throws IOException {
        if (!ke.getCode().equals(KeyCode.ENTER))
            return;
        Item t = projectTree.getSelectionModel().getSelectedItem().getValue();
        if (t instanceof Request)
            loadNewTab((Request) t, true);
    }

    @FXML
    public void menuFileStartNewProject(ActionEvent event) {
        String cmd = ProcessHandle.current().info().commandLine().orElse("");
        try {
            Runtime.getRuntime().exec(cmd + " \"\"");
        } catch (Exception e) {
            ConsoleLogger.error(e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Dialog");
            alert.setHeaderText(null);
            alert.setContentText("Problem with starting new project");
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.initOwner(root.getScene().getWindow());
            alert.showAndWait();
            alert.close();
        }
    }

    @FXML
    public void menuFileOpen(ActionEvent event) {
        Stage mainWindow = (Stage) root.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(bundle.getString("save_project_window_label"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        File projectFile = fileChooser.showOpenDialog(mainWindow);
        if (projectFile == null)
            return;
        String cmd = ProcessHandle.current().info().commandLine().orElse("");
        try {
            Runtime.getRuntime().exec(cmd + " " + projectFile.getAbsolutePath());
        } catch (Exception e) {
            ConsoleLogger.error(e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Dialog");
            alert.setHeaderText(null);
            alert.setContentText("Problem with project file: " + projectFile.getName());
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.initOwner(root.getScene().getWindow());
            alert.showAndWait();
            alert.close();
        }
    }

    @FXML
    public void menuFileSaveHandle(ActionEvent event) {
        UIUtils.updateItemsInternalIds(rootItem);
        project.saveProject();
    }

    @FXML
    public void menuFileSaveAsHandle(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(bundle.getString("save_project_as_window_label"));
        if (project.isSaved())
            fileChooser.setInitialFileName(project.getPath().getFileName().toString());
        else
            fileChooser.setInitialFileName("project.json");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"),
                new FileChooser.ExtensionFilter("All Files", "*"));
        File projectFile = fileChooser.showSaveDialog(root.getScene().getWindow());
        if (projectFile != null) {
            UIUtils.updateItemsInternalIds(rootItem);
            project.setFile(projectFile);
            project.saveProject();
            preferences.addPathToLastOpenProjects(Paths.get(projectFile.getAbsolutePath()));
        }
    }

    @FXML
    public void exitApp(ActionEvent event) {
        handleAppExit(event);
    }

    @FXML
    public void showHideTreeView(ActionEvent event) {
        if (treeViewVisible) {
            paneSplitterPositions = hiddenSplitter.getDividerPositions();
            paneComponent = hiddenSplitter.getItems().get(0);
            hiddenSplitter.getItems().remove(paneComponent);
            treeViewVisible = false;
            hideProjectTreeMenuItem.setText(bundle.getString("view_show_project_tree_label"));
        } else {
            if (paneSplitterPositions[0] == 0.0) {
                paneSplitterPositions[0] = 0.3;
            }
            if (paneSplitterPositions[0] == 1.0) {
                paneSplitterPositions[0] = 0.3;
            }
            hiddenSplitter.getItems().add(0, paneComponent);
            hiddenSplitter.setDividerPositions(paneSplitterPositions);
            ConsoleLogger.info("show/hide position {}", Arrays.toString(paneSplitterPositions));
            treeViewVisible = true;
            hideProjectTreeMenuItem.setText(bundle.getString("view_hide_project_tree_label"));
        }
    }

    private void loadNewTab(Request t, boolean updatePreferences) throws IOException {
        ConsoleLogger.debug("opening request " + t.getName() + " (" + t.getId() + ")");
        Optional<Tab> existingTab = tabPane.getTabs().stream().filter(tab -> tab.getId().equals(t.getId())).findFirst();
        if (existingTab.isPresent()) {
            tabPane.getSelectionModel().select(existingTab.get());
        } else {
            Tab tab = new Tab(t.getName());
            tab.setId(t.getId());

            ContextMenu contextMenu = new ContextMenu();
            List<MenuItem> tabItems = new ArrayList<>();
            MenuItem closeAction = new MenuItem(bundle.getString("tab_context_close_label"));
            closeAction.setOnAction(event -> {
                tabPane.getTabs().remove(tab);
                updateOpenRequestsPreferences();
            });
            MenuItem closeAllAction = new MenuItem(bundle.getString("view_close_all_tabs_label"));
            closeAllAction.setOnAction(event -> {
                tabPane.getTabs().clear();
                updateOpenRequestsPreferences();
            });
            MenuItem closeOtherAction = new MenuItem(bundle.getString("tab_context_close_other_tabs_label"));
            closeOtherAction.setOnAction(event -> {
                tabPane.getTabs().retainAll(tab);
                updateOpenRequestsPreferences();
            });
            MenuItem closeTabsOnRight = new MenuItem(bundle.getString("tab_context_close_tabs_on_right_label"));
            closeTabsOnRight.setOnAction(event -> {
                boolean found = false;
                List<Tab> tabsToSave = Lists.newArrayList();
                for (Tab tt : tabPane.getTabs()) {
                    if (!found) {
                        tabsToSave.add(tt);
                    }
                    if (tt.equals(tab)) {
                        found = true;
                    }

                }
                tabPane.getTabs().retainAll(tabsToSave);
                updateOpenRequestsPreferences();
            });
            tabItems.add(closeAction);
            tabItems.add(closeAllAction);
            tabItems.add(closeOtherAction);
            tabItems.add(closeTabsOnRight);
            contextMenu.getItems().addAll(tabItems);
            tab.setContextMenu(contextMenu);
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
            tabPane.requestFocus();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RequestView.fxml"));
            AnchorPane pane = loader.load();
            tab.setContent(pane);
            RequestViewController controller = loader.getController();
            controller.setViewOrientation(splitPaneOrientation);
            controller.setRequest(t);
            controller.mapValues();
            controller.mapEvents();

            /* OptionsPaneToggleEvent details */
            EventHandler<OptionsPaneToggleEvent> paneToggleEventEventHandler = event -> {
                if (event.isDisablePane())
                    controller.splitPaneDisable(event.getLastDividerPositions(), splitPaneOrientation);
                else
                    controller.splitPaneEnable(event.getLastDividerPositions(), splitPaneOrientation);
                optionsPaneDisabled = event.isDisablePane();
                if (optionsPaneDisabled)
                    hideRequestDetailsMenuItem.setText(bundle.getString("view_show_request_details_label"));
                else {
                    hideRequestDetailsMenuItem.setText(bundle.getString("view_hide_request_details_label"));
                }
                lastDividerPositions = event.getLastDividerPositions();
            };
            tabPane.addEventHandler(OptionsPaneToggleEvent.TOGGLE_OPTIONS_PANE, paneToggleEventEventHandler);

            /* OptionsPaneResizeEvent details */
            EventHandler<OptionsPaneResizeEvent> paneResizeEventEventHandler = event -> {
                controller.resizeSplitPane(event.getLastDividerPositions());
                if (!optionsPaneDisabled) {
                    lastDividerPositions = event.getLastDividerPositions();
                }
            };
            tabPane.addEventHandler(OptionsPaneResizeEvent.RESIZE_OPTIONS_PANE, paneResizeEventEventHandler);

            /* [WS] OptionsPaneSwitchTabEvent has been commented-out because keeping the same option
             * opened for all requests is not possible because "payload" option is enabled only for
             * POST/PUT requests. This creates unnecessary complexity so each request tab will have
             * their own option selected regardless of what's on the other tabs.
             */

            /* removing event handlers when tab is closed */
            tab.setOnCloseRequest(event -> {
                ConsoleLogger.info("closing tab event");
                tabPane.removeEventHandler(OptionsPaneToggleEvent.TOGGLE_OPTIONS_PANE, paneToggleEventEventHandler);
                tabPane.removeEventHandler(OptionsPaneResizeEvent.RESIZE_OPTIONS_PANE, paneResizeEventEventHandler);

                Tab sourceTab = null;
                try {
                    sourceTab = (Tab) event.getSource();
                } catch (Exception e) {
                    ConsoleLogger.error("problem with close tab event source");
                }
                Tab src = sourceTab;

                List<String> openRequests = tabPane.getTabs()
                        .stream()
                        .filter(e -> !e.equals(src))
                        .map(Tab::getId)
                        .collect(Collectors.toList());
                preferences.setOpenRequests(project.getProjectUuid(), openRequests);
                if (openRequests.size() == 0) {
                    projectTree.getSelectionModel().clearSelection();
                }
            });

            if (updatePreferences) {
                /* update list of open requests tabs inside preferences files */
                List<String> openRequests = tabPane.getTabs()
                        .stream()
                        .map(Tab::getId)
                        .collect(Collectors.toList());
                preferences.setOpenRequests(project.getProjectUuid(), openRequests);
            }
        }
    }

    private void recreateProjectTree(TreeItem<Item> node) {
        ConsoleLogger.info("creating project tree based in: " + node.getValue().getName());
        List<Item> currentLevelItems;
        if (node.equals(projectTree.getRoot())) {
            currentLevelItems = project.getTreeItems().stream().filter(t -> t.getParent().isEmpty()).collect(Collectors.toList());
        } else {
            currentLevelItems = project.getTreeItems().stream().filter(t -> t.getParent().equals(node.getValue().getId())).collect(Collectors.toList());
        }
        sortElementsByPreviousItemId(currentLevelItems);
        Platform.runLater(() -> {
            currentLevelItems.forEach(item -> node.getChildren().add(new TreeItem<>(item)));
            node.getChildren().forEach(c -> {
                if (c.getValue() instanceof Folder)
                    recreateProjectTree(c);
            });
        });
    }

    private void sortElementsByPreviousItemId(List<Item> items) {
        int head = 0;
        if (items.size() <= 1)
            return;
        /* move first element to index 0 */
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isFirstChild())
                Collections.swap(items, i, head);
        }
        /* reorder all remaining elements */
        for (int i = 1; i < items.size(); i++) {
            for (int j = i; j < items.size(); j++) {
                if (items.get(j).getPrevious().equals(items.get(head).getId())) {
                    Collections.swap(items, ++head, j);
                    break;
                }
            }
        }
    }

    private <T extends DeepCopy> ObservableList<T> copy(ObservableList<T> list) {
        if (list == null)
            return null;
        ObservableList<T> environments = FXCollections.observableArrayList();
        if (list.isEmpty())
            return environments;
        list.forEach(env -> {
            T environment = (T) env.copy();
            environments.add(environment);
        });
        return environments;
    }

    private void handleAppExit(Event event) {
        ConsoleLogger.info("isChanged() status: {}", changeListener.isChanged());
        if (applicationSettings.isAutoSaveProject()) {
            ConsoleLogger.info("auto-saving project");
            UIUtils.updateItemsInternalIds(rootItem);
            project.saveProject();
        }
        if (changeListener.isChanged()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(bundle.getString("exit_label"));
            alert.setHeaderText(null);
            alert.setContentText(bundle.getString("save_changes_before_closing_label"));
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource("/css/name_value_dialog.css").toExternalForm());
            if (preferences.getApplicationSettings().getAppTheme() == AppTheme.Dark) {
                dialogPane.getStyleClass().add(AppTheme.Dark.getStyleName());
            } else {
                dialogPane.getStyleClass().remove(AppTheme.Dark.getStyleName());
            }
            ButtonType buttonYes = new ButtonType(bundle.getString("yes_label"), ButtonBar.ButtonData.YES);
            ButtonType buttonNo = new ButtonType(bundle.getString("no_label"), ButtonBar.ButtonData.NO);
            ButtonType buttonCancel = new ButtonType(bundle.getString("cancel_label"), ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(buttonYes, buttonNo, buttonCancel);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == buttonYes) {
                UIUtils.updateItemsInternalIds(rootItem);
                project.saveProject();
                Platform.exit();
            } else if (result.isPresent() && result.get() == buttonCancel) {
                event.consume();
            } else {
                Platform.exit();
            }
        } else {
            Platform.exit();
        }
    }

    private void addRecentProjectEntry(Path path) {
        if (path.toString().equals(project.getAbsolutePath()))
            return;
        MenuItem item = new MenuItem(path.getFileName().toString());
        item.setUserData(path);
        item.setMnemonicParsing(false);
        item.setOnAction(event -> {
            String proj = ((Path) item.getUserData()).toAbsolutePath().toString();
            ConsoleLogger.debug("opening project: " + proj);
            String cmd = ProcessHandle.current().info().commandLine().orElse("");
            try {
                Runtime.getRuntime().exec(cmd + " " + proj);
            } catch (Exception e) {
                ConsoleLogger.error(e.getMessage());
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Dialog");
                alert.setHeaderText(null);
                alert.setContentText("Problem with project file " + proj);
                alert.initModality(Modality.APPLICATION_MODAL);
                alert.initOwner(root.getScene().getWindow());
                alert.showAndWait();
                alert.close();
            }
        });
        openRecent.getItems().add(item);
    }

    /**
     * Asks user to save any unsaved changes. Closes all open tabs. If user clicks "No" button, then changes are lost.
     * If user clicks "Cancel" button then closing process is canceled like nothing happend.
     *
     * @param event source action event
     * @return true if user saved his changes and is ready to open another project, false if user wants to abort
     */
    private boolean closeUnsavedChanges(ActionEvent event) {
        if (applicationSettings.isAutoSaveProject()) {
            ConsoleLogger.info("auto-saving project");
            UIUtils.updateItemsInternalIds(rootItem);
            project.saveProject();
        }
        /* if changes were made, ask user to save them */
        if (changeListener.isChanged()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(bundle.getString("exit_label"));
            alert.setHeaderText(null);
            alert.setContentText(bundle.getString("save_changes_label"));
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource("/css/name_value_dialog.css").toExternalForm());
            if (preferences.getApplicationSettings().getAppTheme() == AppTheme.Dark) {
                dialogPane.getStyleClass().add(AppTheme.Dark.getStyleName());
            } else {
                dialogPane.getStyleClass().remove(AppTheme.Dark.getStyleName());
            }
            ButtonType buttonYes = new ButtonType(bundle.getString("yes_label"), ButtonBar.ButtonData.YES);
            ButtonType buttonNo = new ButtonType(bundle.getString("no_label"), ButtonBar.ButtonData.NO);
            ButtonType buttonCancel = new ButtonType(bundle.getString("cancel_label"), ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(buttonYes, buttonNo, buttonCancel);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == buttonYes) {
                if (project.isSaved()) {
                    /* save existing project file */
                    UIUtils.updateItemsInternalIds(rootItem);
                    project.saveProject();
                } else {
                    /* choose project file and save */
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle(bundle.getString("yes_label"));
                    fileChooser.setInitialFileName("project.json");
                    fileChooser.getExtensionFilters().addAll(
                            new FileChooser.ExtensionFilter("JSON Files", "*.json"),
                            new FileChooser.ExtensionFilter("All Files", "*"));
                    File projectFile = fileChooser.showSaveDialog(root.getScene().getWindow());
                    if (projectFile != null) {
                        UIUtils.updateItemsInternalIds(rootItem);
                        project.setFile(projectFile);
                        project.saveProject();
                        preferences.addPathToLastOpenProjects(Paths.get(projectFile.getAbsolutePath()));
                    }
                }
            } else if (result.isPresent() && result.get() == buttonCancel) {
                event.consume();
                return false;
            }
        }
        /* UI cleanup - close all open tabs */
        tabPane.getTabs().clear();

        /* remove all listeners related to old project, new listeners will be created by initialize() method */
        project.getTreeItems().removeListener(closeTabAfterDeletingItemListener);
        tabPane.getSelectionModel().selectedItemProperty().removeListener(focusTreeItemListener);
        editEnvironments.showingProperty().removeListener(openEditEnvironmentsListener);
        editProxies.showingProperty().removeListener(openEditProxiesListener);
        return true;
    }

    @FXML
    public void setPolish(ActionEvent event) {
        preferences.setLocale(new Locale("pl", "PL"));
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(bundle.getString("language_title_label"));
        alert.setHeaderText(null);
        alert.setContentText(bundle.getString("language_polish_selected_message_label"));
        alert.showAndWait();
    }

    @FXML
    public void setEnglish(ActionEvent event) {
        preferences.setLocale(new Locale("en", "EN"));
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(bundle.getString("language_title_label"));
        alert.setHeaderText(null);
        alert.setContentText(bundle.getString("language_english_selected_message_label"));
        alert.showAndWait();
    }

    @FXML
    public void setChinese(ActionEvent event) {
        preferences.setLocale(new Locale("cn", "CN"));
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(bundle.getString("language_title_label"));
        alert.setHeaderText(null);
        alert.setContentText(bundle.getString("language_chinese_selected_message_label"));
        alert.showAndWait();
    }

    @FXML
    public void openApplicationSettings(ActionEvent event) {
        ConsoleLogger.info("opening preferences window");
        Stage stage = new Stage();
        Parent root;
        FXMLLoader loader = new FXMLLoader(PreferencesViewController.class.getResource("/fxml/PreferencesView.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("problem with PreferencesView", e);
        }
        PreferencesViewController controller = loader.getController();
        controller.setApplicationSettings(applicationSettings);
        controller.setProjectSettings(project.getProjectSettings());
        controller.setTabPane(tabPane);
        controller.setRoot(this.root);
        controller.setTheme(preferences.getApplicationSettings().getAppTheme());
        controller.setApplyMethod(() -> {
            preferences.setApplicationSettings(controller.getApplicationSettings());
            project.setProjectSettings(controller.getProjectSettings());
            applicationSettings = controller.getApplicationSettings();
            changeListener.setChanged();
            root.getStyleClass().remove(AppTheme.Dark.getStyleName());
            menuBar.getStyleClass().remove(AppTheme.Dark.getStyleName());
            if (applicationSettings.getAppTheme() == AppTheme.Dark) {
                root.getStyleClass().add(AppTheme.Dark.getStyleName());
                menuBar.getStyleClass().add(AppTheme.Dark.getStyleName());
            }
        });
        stage.setScene(new Scene(root));
        stage.setTitle(bundle.getString("file_menu_settings_label"));
        stage.initModality(Modality.APPLICATION_MODAL);
        controller.showApplicationSettings(null);
        stage.showAndWait();
    }

    @FXML
    public void openProjectSettings(ActionEvent event) {
        ConsoleLogger.info("opening preferences window");
        Stage stage = new Stage();
        Parent root;
        FXMLLoader loader = new FXMLLoader(PreferencesViewController.class.getResource("/fxml/PreferencesView.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("problem with PreferencesView", e);
        }
        PreferencesViewController controller = loader.getController();
        controller.setApplicationSettings(applicationSettings);
        controller.setProjectSettings(project.getProjectSettings());
        controller.setTabPane(tabPane);
        controller.setRoot(this.root);
        controller.setTheme(preferences.getApplicationSettings().getAppTheme());
        controller.setApplyMethod(() -> {
            preferences.setApplicationSettings(controller.getApplicationSettings());
            project.setProjectSettings(controller.getProjectSettings());
            applicationSettings = controller.getApplicationSettings();
            changeListener.setChanged();
            root.getStyleClass().remove(AppTheme.Dark.getStyleName());
            menuBar.getStyleClass().remove(AppTheme.Dark.getStyleName());
            if (applicationSettings.getAppTheme() == AppTheme.Dark) {
                root.getStyleClass().add(AppTheme.Dark.getStyleName());
                menuBar.getStyleClass().add(AppTheme.Dark.getStyleName());
            }
        });
        stage.setScene(new Scene(root));
        stage.setTitle(bundle.getString("file_menu_settings_label"));
        stage.initModality(Modality.APPLICATION_MODAL);
        controller.showProjectSettings(null);
        stage.showAndWait();
        if (controller.wasSubmitted()) {
            preferences.setApplicationSettings(controller.getApplicationSettings());
            project.setProjectSettings(controller.getProjectSettings());
            changeListener.setChanged();
        }
    }

    @FXML
    public void changeSplitOrientation(ActionEvent event) {
        if (tabPane.getTabs().size() == 0)
            return;
        if (splitPaneOrientation == Orientation.HORIZONTAL) {
            splitPaneOrientation = Orientation.VERTICAL;
            switchOrientationMenuItem.setText(bundle.getString("view_horizontal_view_label"));
        }
        else if (splitPaneOrientation == Orientation.VERTICAL) {
            splitPaneOrientation = Orientation.HORIZONTAL;
            switchOrientationMenuItem.setText(bundle.getString("view_vertical_view_label"));
        }
        for (Tab tab : tabPane.getTabs()) {
            Node tabNode = tab.getContent();
            SplitPaneOrientationChangeEvent changeEvent = new SplitPaneOrientationChangeEvent(this, tabNode, splitPaneOrientation);
            tabNode.fireEvent(changeEvent);
        }
    }

    @FXML
    public void aboutViewOpen(ActionEvent event) {
        ConsoleLogger.info("starting About window...");
        Stage stage = new Stage();
        Parent root;
        FXMLLoader loader = new FXMLLoader(EnvironmentsViewController.class.getResource("/fxml/AboutView.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("problem with AboutView", e);
        }
        AboutViewController controller = loader.getController();
        stage.setScene(new Scene(root));
        stage.setTitle(bundle.getString("help_menu_about_label"));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent ev) -> {
            if (ev.getCode() == KeyCode.ESCAPE)
                stage.close();
        });
        stage.showAndWait();
    }

    @FXML
    public void getExecutorLogs(ActionEvent event) {
        Stage stage = new Stage();
        Parent root;
        FXMLLoader loader = new FXMLLoader(HeadersDialogController.class.getResource("/fxml/ExecutorLogView.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("problem with ExecutorLogView", e);
        }
        ExecutorLogViewController controller = loader.getController();
        stage.setScene(new Scene(root));
        stage.setTitle("Executor log");
        stage.initModality(Modality.WINDOW_MODAL);
        controller.initializeEvents();
        controller.setTheme(preferences.getApplicationSettings().getAppTheme());
        stage.show();
    }

    @FXML
    public void closeAllOpenTabs(ActionEvent event) {
        tabPane.getTabs().clear();
        updateOpenRequestsPreferences();
    }

    @FXML
    public void closeCurrentTab(ActionEvent event) {
        Tab tabToRemove = tabPane.getSelectionModel().getSelectedItem();
        if (tabToRemove == null)
            return;
        tabPane.getTabs().remove(tabToRemove);
        updateOpenRequestsPreferences();
    }

    @FXML
    public void closeOtherTabs(ActionEvent event) {
        Tab tabToRetain = tabPane.getSelectionModel().getSelectedItem();
        if (tabToRetain == null)
            return;
        tabPane.getTabs().retainAll(tabToRetain);
        updateOpenRequestsPreferences();
    }

    @FXML
    public void closeTabsOnRight(ActionEvent event) {
        Tab focusedTab = tabPane.getSelectionModel().getSelectedItem();
        if (focusedTab == null)
            return;
        boolean found = false;
        List<Tab> tabsToSave = Lists.newArrayList();
        for (Tab tt : tabPane.getTabs()) {
            if (!found) {
                tabsToSave.add(tt);
            }
            if (tt.equals(focusedTab)) {
                found = true;
            }

        }
        tabPane.getTabs().retainAll(tabsToSave);
        updateOpenRequestsPreferences();
    }

    private void updateOpenRequestsPreferences() {
        List<String> openRequests = tabPane.getTabs()
                .stream()
                .map(Tab::getId)
                .collect(Collectors.toList());
        preferences.setOpenRequests(project.getProjectUuid(), openRequests);
    }

    @FXML
    public void toggleRequestDetails(ActionEvent event) {
        boolean isDisabled = !optionsPaneDisabled;
        OptionsPaneToggleEvent toggleEvent = new OptionsPaneToggleEvent(this, root, isDisabled, lastDividerPositions);
        optionsPaneDisabled = isDisabled;
        if (isDisabled)
            hideRequestDetailsMenuItem.setText(bundle.getString("view_show_request_details_label"));
        else
            hideRequestDetailsMenuItem.setText(bundle.getString("view_hide_request_details_label"));
        tabPane.fireEvent(toggleEvent);
    }
}