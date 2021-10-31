/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.utils;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wsojka.hammerui.Constants;
import com.wsojka.hammerui.controllers.CurlExportViewController;
import com.wsojka.hammerui.controllers.HeadersDialogController;
import com.wsojka.hammerui.controllers.ValueViewController;
import com.wsojka.hammerui.dto.*;
import com.wsojka.hammerui.dto.authentication.Authentication;
import com.wsojka.hammerui.dto.authentication.BasicAuthentication;
import com.wsojka.hammerui.dto.authentication.BearerAuthentication;
import com.wsojka.hammerui.dto.payload.PostFormEntry;
import com.wsojka.hammerui.enums.AuthenticationType;
import com.wsojka.hammerui.enums.HttpMethod;
import com.wsojka.hammerui.enums.PayloadContentType;
import com.wsojka.hammerui.enums.PostFormEntryType;
import com.wsojka.hammerui.persintence.*;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.wsojka.hammerui.persintence.ObservableListTypeAdapterFactory.getObservableListTypeAdapterFactory;

/**
 * @author Waldemar Sojka
 */
public class UIUtils {

    //TODO: move tab size into user defined settings
    private static final String TAB = StringUtils.repeat(' ', 4);

    private static final String[] SIZES = {"B", "KB", "MB", "GB", "TB", "PB", "EB"};

    private static final int SIZE_BASE = 1024;

    private static final PreferencesWrapper preferences = PreferencesWrapperFactory.getInstance();

    private static final ResourceBundle bundle = ResourceBundle.getBundle("bundles.MainView", preferences.getLocale());

    private static final ProjectFileWrapper project = ProjectFileWrapperFactory.getInstance();

    public static String getValueFromUser(String suggested, String title) {
        Stage stage = new Stage();
        Parent root;
        FXMLLoader loader = new FXMLLoader(ValueViewController.class.getResource("/fxml/ValueView.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("problem with ValueView", e);
        }
        ValueViewController controller = loader.getController();
        stage.setScene(new Scene(root));
        controller.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        controller.setValue(suggested);
        controller.setTheme(preferences.getApplicationSettings().getAppTheme());
        stage.addEventHandler(KeyEvent.KEY_RELEASED, (KeyEvent ev) -> {
            if (KeyCode.ESCAPE == ev.getCode())
                stage.close();
        });
        stage.showAndWait();
        if (controller.isSubmitted())
            return controller.getValue();
        else
            return null;
    }

    public static List<Item> getAllChildren(TreeItem<Item> parent) {
        List<Item> itemsToRemove = new ArrayList<>();
        itemsToRemove.add(parent.getValue());
        if (parent.getValue() instanceof Folder) {
            for (TreeItem<Item> i : parent.getChildren())
                itemsToRemove.addAll(getAllChildren(i));
        }
        return itemsToRemove;
    }

    public static Item createNewItem(TreeItem<Item> parent, Class itemType) {
        ConsoleLogger.info("adding new {} item under \"{}\"", itemType.getSimpleName(), parent.getValue().getName());
        long count = parent.getChildren().parallelStream().filter(r -> r.getValue().getClass().equals(itemType)).count();
        String name;
        String title;
        if (itemType == Folder.class) {
            name = String.format("folder %02d", count + 1);
            title = bundle.getString("add_new_folder_title");

        } else {
            name = String.format("request %02d", count + 1);
            title = bundle.getString("add_new_request_title");
        }
        name = UIUtils.getValueFromUser(name, title);
        if (name == null)
            return null;
        Item t;
        if (itemType == Folder.class) {
            t = new Folder(name);
        } else {
            t = new Request(name);
        }

        /* set parent id */
        t.setParent(parent.getValue().getId());

        /* set previous item id for proper order */
        if (parent.getChildren().size() == 0) {
            t.setPrevious("");
        } else {
            int index = parent.getChildren().size();
            t.setPrevious(parent.getChildren().get(index - 1).getValue().getId());
        }
        return t;
    }

    public static TreeItem<Item> getRoot(TreeItem<Item> item) {
        if (item.getParent() == null)
            return item;
        else
            return getRoot(item.getParent());
    }

    public static TreeItem<Item> getTreeItemById(TreeItem<Item> node, String id) {
        if (id == null || node == null)
            throw new RuntimeException("invalid parameter provided");
        if (node.getValue().getId().equals(id))
            return node;
        for (TreeItem<Item> child : node.getChildren()) {
            TreeItem<Item> item = getTreeItemById(child, id);
            if (item != null)
                return item;
        }
        return null;
    }

    public static void updateItemsInternalIds(TreeItem<Item> item) {
        for (int i = 0; i < item.getChildren().size(); i++) {
            Item child = item.getChildren().get(i).getValue();
            if (i == 0) {
                child.setPrevious("");
            } else {
                child.setPrevious(item.getChildren().get(i - 1).getValue().getId());
            }
            child.setParent(item.getValue().getId());

            /* for folders, iterate all child elements */
            if (child instanceof Folder) {
                updateItemsInternalIds(item.getChildren().get(i));
            }
        }
    }

    public static <T> T cloneItem(T item) {
        try {
            RuntimeTypeAdapterFactory<Authentication> authAdapterFactory = AuthenticationType.getRuntimeTypeAdapterFactory();
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder
                    .setPrettyPrinting()
                    .registerTypeAdapterFactory(getObservableListTypeAdapterFactory())
                    .registerTypeAdapter(SimpleBooleanProperty.class, new SimpleBooleanPropertySerializer())
                    .registerTypeAdapter(SimpleBooleanProperty.class, new SimpleBooleanPropertyDeserializer())
                    .registerTypeAdapterFactory(authAdapterFactory)
                    .create();
            final String json = gson.toJson(item);
            T copy = (T) gson.fromJson(json, item.getClass());
            return copy;
        } catch (Throwable e) {
            ConsoleLogger.error("problem with the request", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Problem");
            alert.setHeaderText(null);
            alert.setContentText("Problem with the request:\n" + e.getMessage());
            alert.showAndWait();
        }
        return null;
    }

    public static String bytesToString(long size) {
        if (size < SIZE_BASE)
            return size + " B";
        int exp = (int) (Math.log(size) / Math.log(SIZE_BASE));
        return String.format("%.1f %s", size / Math.pow(SIZE_BASE, exp), SIZES[exp]);
    }

    /**
     * Reformats JSON string without any validation.
     *
     * @param source un-formatted JSON string
     * @return formatted JSON string
     */
    public static String formatJson(String source) {
        StringBuffer sb = new StringBuffer();
        boolean isRegularChar = false;
        boolean ignoreWhiteSpace = true;
        int indent = 0;
        for (int i = 0; i < source.length(); i++) {
            switch (source.charAt(i)) {
                case '\t':
                case ' ':
                case '\n':
                    if (!ignoreWhiteSpace)
                        sb.append(source.charAt(i));
                    break;
                case ':':
                    if (isRegularChar)
                        sb.append(source.charAt(i));
                    else
                        sb.append(source.charAt(i) + " ");
                    break;
                case '\"':
                    sb.append(source.charAt(i));
                    if (i - 1 >= 0 && source.charAt(i - 1) != '\\') {
                        isRegularChar = !isRegularChar;
                        ignoreWhiteSpace = !ignoreWhiteSpace;
                    }
                    break;
                case '[':
                    if (isRegularChar) {
                        sb.append(source.charAt(i));
                    } else {
                        sb.append(source.charAt(i));
                        indent++;
                        sb.append("\n" + StringUtils.repeat(TAB, indent));
                    }
                    break;
                case '{':
                    sb.append(source.charAt(i));
                    indent++;
                    sb.append("\n" + StringUtils.repeat(TAB, indent));
                    break;
                case ']':
                    if (isRegularChar) {
                        sb.append(source.charAt(i));
                    } else {
                        indent--;
                        sb.append("\n" + StringUtils.repeat(TAB, indent));
                        sb.append(source.charAt(i));
                    }
                    break;
                case '}':
                    indent--;
                    sb.append("\n" + StringUtils.repeat(TAB, indent));
                    sb.append(source.charAt(i));
                    break;
                case ',':
                    sb.append(source.charAt(i));
                    if (!isRegularChar)
                        sb.append("\n" + StringUtils.repeat(TAB, indent));
                    break;
                default:
                    sb.append(source.charAt(i));
                    break;
            }
        }
        return sb.toString();
    }


    /**
     * Compares current version against version retrieved from a project file.
     * Returns 1, if current app version is greater than version from project file,
     * returns 0 if versions are equal
     * returns -1 if current version is smaller than version from project file
     *
     * @param fileVersion
     * @return
     */
    public static int compareVersions(String fileVersion, int currentMajorNumber, int currentMinorNumber) {
        if (StringUtils.isEmpty(fileVersion))
            return 1;
        String[] numbers = StringUtils.splitByWholeSeparator(fileVersion, ".");
        if (numbers.length != 2)
            return 1;
        int fileMajorNumber = 0;
        int fileMinorNumber = 0;
        try {
            fileMajorNumber = Integer.parseInt(numbers[0]);
            fileMinorNumber = Integer.parseInt(numbers[1]);
        } catch (Exception ignore) {
        }
        if (fileMajorNumber == currentMajorNumber) {
            return Integer.compare(currentMinorNumber, fileMinorNumber);
        } else
            return Integer.compare(currentMajorNumber, fileMajorNumber);
    }


    public static void exportRequestAsCurl(Request request) {
        StringBuilder sb = new StringBuilder();
        String urlString = request.getUrl();

        //find current environment
        Optional<Environment> env = project.getEnvironments()
                .parallelStream()
                .filter(e -> e.getUuid().equals(project.getDefaultEnvironmentUuid()))
                .findFirst();
        Environment environment = null;
        if (env.isPresent())
            environment = env.get();

        //replace parameters with values
        if (environment != null) {
            for (Parameter var : environment.getParameters()) {
                urlString = StringUtils.replace(urlString, Constants.PARAM_OPENING_TAG + var.getName() + Constants.PARAM_CLOSING_TAG, var.getValue());
            }
        }

        /* basic url command start */
        sb.append("curl -X ");
        sb.append(request.getHttpMethod());

        /* URL */
        sb.append(" \"");
        sb.append(urlString);
        sb.append("\" ");

        /* headers */
        for (Header h : request.getHeaders()) {
            if (!h.getEnabled())
                continue;
            String hName = replaceWithParams(environment, h.getName());
            String hValue = replaceWithParams(environment, h.getValue());
            sb.append("-H \"" + hName + ":" + hValue + "\" ");
        }

        if (project.getProjectSettings().isFollowRedirects()) {
            sb.append(" --location ");
        }
        if (project.getProjectSettings().isIgnoreSslErrors()) {
            sb.append(" --insecure ");
        }

        /* process Authorization */
        if (request.getAuthentication().getAuthenticationType() == AuthenticationType.BASIC) {
            BasicAuthentication auth = (BasicAuthentication) request.getAuthentication();
            String val = auth.getUsername() + ":" + auth.getPassword();
            String value = Base64.getEncoder().encodeToString(val.getBytes(StandardCharsets.UTF_8));
            sb.append("-H \"Authorization:Basic " + value + "\" ");
        }
        if (request.getAuthentication().getAuthenticationType() == AuthenticationType.BEARER) {
            BearerAuthentication auth = (BearerAuthentication) request.getAuthentication();
            sb.append("-H \"Authorization:Bearer " + auth.getToken() + "\" ");
        }

        /* send POST payload with text content */
        if (request.getHttpMethod() != HttpMethod.GET &&
                request.getHttpMethod() != HttpMethod.HEAD &&
                request.getHttpMethod() != HttpMethod.OPTIONS && request.getPayloadContentType() == PayloadContentType.TEXT) {
            String payload = StringUtils.replace(request.getPostFormRawString(), "\n", "");
            payload = StringUtils.replace(payload, "\\", "\\\\");
            payload = StringUtils.replace(payload, "\"", "\\\"");
            payload = StringUtils.replace(payload, "(", "\\(");
            payload = StringUtils.replace(payload, ")", "\\)");
            sb.append("-d \"" + payload + "\" ");
        }

        /* upload local file */
        if (request.getHttpMethod() != HttpMethod.GET &&
                request.getHttpMethod() != HttpMethod.HEAD &&
                request.getHttpMethod() != HttpMethod.OPTIONS && request.getPayloadContentType() == PayloadContentType.FILE) {
            sb.append(" --upload-file " + request.getPostFormFile().getFieldValue() + " ");
        }

        /* submit form as X_WWW_FORM_URLENCODED */
        if (request.getHttpMethod() != HttpMethod.GET &&
                request.getHttpMethod() != HttpMethod.HEAD &&
                request.getHttpMethod() != HttpMethod.OPTIONS && request.getPayloadContentType() == PayloadContentType.X_WWW_FORM_URLENCODED) {
            List<String> entries = Lists.newArrayList();
            for (PostFormEntry entry : request.getApplicationFormUrlEncodedEntries()) {
                entries.add(entry.getFieldName() + "=" + entry.getFieldValue());
            }
            String content = entries.stream().collect(Collectors.joining("&"));
            sb.append("-d \"" + content + "\" ");
        }

        /* submit form as FORM_DATA */
        if (request.getHttpMethod() != HttpMethod.GET &&
                request.getHttpMethod() != HttpMethod.HEAD &&
                request.getHttpMethod() != HttpMethod.OPTIONS && request.getPayloadContentType() == PayloadContentType.FORM_DATA) {
            for (PostFormEntry entry : request.getMultipartFormDataEntries()) {
                if (entry.getType() == PostFormEntryType.TEXT) {
                    sb.append(" --form \"" + entry.getFieldName() + "=" + entry.getFieldValue() + "\" ");
                } else if (entry.getType() == PostFormEntryType.FILE) {
                    sb.append(" --form \"" + entry.getFieldName() + "=@" + entry.getFieldValue() + "\" ");
                }
            }
        }
        Stage stage = new Stage();
        Parent root;
        FXMLLoader loader = new FXMLLoader(HeadersDialogController.class.getResource("/fxml/CurlExportView.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("problem with ExecutorLogView", e);
        }
        CurlExportViewController controller = loader.getController();
        stage.setScene(new Scene(root));
        stage.setTitle("CURL command");
        stage.initModality(Modality.WINDOW_MODAL);
        controller.setContent(sb.toString());
        controller.setTheme(preferences.getApplicationSettings().getAppTheme());
        stage.show();
    }

    private static String replaceWithParams(Environment env, String text) {
        if (env != null) {
            for (Parameter var : env.getParameters()) {
                text = StringUtils.replace(text, Constants.PARAM_OPENING_TAG + var.getName() + Constants.PARAM_CLOSING_TAG, var.getValue());
            }
        }
        return text;
    }
}
