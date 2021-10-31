/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui;

import com.google.common.collect.Lists;
import com.wsojka.hammerui.cli.CliExecutor;
import com.wsojka.hammerui.cli.CliExecutorFactory;
import com.wsojka.hammerui.cli.CliParams;
import com.wsojka.hammerui.controllers.MainViewController;
import com.wsojka.hammerui.enums.AppTheme;
import com.wsojka.hammerui.enums.LogLevel;
import com.wsojka.hammerui.events.shortcuts.CloseActiveTabEvent;
import com.wsojka.hammerui.events.shortcuts.SaveProjectEvent;
import com.wsojka.hammerui.persintence.*;
import com.wsojka.hammerui.ui.UIPreloader;
import com.wsojka.hammerui.ui.UserAlert;
import com.wsojka.hammerui.utils.ConsoleLogger;
import com.wsojka.hammerui.utils.UIUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class Main extends Application {

    public static Stage primaryStage;

    private Parent rootNode;
    private ProjectFileWrapper project = ProjectFileWrapperFactory.getInstance();
    private ProjectChangeListener changeListener = ProjectChangeListenerFactory.getInstance();
    private PreferencesWrapper preferences = PreferencesWrapperFactory.getInstance();
    private boolean projectLoaded = true;
    private List<UserAlert> alerts = Lists.newArrayList();
    private MainViewController mainViewController;
    private ApplicationSettings applicationSettings;

    private static CliExecutor cliExecutor = CliExecutorFactory.getInstance();

    public static void main(String[] args) {
        var version = java.lang.Runtime.version();
        ConsoleLogger.info("JVM: " + version);
        String cmd = ProcessHandle.current().info().commandLine().orElse("");
        ConsoleLogger.info("Command: " + cmd);
        String[] arguments = parseCliArguments(args);
        System.setProperty("javafx.preloader", UIPreloader.class.getCanonicalName());
        launch(arguments);
    }

    @Override
    public void init() throws Exception {
        applicationSettings = preferences.getApplicationSettings();

        /* load project from file if it was provided, handle any errors */
        List<String> params = getParameters().getRaw();
        alerts.clear();
        ConsoleLogger.info("Program arguments: " + params.toString());
        if (params.isEmpty() && applicationSettings.isOpenLastProjectAtStartup()) {
            List<Path> projects = preferences.getLastOpenProjects();
            if (projects != null && !projects.isEmpty())
                params = Lists.newArrayList(projects.get(0).toAbsolutePath().toString());
        }
        if (params.size() > 0 && !params.get(params.size() - 1).isEmpty() && !StringUtils.equals(params.get(params.size() - 1), "\"\"")) {
            String projectArgument = params.get(params.size() - 1);
            File projectFile = new File(projectArgument);
            //TODO: open read-only project in "read only" mode
            //TODO: load project file based on users's current location in filesystem
            if (!projectFile.exists() || !projectFile.canRead() || !projectFile.canWrite() || projectFile.isDirectory()) {
                /* incorrect path provided or no privileges to read/write */
                UserAlert alert = new UserAlert();
                alert.setType(Alert.AlertType.ERROR);
                alert.setTitle("Error Dialog");
                alert.setContentText("Problem opening project file: " + projectFile.getName());
                alerts.add(alert);
                projectLoaded = false;
            } else {
                ConsoleLogger.debug("opening project file " + projectArgument);
                projectLoaded = project.loadProject(projectFile);
                if (!projectLoaded && !project.isProjectFileCompatible()) {
                    /* show error if project is not compatible with current app version */
                    UserAlert alert = new UserAlert();
                    alert.setType(Alert.AlertType.ERROR);
                    alert.setTitle("Project file not compatible");
                    alert.setContentText("Could not open project file " + projectArgument + "\nPlease upgrade to the latest version of HammerUI.");
                    alerts.add(alert);
                } else if (!projectLoaded) {
                    /* show error if we can't open the project for whatever reason */
                    UserAlert alert = new UserAlert();
                    alert.setType(Alert.AlertType.ERROR);
                    alert.setTitle("Error Dialog");
                    alert.setContentText("Problem opening project file " + projectFile.getName());
                    alerts.add(alert);
                }
            }
        }
        ConsoleLogger.debug("opening MainView");
        FXMLLoader loader = new FXMLLoader();
        loader.setResources(ResourceBundle.getBundle("bundles.MainView", preferences.getLocale()));
        loader.setLocation(Main.class.getResource("/fxml/MainView.fxml"));
        rootNode = (AnchorPane) loader.load();
        mainViewController = loader.getController();
    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        stage.setScene(new Scene(rootNode));
        stage.setMaximized(false);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/logo.png")));
        stage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            KeyCombination save = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
            KeyCombination closeActiveTab = new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN);
            if (save.match(event)) {
                SaveProjectEvent saveEvent = new SaveProjectEvent(this, rootNode);
                rootNode.fireEvent(saveEvent);
            } else if (closeActiveTab.match(event)) {
                CloseActiveTabEvent closeActiveTabEvent = new CloseActiveTabEvent(this, rootNode);
                rootNode.fireEvent(closeActiveTabEvent);
            }
        });
        setUserAgentStylesheet(STYLESHEET_MODENA);
        stage.getScene().getWindow().setOnShowing(windowEvent -> {
            Platform.runLater(() -> {
                if (!projectLoaded) {
                    for (UserAlert userAlert : alerts) {
                        Alert alert = new Alert(userAlert.getType());
                        alert.setTitle(userAlert.getTitle());
                        alert.setHeaderText(null);
                        alert.setContentText(userAlert.getContentText());
                        alert.initModality(Modality.APPLICATION_MODAL);
                        alert.initOwner(primaryStage);
                        DialogPane dialogPane = alert.getDialogPane();
                        dialogPane.getStylesheets().add(getClass().getResource("/css/name_value_dialog.css").toExternalForm());
                        if (preferences.getApplicationSettings().getAppTheme() == AppTheme.Dark) {
                            dialogPane.getStyleClass().add(AppTheme.Dark.getStyleName());
                        } else {
                            dialogPane.getStyleClass().remove(AppTheme.Dark.getStyleName());
                        }
                        alert.showAndWait();
                        alert.close();
                    }
                    alerts.clear();
                }
            });
        });
        Platform.runLater(() -> {
            stage.setOnCloseRequest(event -> {
                ConsoleLogger.info("closing application...");
                project = ProjectFileWrapperFactory.getInstance();
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Exit");
                alert.setHeaderText(null);
                alert.setContentText("Save changes before closing?");
                ButtonType buttonYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
                ButtonType buttonNo = new ButtonType("No", ButtonBar.ButtonData.NO);
                ButtonType buttonCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(buttonYes, buttonNo, buttonCancel);
                DialogPane dialogPane = alert.getDialogPane();
                dialogPane.getStylesheets().add(getClass().getResource("/css/name_value_dialog.css").toExternalForm());
                if (preferences.getApplicationSettings().getAppTheme() == AppTheme.Dark) {
                    dialogPane.getStyleClass().add(AppTheme.Dark.getStyleName());
                } else {
                    dialogPane.getStyleClass().remove(AppTheme.Dark.getStyleName());
                }
                ConsoleLogger.info("isChanged() status: {}", changeListener.isChanged());
                applicationSettings = preferences.getApplicationSettings();
                if (applicationSettings.isAutoSaveProject()) {
                    ConsoleLogger.info("auto-saving project");
                    UIUtils.updateItemsInternalIds(mainViewController.getRootItem());
                    project.saveProject();
                }
                if (changeListener.isChanged()) {
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == buttonYes) {
                        project.saveProject();
                    } else if (result.isPresent() && result.get() == buttonCancel) {
                        event.consume();
                        return;
                    }
                }
                Platform.exit();
            });
        });
        stage.show();
        project.updateWindowTitle();
    }

    @Override
    public void stop() {
        ConsoleLogger.info("stop() method called");
    }

    private static String[] parseCliArguments(String[] args) {
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();

        /* generic options */
        options.addOption(Option.builder(CliParams.LOG_LEVEL)
                .longOpt(CliParams.LOG_LEVEL_OPT)
                .optionalArg(true)
                .desc("change log level, possible values: DEBUG, INFO, ERROR, NONE. Default is INFO")
                .build());

        options.addOption(Option.builder(CliParams.HELP)
                .longOpt(CliParams.HELP_OPT)
                .optionalArg(true)
                .desc("print this message")
                .build());

        String[] arguments = new String[]{};
        if (args.length > 0 && StringUtils.equalsIgnoreCase(args[0], "cli")) {
            /* CLI commands */
            options.addOption(Option.builder(CliParams.PROJECT_FILE)
                    .longOpt(CliParams.PROJECT_FILE_OPT)
                    .optionalArg(false)
                    .desc("project file")
                    .numberOfArgs(1)
                    .argName("project_file").build());

            options.addOption(Option.builder(CliParams.ENV)
                    .longOpt(CliParams.ENV_OPT)
                    .optionalArg(true)
                    .desc("environment to use")
                    .numberOfArgs(1)
                    .argName("environment_name").build());

            options.addOption(Option.builder(CliParams.PROXY)
                    .longOpt(CliParams.PROXY_OPT)
                    .optionalArg(true)
                    .desc("proxy to use")
                    .numberOfArgs(1)
                    .argName("proxy_name").build());

            try {
                CommandLine line = parser.parse(options, args);

                /* setup log level */
                if (line.hasOption(CliParams.LOG_LEVEL_OPT)) {
                    String logLevel = line.getOptionValue(CliParams.LOG_LEVEL_OPT);
                    if (StringUtils.isBlank(logLevel) || LogLevel.getByName(logLevel) == null)
                        throw new IllegalArgumentException("incorrect log level provided " + logLevel + ", accepted values: DEBUG, INFO, ERROR, NONE");
                    ConsoleLogger.LOG_LEVEL = LogLevel.getByName(logLevel);
                    ConsoleLogger.info("setting log level to " + logLevel);
                }

                /* print help message */
                if (line.hasOption(CliParams.HELP_OPT)) {
                    printHelp("hammerui cli", options, "command line execution");
                    System.exit(0);
                }

                if (!line.hasOption(CliParams.PROJECT_FILE_OPT)) {
                    printHelp("hammerui cli", options, "command line execution");
                    System.exit(0);
                }
                boolean ret = cliExecutor.execute(line);
                System.exit(ret ? 0 : 1);
            } catch (ParseException exp) {
                ConsoleLogger.error("Unexpected exception:" + exp.getMessage());
            }
            System.exit(0);
        } else {
            /* regular options */
            try {
                CommandLine line = parser.parse(options, args);

                /* setup log level */
                if (line.hasOption(CliParams.LOG_LEVEL_OPT)) {
                    String logLevel = line.getOptionValue(CliParams.LOG_LEVEL_OPT);
                    if (StringUtils.isBlank(logLevel) || LogLevel.getByName(logLevel) == null)
                        throw new IllegalArgumentException("incorrect log level provided " + logLevel + ", accepted values: DEBUG, INFO, ERROR, NONE");
                    ConsoleLogger.LOG_LEVEL = LogLevel.getByName(logLevel);
                    ConsoleLogger.info("setting log level to " + logLevel);
                }

                /* print help message */
                if (line.hasOption(CliParams.HELP_OPT)) {
                    printHelp("hammerui", options, "Rest API client");
                    System.exit(0);
                }
                arguments = line.getArgs();
            } catch (ParseException exp) {
                ConsoleLogger.error("Unexpected exception:" + exp.getMessage());
            }
        }
        return arguments;
    }

    public static void printHelp(String title, Options options, String description) {
        String header = title + " " + Constants.VERSION_STRING + " - " + description + ", (C) 2021 Waldemar Sojka\n\n";
        String footer = "\nPlease report issues at http://github.com/hammerui/hammerui_com";
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(title, header, options, footer, true);
    }
}
