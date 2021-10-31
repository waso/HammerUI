/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.controllers;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wsojka.hammerui.Constants;
import com.wsojka.hammerui.controllers.payload.ApplicationFormUrlencodedEntryController;
import com.wsojka.hammerui.controllers.payload.MultipartFormDataEntryController;
import com.wsojka.hammerui.driver.RequestExecutor;
import com.wsojka.hammerui.driver.RequestExecutorFactory;
import com.wsojka.hammerui.dto.*;
import com.wsojka.hammerui.dto.authentication.Authentication;
import com.wsojka.hammerui.dto.editor.TextSelection;
import com.wsojka.hammerui.dto.payload.PostFormEntry;
import com.wsojka.hammerui.enums.*;
import com.wsojka.hammerui.enums.autocomplete.HttpHeader;
import com.wsojka.hammerui.enums.autocomplete.MimeType;
import com.wsojka.hammerui.events.*;
import com.wsojka.hammerui.persintence.*;
import com.wsojka.hammerui.service.AutocompleteService;
import com.wsojka.hammerui.service.AutocompleteServiceFactory;
import com.wsojka.hammerui.service.UrlHistoryService;
import com.wsojka.hammerui.service.UrlHistoryServiceFactory;
import com.wsojka.hammerui.tests.ResponseTest;
import com.wsojka.hammerui.tests.TestType;
import com.wsojka.hammerui.ui.EditorSearch;
import com.wsojka.hammerui.utils.ConsoleLogger;
import com.wsojka.hammerui.utils.UIUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import netscape.javascript.JSObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.wsojka.hammerui.persintence.ObservableListTypeAdapterFactory.getObservableListTypeAdapterFactory;


public class RequestViewController {

    private enum UserActionMode {ADD, EDIT}

    private static double[] lastDividerPositions;
    private static double[] initialDividerPositions;
    private static double[] horizontalPositions;
    private static boolean splitPaneIsDisabled = false;

    static {
        lastDividerPositions = new double[]{0.3};
        initialDividerPositions = new double[]{0.3};
        horizontalPositions = new double[]{0.5};
    }

    private Request request;

    private ResponseDetails responseDetails;

    private String formattedResponse = null;

    private ProjectChangeListener changeListener = ProjectChangeListenerFactory.getInstance();

    private AutocompleteService autocompleteService = AutocompleteServiceFactory.getInstance();

    private PreferencesWrapper preferences = PreferencesWrapperFactory.getInstance();

    private UrlHistoryService urlHistoryService = UrlHistoryServiceFactory.getInstance();

    private ProjectFileWrapper project = ProjectFileWrapperFactory.getInstance();

    @FXML
    private Button sendButton;

    @FXML
    private ComboBox<UrlEntry> url;

    @FXML
    private HBox urlParent;

    @FXML
    private GridPane gridPane;

    @FXML
    private TabPane optionsPane;

    @FXML
    private ChoiceBox<HttpMethod> httpMethodsList;

    @FXML
    private ToggleButton requestHeadersButton;

    @FXML
    private ToggleButton requestAuthButton;

    @FXML
    private ToggleButton requestPayloadButton;

    @FXML
    private ToggleButton requestHeadersButton1;

    @FXML
    private ToggleButton requestAuthButton1;

    @FXML
    private ToggleButton requestPayloadButton1;

    @FXML
    private ChoiceBox<AuthenticationType> authenticationList;

    @FXML
    private SplitPane splitPane;

    @FXML
    private Pane authenticationDetailsPane;

    @FXML
    private ToggleButton responsePayloadButton;

    @FXML
    private TabPane requestBodyOptions;

    @FXML
    private TableView<PostFormEntry> appFormUrlencodedDataTable;

    @FXML
    private TableView<PostFormEntry> multipartFormDataTable;

    @FXML
    private WebView postRawString;

    @FXML
    private TextField fileFieldName;

    @FXML
    private TextField filePath;

    @FXML
    private TextFlow statusMessage;

    @FXML
    private TabPane responseTabs;

    @FXML
    private WebView responseBody;

    @FXML
    private TableView<Header> responseHeaders;

    @FXML
    private ToggleButton formatButton;

    @FXML
    private ToggleButton wrapButton;

    @FXML
    private AnchorPane rootPane;

    @FXML
    private TextField responseSearchText;

    @FXML
    private Label searchItemCountLabel;

    @FXML
    private Button copyPostRawStringButton;

    @FXML
    private Button pastePostRawStringButton;

    @FXML
    private Button formatPostRawStringButton;

    @FXML
    private Button undoPostRawStringButton;

    @FXML
    private Button redoPostRawStringButton;

    @FXML
    private Button cutPostRawStringButton;

    @FXML
    private HBox statusMessageWrapper;

    @FXML
    private VBox middleBar;

    @FXML
    private TableView<ResponseTest> responseTests;

    @FXML
    private RowConstraints fixedRow;

    @FXML
    private HBox fixedHbox;

    @FXML
    private HBox floatingHbox;

    @FXML
    private ChoiceBox<PayloadContentType> requestPayloadType;
    
    @FXML
    private GridPane headersView;

    private TextArea longUrl;

    private long searchItemCount = 0;

    private ObservableList<Header> responseHeadersList = FXCollections.observableArrayList();

    private ObservableList<ResponseTest> responseTestsList = FXCollections.observableArrayList();

    private ObservableList<PostFormEntry> appFormUrlencodedData = FXCollections.observableArrayList();

    private ObservableList<PostFormEntry> multipartFormData = FXCollections.observableArrayList();

    private RequestExecutor requestExecutor = RequestExecutorFactory.getInstance();

    private EditorSearch editorSearch = new EditorSearch();

    private double splitPaneMouseDelta = 0.0;

    private long resizeTimestamp = 0L;

    private ProgressBar progressBar;

    private Stage testsViewStage;

    private ResponseTestViewController testViewController;

    private UrlEntry originalUrlEntry;

    private boolean isUndoActive;

    public Request getRequest() {
        return request;
    }

    public void setViewOrientation(Orientation orientation) {
        splitPane.setOrientation(orientation);
        if (orientation == Orientation.HORIZONTAL) {
            splitPane.setDividerPositions(horizontalPositions);
            fixedRow.setMaxHeight(0);
            fixedHbox.setVisible(false);
            floatingHbox.setVisible(true);
            floatingHbox.setMaxHeight(Control.USE_COMPUTED_SIZE);
            floatingHbox.setMinHeight(Control.USE_COMPUTED_SIZE);
        } else if (orientation == Orientation.VERTICAL) {
            splitPane.setDividerPositions(horizontalPositions);
            fixedRow.setMaxHeight(Control.USE_COMPUTED_SIZE);
            fixedHbox.setVisible(true);
            floatingHbox.setVisible(false);
            floatingHbox.setMaxHeight(0);
            floatingHbox.setMinHeight(0);
        }
    }

    public void setRequest(Request request) {
        this.request = request;

        /* cleanup headers view from any crap */
        while(headersView.getRowConstraints().size() > 0){
            headersView.getRowConstraints().remove(0);
        }
        while(headersView.getColumnConstraints().size() > 0){
            headersView.getColumnConstraints().remove(0);
        }
        /* update headers view */
        this.request.getHeaders().forEach(h -> addHeaderEntry(h));

        /* deep copy multi part form data from "request" to "multipartFormData" list */
        multipartFormData.clear();
        multipartFormData.addAll(request.getMultipartFormDataEntries());

        /* deep copy multi part form data from "request" to "appFormUrlencodedData" list */
        appFormUrlencodedData.clear();
        appFormUrlencodedData.addAll(request.getApplicationFormUrlEncodedEntries());

        /* deep copy multi part form data from "request" to "appFormUrlencodedData" list */
        responseTestsList.clear();
        this.request
                .getResponseTestsList()
                .forEach(t -> {
                    responseTestsList.add(new ResponseTest(t.getId(), t.getType(), t.getParams().toArray(new String[0])));
                });
    }

    public void initialize() {
        longUrl = new TextArea();
        longUrl.getStylesheets().add(getClass().getResource("/css/url_textarea.css").toExternalForm());
        httpMethodsList.setItems(FXCollections.observableArrayList());
        httpMethodsList.getItems().setAll(HttpMethod.values());
        httpMethodsList.getSelectionModel().selectFirst();
        httpMethodsList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // switching from POST/PUT http methods into something else should disable "PAYLOAD" button
            // and move user away from "Payload" options tab to something else (e.g. Authentication options)
            ConsoleLogger.info("http method changed from {} to {}", oldValue, newValue);
            if (newValue != request.getHttpMethod())
                changeListener.setChanged();
            request.setHttpMethod(newValue);
        });

        requestPayloadType.setItems(FXCollections.observableArrayList());
        requestPayloadType.getItems().setAll(PayloadContentType.values());
        requestPayloadType.getSelectionModel().selectFirst();
        requestPayloadType.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == request.getPayloadContentType())
                return;
            ConsoleLogger.info("payload type changed from {} to {}", oldValue, newValue);
            if (newValue == PayloadContentType.TEXT)
                showPayloadTypeText(null);
            else if (newValue == PayloadContentType.FILE)
                showPayloadTypeFile(null);
            else if (newValue == PayloadContentType.X_WWW_FORM_URLENCODED)
                showPayloadTypeFormUrlencoded(null);
            else if (newValue == PayloadContentType.FORM_DATA)
                showPayloadTypeFormData(null);
            changeListener.setChanged();
            request.setPayloadContentType(newValue);

            if (newValue == PayloadContentType.TEXT) {
                copyPostRawStringButton.setVisible(true);
                pastePostRawStringButton.setVisible(true);
                cutPostRawStringButton.setVisible(true);
                formatPostRawStringButton.setVisible(true);
                undoPostRawStringButton.setVisible(true);
                redoPostRawStringButton.setVisible(true);
            } else {
                copyPostRawStringButton.setVisible(false);
                pastePostRawStringButton.setVisible(false);
                cutPostRawStringButton.setVisible(false);
                formatPostRawStringButton.setVisible(false);
                undoPostRawStringButton.setVisible(false);
                redoPostRawStringButton.setVisible(false);
            }
        });

        /* authentication setup */
        authenticationList.setItems(FXCollections.observableArrayList());
        authenticationList.getItems().setAll(AuthenticationType.getSupportedValues());
        authenticationList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            ConsoleLogger.info("authentication method changed from {} to {}", oldValue, newValue);
            setAuthenticationForm(newValue);
        });

        /* hide split pane if it was disabled in another tab */
        if (RequestViewController.splitPaneIsDisabled) {
            splitPane.setDividerPositions(0.0, 1.0);
        } else {
            splitPane.setDividerPositions(initialDividerPositions);
        }

        responsePayloadButton.getToggleGroup().selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            /* ignore event if user clicks on already selected payload type */
            if (newValue == null && oldValue != null) {
                ConsoleLogger.debug("selected the same button");
                oldValue.setSelected(true);
            }
        });

        requestHeadersButton.getToggleGroup().selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            /* ignore event if user clicks on already selected payload type */
            if (newValue == null && oldValue != null) {
                ConsoleLogger.debug("selected the same button");
                oldValue.setSelected(true);
            }
        });

        requestHeadersButton1.getToggleGroup().selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            /* ignore event if user clicks on already selected payload type */
            if (newValue == null && oldValue != null) {
                ConsoleLogger.debug("selected the same button");
                oldValue.setSelected(true);
            }
        });

        /* POST payload UI */
        applicationFormUrlencodedTableConfiguration();
        multiPartFormTableConfiguration();

        /* response view events handlers */
        responseBody.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED)
                findTextInResponse(null, true);
        });
        responseBody.addEventHandler(KeyEvent.ANY, event -> {
            KeyCombination copyOp = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
            if (copyOp.match(event)) {
                event.consume();
                String selectedText = (String) responseBody.getEngine().executeScript("editor.getCopyText()");
                ClipboardContent content = new ClipboardContent();
                content.putString(selectedText);
                Clipboard.getSystemClipboard().setContent(content);
            }
        });

        /* event handler for making request call */
        EventHandler<ResponseEvent> responseEventHandler = event -> {
            ConsoleLogger.info("ResponseEvent received for request {}", event.getRequest().getName());
            autocompleteService.addEntry(new UrlEntry(event.getSource().getUrl(), event.getSource().getHttpMethod(), event.getResponseDetails().getReturnCode()));
            responseDetails = event.getResponseDetails();

            /* reset the state of "format json" and "wrap lines" buttons */
            formatButton.setSelected(false);
            wrapButton.setSelected(false);

            /* format response if it's unformatted */
            long newLineCount = StringUtils.countMatches(responseDetails.getPayload(), "\n");
            String responseStringToShow = responseDetails.getPayload();
            boolean isHtmlContent = responseDetails.getHeaders()
                    .parallelStream()
                    .anyMatch(h -> StringUtils.equalsIgnoreCase(h.getName(), HttpHeader.CONTENT_TYPE.getName()) &&
                            StringUtils.containsIgnoreCase(h.getValue(), MimeType.TEXT_HTML.getName()));
            boolean isXmlContent = responseDetails.getHeaders()
                    .parallelStream()
                    .anyMatch(h -> StringUtils.equalsIgnoreCase(h.getName(), HttpHeader.CONTENT_TYPE.getName()) &&
                            StringUtils.containsIgnoreCase(h.getValue(), MimeType.APPLICATION_XML.getName()));
            boolean isJsonContent = responseDetails.getHeaders()
                    .parallelStream()
                    .anyMatch(h -> StringUtils.equalsIgnoreCase(h.getName(), HttpHeader.CONTENT_TYPE.getName()) &&
                            StringUtils.containsIgnoreCase(h.getValue(), MimeType.APPLICATION_JSON.getName()));
            /* reformat response only in case of JSON content and if response is not formatted */
            if (newLineCount <= 2 && isJsonContent) {
                formatButton.setSelected(true);
                formattedResponse = UIUtils.formatJson(responseDetails.getPayload());
                responseStringToShow = formattedResponse;
            }
            /* load ACE editor with the response  */
            try {
                InputStream in = getClass().getResourceAsStream("/html/response.html");
                StringWriter writer = new StringWriter();
                IOUtils.copy(in, writer, StandardCharsets.UTF_8);
                String template = writer.toString();
                if (isHtmlContent)
                    responseStringToShow = StringEscapeUtils.escapeHtml4(responseStringToShow);
                else if (isXmlContent)
                    responseStringToShow = StringEscapeUtils.escapeXml11(responseStringToShow);
                template = StringUtils.replace(template, "<div id=\"editor\"></div>", "<div id=\"editor\">" + responseStringToShow + "</div>");
                responseBody.getEngine().loadContent(template);
                responseBody.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == Worker.State.SUCCEEDED) {
                        String themeName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, preferences.getApplicationSettings().getEditorTheme().name());
                        responseBody.getEngine().executeScript("set_theme('ace/theme/" + themeName + "')");
                    }
                });
                responseBody.setContextMenuEnabled(false);
                createContextMenu(responseBody);
            } catch (IOException e) {
                ConsoleLogger.error("problem with opening response.html", e);
            }
            statusMessage.getChildren().clear();
            statusMessage.getChildren().addAll(buildStatusMessage(event));
            statusMessageWrapper.getChildren().clear();
            statusMessageWrapper.getChildren().add(statusMessage);
            progressBar.setVisible(false);
            responseHeadersList.clear();
            responseHeadersList.addAll(event.getResponseDetails().getHeaders());
            responseHeaders.refresh();
            if (testViewController != null && testViewController.isVisible()) {
                testViewController.setResponseDetails(event.getResponseDetails());
            }
            for (ResponseTest test : responseTestsList) {
                test.perform(responseDetails);
            }
            //TODO: change request icon and maybe add green/red image depending on tests outcome
            responseTests.refresh();
            editorSearch.calculateNewLines(responseStringToShow);
        };
        sendButton.addEventHandler(ResponseEvent.RESPONSE_EVENT, responseEventHandler);

        /* event handler for exception being thrown during request call */
        EventHandler<RequestFailedEvent> requestFailedEventHandler = event -> {
            ConsoleLogger.info("Request {} failed with exception", event.getRequest().getName());

            Text text;
            if (event.getException() instanceof java.net.ConnectException) {
                text = new Text("Connection refused");
            } else {
                text = new Text(event.getException().getMessage());
            }
            text.setFill(Color.RED);
            text.setText(text.getText());
            statusMessage.getChildren().clear();
            statusMessage.getChildren().add(text);
            statusMessageWrapper.getChildren().clear();
            statusMessageWrapper.getChildren().add(statusMessage);
            progressBar.setVisible(false);
            responseDetails = null;
            for (ResponseTest test : responseTestsList) {
                test.perform(responseDetails);
            }

            loadDefaultResponseViewContent();
        };
        sendButton.addEventHandler(RequestFailedEvent.REQUEST_FAILED_EVENT, requestFailedEventHandler);

        /* setup TableView for response heders */
        responseHeadersTableConfiguration();

        /* load request webview component */
        try {
            InputStream in = getClass().getResourceAsStream("/html/request.html");
            StringWriter writer = new StringWriter();
            IOUtils.copy(in, writer, StandardCharsets.UTF_8);
            String theString = writer.toString();
            postRawString.getEngine().setJavaScriptEnabled(true);
            postRawString.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == Worker.State.SUCCEEDED) {
                    Document theDocument = postRawString.getEngine().getDocument();
                    Element theEditorElement = theDocument.getElementById("editor");
                    theEditorElement.setTextContent(request.getPostFormRawString());
                    String themeName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, preferences.getApplicationSettings().getEditorTheme().name());
                    postRawString.getEngine().executeScript("initeditor()");
                    postRawString.getEngine().executeScript("set_theme('" + themeName + "')");
                }
            });
            postRawString.setContextMenuEnabled(false);
            postRawString.getEngine().loadContent(theString);
            postRawString.getProperties().put("TextInputControlBehavior.disableForwardToParent", true);
            postRawString.addEventHandler(KeyEvent.ANY, event -> {
                KeyCombination copyOp = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
                if (copyOp.match(event)) {
                    event.consume();
                    String selectedText = (String) postRawString.getEngine().executeScript("editor.getCopyText()");
                    ClipboardContent content = new ClipboardContent();
                    content.putString(selectedText);
                    Clipboard.getSystemClipboard().setContent(content);
                }

                KeyCombination cutOp = new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN);
                if (cutOp.match(event)) {
                    ConsoleLogger.info("....doing cut...");
                    String selectedText = (String) postRawString.getEngine().executeScript("editor.getCopyText()");
                    ClipboardContent content = new ClipboardContent();
                    content.putString(selectedText);
                    Clipboard.getSystemClipboard().setContent(content);
                }
                if (event.getCode() == KeyCode.F11 && event.isControlDown() && event.isShortcutDown()) {
                    event.consume();//consume() does not work here
                    onSendButtonClick(null);
                    //throw new RuntimeException("canceling key event");
                }
            });
            postRawString.setOnKeyReleased(event -> {
                KeyCombination pasteOp = new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN);
                KeyCombination cutOp = new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN);

                if (event.getCode().isDigitKey() ||
                        event.getCode().isLetterKey() ||
                        event.getCode().isWhitespaceKey() ||
                        event.getCode() == KeyCode.BACK_SPACE ||
                        event.getCode() == KeyCode.DELETE ||
                        pasteOp.match(event) ||
                        cutOp.match(event)) {
                    String payload = (String) postRawString.getEngine().executeScript("editor.getValue()");
                    ConsoleLogger.info("POST text has changed to {}", StringUtils.substring(payload, 0, 1024).replace("\n", ""));
                    request.setPostFormRawString(payload);
                    changeListener.setChanged();
                }
            });
        } catch (Exception e) {
            //TODO: add proper error handling
            ConsoleLogger.error("ignore");
        }

        /* load response webview component */
        loadDefaultResponseViewContent();

        /* response search */
        responseSearchText.textProperty().addListener((observable, oldValue, newValue) -> findTextInResponse(null, false));

        /* handle resizing split pane by hand */
        middleBar.setOnMousePressed(event -> {
            /* ignore split pane resize in horizontal orientation */
            if (splitPane.getOrientation() == Orientation.HORIZONTAL) {
                event.consume();
                return;
            }
            splitPaneMouseDelta = event.getY();
            middleBar.setCursor(Cursor.MOVE);
        });
        middleBar.setOnMouseReleased(event -> {
            splitPaneMouseDelta = 0.0;
            middleBar.setCursor(Cursor.V_RESIZE);
        });
        middleBar.setOnMouseDragged(event -> {
            /* ignore mouse dragged events if it's less than 30 milliseconds since the last one */
            if ((System.currentTimeMillis() - resizeTimestamp) < 30) {
                event.consume();
                return;
            }
            /* ignore split pane resize in horizontal orientation */
            if (splitPane.getOrientation() == Orientation.HORIZONTAL) {
                event.consume();
                return;
            }
            double current = splitPane.getDividerPositions()[0];
            double currentSplitPos = splitPane.getBoundsInLocal().getMaxY() * current;
            currentSplitPos = currentSplitPos + event.getY() - splitPaneMouseDelta;
            double newPos = currentSplitPos / splitPane.getBoundsInLocal().getMaxY();
            if (newPos > 0.0 && newPos < 1.0) {
                splitPane.setDividerPositions(newPos);
                resizeTimestamp = System.currentTimeMillis();
            }
        });
        middleBar.setCursor(Cursor.V_RESIZE);

        /* initialize progress bar visible to the user during request execution */
        progressBar = new ProgressBar();
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        progressBar.setMinWidth(150);
        progressBar.setPrefWidth(150);
        progressBar.setMaxWidth(150);

        /* setup table view for response tests */
        responseTestsTableConfiguration();

        /* handle split pane orientation change */
        EventHandler<SplitPaneOrientationChangeEvent> splitPaneOrientationChangeEvent = event -> {
            ConsoleLogger.info("changing orientation from " + splitPane.getOrientation() + " into " + event.getOrientation());
            splitPane.setOrientation(event.getOrientation());
            if (event.getOrientation() == Orientation.HORIZONTAL) {
                splitPane.setDividerPositions(horizontalPositions);
                fixedRow.setMaxHeight(0);
                fixedHbox.setVisible(false);
                floatingHbox.setVisible(true);
                floatingHbox.setMaxHeight(Control.USE_COMPUTED_SIZE);
                floatingHbox.setMinHeight(Control.USE_COMPUTED_SIZE);
            } else if (event.getOrientation() == Orientation.VERTICAL) {
                splitPane.setDividerPositions(horizontalPositions);
                fixedRow.setMaxHeight(Control.USE_COMPUTED_SIZE);
                fixedHbox.setVisible(true);
                floatingHbox.setVisible(false);
                floatingHbox.setMaxHeight(0);
                floatingHbox.setMinHeight(0);
            }
        };
        rootPane.addEventHandler(SplitPaneOrientationChangeEvent.SPLIT_PANE_ORIENTATION_CHANGE_EVENT_EVENT_TYPE, splitPaneOrientationChangeEvent);

        /* update autocomplete list */
        EventHandler<AutocompleteEntryAddedEvent> autocompleteEntryAddedEvent = event -> {
            int caretPos = url.getEditor().getCaretPosition();
            HttpMethod method = request.getHttpMethod();
            String urlStr = request.getUrl();
            url.setItems(event.getEntries());
            url.getSelectionModel().select(new UrlEntry(urlStr, method));
            url.getEditor().positionCaret(caretPos);
        };
        rootPane.addEventHandler(AutocompleteEntryAddedEvent.AUTOCOMPLETE_ENTRY_ADDED_EVENT_TYPE, autocompleteEntryAddedEvent);

        /* handle editor theme change event */
        EventHandler<EditorThemeChangeEvent> editorThemeChangeEvent = event -> {
            String themeName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, event.getThemeName().name());
            postRawString.getEngine().executeScript("set_theme('" + themeName + "')");
            responseBody.getEngine().executeScript("set_theme('" + themeName + "')");
            //refresh UI theme at authentication form
            Platform.runLater(() -> {
                setAuthenticationForm(request.getAuthentication().getAuthenticationType());
            });
        };
        rootPane.addEventFilter(EditorThemeChangeEvent.EDITOR_THEME_CHANGE_EVENT_EVENT_TYPE, editorThemeChangeEvent);

        Platform.runLater(() -> {
            int optionsTabToSelect = preferences.getSelectedRequestOptionsTab(getRequest().getId());
            switchOptionsTabTo(OptionsTab.get(optionsTabToSelect), true);
        });
        headersView.setHgap(5);
        headersView.setVgap(5);

        /* set headersView column constrains */
        ColumnConstraints col0 = new ColumnConstraints();
        col0.setHgrow(Priority.NEVER);
        col0.setMaxWidth(30);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        col1.setMaxWidth(Double.MAX_VALUE);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        col2.setMaxWidth(Double.MAX_VALUE);

        ColumnConstraints col3 = new ColumnConstraints();
        col3.setHgrow(Priority.NEVER);
        col3.setMaxWidth(30);

        headersView.getColumnConstraints().addAll(col0, col1, col2, col3);
    }


    private int countHeaders() {
        int count = 0;
        for (Node child : headersView.getChildren()) {
            Integer column = GridPane.getColumnIndex(child);
            Integer row = GridPane.getRowIndex(child);
            if (row == null || column == null)
                continue;
            if (column == 0)
                count++;
        }
        return count;
    }

    @FXML
    public void addNewHeaderEntry(ActionEvent event) {
        Header newHeader = new Header();
        this.request.getHeaders().add(newHeader);
        addHeaderEntry(newHeader);
    }
    /**
     *  generate headersRowEntry
     */
    private void addHeaderEntry(Header header) {

        /*  create header key field */
        TextField r3key = new TextField(header.getName());
        r3key.setUserData(header);
        r3key.textProperty().addListener((observable, oldVal, newVal) -> {
            Header h = (Header) r3key.getUserData();
            h.setName(newVal);
            changeListener.setChanged();
        });
        r3key.setPrefWidth(Control.USE_COMPUTED_SIZE);
        r3key.setPrefHeight(25);
        r3key.setDisable(!header.getEnabled());
        GridPane.setHgrow(r3key, Priority.ALWAYS);

        /* create header value field */
        TextField r3val = new TextField(header.getValue());
        r3val.setUserData(header);
        r3val.textProperty().addListener((observable, oldVal, newVal) -> {
            Header h = (Header) r3val.getUserData();
            h.setValue(newVal);
            changeListener.setChanged();
        });
        r3val.setPrefWidth(Control.USE_COMPUTED_SIZE);
        r3val.setPrefHeight(25);
        r3val.setDisable(!header.getEnabled());
        GridPane.setHgrow(r3val, Priority.ALWAYS);

        /* create delete button */
        ImageView delImage = new ImageView(new Image(getClass().getResourceAsStream("/icons-material/outline_clear_black_48dp.png")));
        delImage.setFitHeight(15);
        delImage.setPreserveRatio(true);

        Button delButton = new Button();
        delButton.setUserData(header);
        delButton.setPadding(new Insets(0, 5, 0, 5));
        GridPane.setMargin(delButton, new Insets(0, 5, 0, 0));
        delButton.setMaxWidth(10);
        delButton.setMaxHeight(10);
        delButton.setGraphic(delImage);
        delButton.setOnAction(actionEvent -> {
            Header h = (Header) delButton.getUserData();
            if (h == null) {
                return;
            }
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete");
            alert.setHeaderText(null);
            alert.setContentText(String.format("Delete header %s ?", h.getName() == null ? "" : h.getName()));

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Integer rowIndex = GridPane.getRowIndex(delButton);
                if (rowIndex == null)
                    return;

                /* remove entire row grom grid pane and request headers */
                headersView.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node).equals(rowIndex));
                request.getHeaders().remove(h);

                /* recalculate grid pane indices */
                int counter = 0;
                int rowCounter = 0;
                for (Node node : headersView.getChildren()) {
                    Integer r = GridPane.getRowIndex(node);
                    GridPane.setRowIndex(node, rowCounter);
                    counter++;
                    if (counter % 4 == 0)
                        rowCounter++;
                }
                headersView.getChildren().forEach(node -> {
                    Integer r = GridPane.getRowIndex(node);
                });
                changeListener.setChanged();
            }
        });

        /* create header enabled checkbox */
        CheckBox enabled = new CheckBox();
        enabled.setSelected(header.getEnabled());
        enabled.setUserData(header);
        enabled.selectedProperty().addListener((observable, oldVal, newVal) -> {
            Header h = (Header) enabled.getUserData();
            h.setEnabled(newVal);
            r3key.setDisable(!newVal);
            r3val.setDisable(!newVal);
            changeListener.setChanged();
        });
        enabled.setPadding(new Insets(0, 0, 0, 5));
        int rowIndex = countHeaders();

        /* update headers view */
        headersView.add(enabled, 0, rowIndex);
        headersView.add(r3key, 1, rowIndex);
        headersView.add(r3val, 2, rowIndex);
        headersView.add(delButton, 3, rowIndex);
    }

    private void createContextMenu(WebView webView) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem copyCmd = new MenuItem("Copy");
        copyCmd.setOnAction(e -> {
            String selectedText = (String) webView.getEngine().executeScript("getSelectedText()");
            ClipboardContent content = new ClipboardContent();
            content.putString(selectedText);
            Clipboard.getSystemClipboard().setContent(content);
        });
        contextMenu.getItems().addAll(copyCmd);

        webView.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.SECONDARY)
                contextMenu.show(webView, e.getScreenX(), e.getScreenY());
            else
                contextMenu.hide();
        });
    }

    private void responseHeadersTableConfiguration() {
        responseHeaders.setEditable(true);
        responseHeaders.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        responseHeaders.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            Node source = event.getPickResult().getIntersectedNode();
            while (source != null && !(source instanceof TableRow)) {
                source = source.getParent();
            }
            if (source == null || ((TableRow) source).isEmpty()) {
                responseHeaders.getSelectionModel().clearSelection();
            }
        });
        TableColumn<Header, String> headerNameCol = new TableColumn<>("name");
        headerNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        headerNameCol.setEditable(false);

        headerNameCol.setCellFactory(tc -> {
            TableCell<Header, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };
            return cell;
        });

        TableColumn<Header, String> headerValueCol = new TableColumn<>("value");
        headerValueCol.setCellValueFactory(
                new PropertyValueFactory<>("value")
        );
        headerValueCol.setEditable(false);
        headerValueCol.setCellFactory(tc -> {
            TableCell<Header, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };
            return cell;
        });

        responseHeaders.setItems(responseHeadersList);
        responseHeaders.getColumns().clear();
        responseHeaders.getColumns().addAll(Arrays.asList(headerNameCol, headerValueCol));
        responseHeaders.setFixedCellSize(25);
        responseHeaders.setPlaceholder(new Label(""));

        /* copy header actions */
        MenuItem copyAction = new MenuItem("copy header");
        copyAction.setOnAction((ActionEvent event) -> {
            Header item = responseHeaders.getSelectionModel().getSelectedItem();
            if (item == null)
                return;
            ClipboardContent content = new ClipboardContent();
            content.putString(item.getName() + ":" + item.getValue());
            Clipboard.getSystemClipboard().setContent(content);

        });

        MenuItem copyNameAction = new MenuItem("copy header name");
        copyNameAction.setOnAction((ActionEvent event) -> {
            Header item = responseHeaders.getSelectionModel().getSelectedItem();
            if (item == null)
                return;
            ClipboardContent content = new ClipboardContent();
            content.putString(item.getName());
            Clipboard.getSystemClipboard().setContent(content);
        });

        MenuItem copyValueAction = new MenuItem("copy header value");
        copyValueAction.setOnAction((ActionEvent event) -> {
            Header item = responseHeaders.getSelectionModel().getSelectedItem();
            if (item == null)
                return;
            ClipboardContent content = new ClipboardContent();
            content.putString(item.getValue());
            Clipboard.getSystemClipboard().setContent(content);
        });

        ContextMenu menu = new ContextMenu();
        menu.getItems().addAll(copyAction, copyNameAction, copyValueAction);
        responseHeaders.setContextMenu(menu);
    }


    private void responseTestsTableConfiguration() {
        responseTests.setEditable(true);
        responseTests.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        responseTests.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            Node source = event.getPickResult().getIntersectedNode();
            while (source != null && !(source instanceof TableRow)) {
                source = source.getParent();
            }
            if (source == null || ((TableRow) source).isEmpty()) {
                responseTests.getSelectionModel().clearSelection();
            }
        });
        TableColumn<ResponseTest, String> testColumn = new TableColumn<>("test");
        testColumn.setCellValueFactory(cell -> cell.getValue().testNameProperty());
        testColumn.setEditable(false);
        testColumn.setCellFactory(tc -> {
            TableCell<ResponseTest, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };
            cell.setOnMouseClicked(e -> {
                if (e.getClickCount() >= 2) {
                    editResponseTest(null);
                }
            });
            return cell;
        });

        responseTests.setItems(responseTestsList);
        responseTests.getColumns().clear();
        responseTests.getColumns().addAll(Arrays.asList(testColumn));
        responseTests.setFixedCellSize(25);
        responseTests.setPlaceholder(new Label(""));
    }

    private void applicationFormUrlencodedTableConfiguration() {
        appFormUrlencodedDataTable.setEditable(false);
        appFormUrlencodedDataTable.setPlaceholder(new Label(""));
        appFormUrlencodedDataTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        appFormUrlencodedDataTable.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            Node source = event.getPickResult().getIntersectedNode();
            while (source != null && !(source instanceof TableRow)) {
                source = source.getParent();
            }
            if (source == null || ((TableRow) source).isEmpty()) {
                appFormUrlencodedDataTable.getSelectionModel().clearSelection();
            }
        });
        appFormUrlencodedDataTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                ConsoleLogger.info("editing app form table");
                editFormUrlencodedEntry(null);
            } else if (event.getCode() == KeyCode.DELETE) {
                ConsoleLogger.info("deleting app form table entry");
                deleteFormUrlencodedEntry(null);
            }
        });

        TableColumn<PostFormEntry, String> fieldNameCol = new TableColumn<>("name");
        fieldNameCol.setCellValueFactory(new PropertyValueFactory<>("fieldName"));
        fieldNameCol.setEditable(false);

        fieldNameCol.setCellFactory(tc -> {
            TableCell<PostFormEntry, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };
            cell.setOnMouseClicked(e -> {
                if (e.getClickCount() >= 2) {
                    ConsoleLogger.info("edit app form data table");
                    editFormUrlencodedEntry(null);
                }
            });
            return cell;
        });

        TableColumn<PostFormEntry, String> fieldValueCol = new TableColumn<>("value");
        fieldValueCol.setCellValueFactory(
                new PropertyValueFactory<>("fieldValue")
        );
        fieldValueCol.setEditable(false);
        fieldValueCol.setCellFactory(tc -> {
            TableCell<PostFormEntry, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };
            cell.setOnMouseClicked(e -> {
                if (e.getClickCount() >= 2) {
                    ConsoleLogger.info("editing app form urlencoded data table");
                    editFormUrlencodedEntry(null);
                }
            });
            return cell;
        });
        appFormUrlencodedDataTable.setItems(appFormUrlencodedData);
        appFormUrlencodedDataTable.getColumns().clear();
        appFormUrlencodedDataTable.getColumns().addAll(Arrays.asList(fieldNameCol, fieldValueCol));
        appFormUrlencodedDataTable.setFixedCellSize(25);
    }

    private void multiPartFormTableConfiguration() {
        multipartFormDataTable.setEditable(false);
        multipartFormDataTable.setPlaceholder(new Label(""));
        multipartFormDataTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        multipartFormDataTable.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            Node source = event.getPickResult().getIntersectedNode();
            while (source != null && !(source instanceof TableRow)) {
                source = source.getParent();
            }
            if (source == null || ((TableRow) source).isEmpty()) {
                multipartFormDataTable.getSelectionModel().clearSelection();
            }
        });
        multipartFormDataTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                ConsoleLogger.info("editing app form table");
                editFormDataEntry(null);
            } else if (event.getCode() == KeyCode.DELETE) {
                ConsoleLogger.info("deleting app form table entry");
                deleteMultiFormDataAction(null);
            }
        });

        TableColumn<PostFormEntry, String> fieldNameCol = new TableColumn<>("name");
        fieldNameCol.setCellValueFactory(new PropertyValueFactory<>("fieldName"));
        fieldNameCol.setEditable(false);

        fieldNameCol.setCellFactory(tc -> {
            TableCell<PostFormEntry, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };
            cell.setOnMouseClicked(e -> {
                if (e.getClickCount() >= 2) {
                    ConsoleLogger.info("editing multipart form data table");
                    editFormDataEntry(null);
                }
            });
            return cell;
        });

        TableColumn<PostFormEntry, String> fieldValueCol = new TableColumn<>("value");
        fieldValueCol.setCellValueFactory(
                new PropertyValueFactory<>("fieldValue")
        );
        fieldValueCol.setEditable(false);
        fieldValueCol.setCellFactory(tc -> {
            TableCell<PostFormEntry, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };
            cell.setOnMouseClicked(e -> {
                if (e.getClickCount() >= 2) {
                    ConsoleLogger.info("editing multipart form data table");
                    editFormDataEntry(null);
                }
            });
            return cell;
        });
        multipartFormDataTable.setItems(multipartFormData);
        multipartFormDataTable.getColumns().clear();
        multipartFormDataTable.getColumns().addAll(Arrays.asList(fieldNameCol, fieldValueCol));
        multipartFormDataTable.setFixedCellSize(25);
    }

    private <S extends Authentication> void setAuthenticationForm(AuthenticationType type) {
        Class<S> claz = type.getControllerClass();
        S auth = null;
        Optional<Authentication> au = request
                .getAuthentications()
                .stream()
                .filter(e -> e.getAuthenticationType() == type)
                .findFirst();
        if (request.getAuthentication().getAuthenticationType() == type) {
            auth = claz.cast(request.getAuthentication());
            auth.setAuthenticationDetailsPane(authenticationDetailsPane, preferences.getApplicationSettings().getAppTheme());
            if (!au.isPresent()) {
                request.getAuthentications().add(auth);
            }
        } else {
            if (au.isPresent()) {
                auth = (S) au.get();
            } else {
                try {
                    auth = claz.newInstance();
                } catch (Exception e) {
                    ConsoleLogger.error("problem with creating object of type {}", claz.getSimpleName());
                }
                request.getAuthentications().add(auth);
            }
            auth.setAuthenticationDetailsPane(authenticationDetailsPane, preferences.getApplicationSettings().getAppTheme());
            changeListener.setChanged();
            request.setAuthentication(auth);
        }
    }

    void mapValues() {
        /* set UI elements based on object values */
        longUrl.textProperty().addListener((observable, oldValue, newValue) -> {
            url.getEditor().setText(newValue);
        });
        url.setCellFactory(new Callback<>() {
            @Override
            public ListCell<UrlEntry> call(ListView<UrlEntry> entries) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(UrlEntry item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                            setTooltip(null);
                        } else {
                            HBox hbox0 = new HBox();
                            HBox hbox1 = new HBox();
                            HBox hbox2 = new HBox();
                            HBox hbox3 = new HBox();

                            HBox.setHgrow(hbox0, Priority.NEVER);
                            hbox0.setMinWidth(45);
                            hbox0.setPrefWidth(Control.USE_COMPUTED_SIZE);

                            HBox.setHgrow(hbox1, Priority.ALWAYS);
                            hbox1.setMaxWidth(Double.MAX_VALUE);
                            hbox1.setPrefWidth(Control.USE_PREF_SIZE);
                            hbox3.setMaxWidth(Double.MAX_VALUE);
                            HBox.setHgrow(hbox3, Priority.ALWAYS);

                            Label methodLabel = new Label(item.getHttpMethod().name());
                            methodLabel.setTextFill(Color.web("#00b33c"));
                            Label statusCodeLabel = new Label("");

                            /* include status code into the label only if it's available */
                            if (item.getStatusCode() > 0) {
                                statusCodeLabel.setText(" " + item.getStatusCode() + " ");
                            }

                            /* set status label color based on HTTP code value */
                            if (item.getStatusCode() >= 400) {
                                statusCodeLabel.setTextFill(Color.web("red"));
                            } else {
                                statusCodeLabel.setTextFill(Color.web("#00b33c"));
                            }
                            Label urlLabel = new Label(" " + item.getUrl());

                            /* for requests longer than 120 chars, add tooltip */
                            if (item.getUrl().length() > 120) {
                                Tooltip tooltip = new Tooltip(item.getUrl());
                                tooltip.setWrapText(true);
                                setTooltip(tooltip);
                            }
                            if (preferences.getApplicationSettings().getAppTheme() == AppTheme.Dark)
                                urlLabel.setTextFill(Color.web("white"));
                            else
                                urlLabel.setTextFill(Color.web("black"));

                            hbox0.getChildren().addAll(methodLabel);
                            hbox1.getChildren().addAll(urlLabel);
                            hbox2.getChildren().addAll(statusCodeLabel);
                            hbox3.getChildren().addAll(hbox0, hbox1, hbox2);
                            setGraphic(hbox3);
                        }
                    }
                };
            }
        });
        url.setItems(autocompleteService.getEntries());
        url.setValue(new UrlEntry(request.getUrl(), request.getHttpMethod()));
        httpMethodsList.getSelectionModel().select(request.getHttpMethod());
        requestPayloadType.getSelectionModel().select(request.getPayloadContentType());

        /* map UI changes back to the objects */
        url.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (!StringUtils.equals(request.getUrl(), newValue))
                changeListener.setChanged();
            request.setUrl(newValue);
            if (!isUndoActive)
                urlHistoryService.addUrlEntry(request.getId(), newValue);
        });
        url.setConverter(new StringConverter<>() {
            @Override
            public String toString(UrlEntry entry) {
                if (entry == null) {
                    return null;
                } else {
                    return entry.getUrl();
                }
            }

            @Override
            public UrlEntry fromString(String entry) {
                return new UrlEntry(entry, request.getHttpMethod());
            }
        });
        ComboBoxListViewSkin<UrlEntry> skin = new ComboBoxListViewSkin<>(url);
        skin.getPopupContent().setOnKeyReleased(e -> {
            if (!e.isShiftDown() && e.getCode() == KeyCode.HOME) {
                url.getEditor().positionCaret(0);
                e.consume();
            } else if (!e.isShiftDown() && e.getCode() == KeyCode.END) {
                url.getEditor().positionCaret(url.getEditor().getLength());
                e.consume();
            }
        });
        skin.getPopupContent().setOnMousePressed(event -> {
            originalUrlEntry = null;
        });
        skin.getPopupContent().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                originalUrlEntry = null;
                url.hide();
                e.consume();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                if (originalUrlEntry != null)
                    url.getSelectionModel().select(originalUrlEntry);
                originalUrlEntry = null;
                url.hide();
                e.consume();
            } else if (!e.isShiftDown() && e.getCode() == KeyCode.RIGHT) {
                int pos = url.getEditor().getCaretPosition();
                if (pos < url.getEditor().getLength()) {
                    pos++;
                }
                url.getEditor().positionCaret(pos);
                e.consume();
            } else if (!e.isShiftDown() && e.getCode() == KeyCode.LEFT) {
                int pos = url.getEditor().getCaretPosition();
                if (pos > 0) {
                    pos--;
                }
                url.getEditor().positionCaret(pos);
                e.consume();
            } else if (!e.isShiftDown() && e.getCode() == KeyCode.HOME) {
                url.getEditor().positionCaret(0);
                e.consume();
            } else if (!e.isShiftDown() && e.getCode() == KeyCode.END) {
                url.getEditor().positionCaret(url.getEditor().getLength());
                e.consume();
            }
        });
        skin.getPopupContent().addEventFilter(KeyEvent.ANY, e -> {
            if (!e.isShiftDown() && e.getCode() == KeyCode.HOME) {
                url.getEditor().positionCaret(0);
                e.consume();
            } else if (!e.isShiftDown() && e.getCode() == KeyCode.END) {
                url.getEditor().positionCaret(url.getEditor().getLength());
                e.consume();
            }
        });
        url.setSkin(skin);
        url.getEditor().setOnMouseClicked(event -> {
            Double editorWidth = url.getEditor().getBoundsInLocal().getWidth();
            Text helper = new Text();
            Font ubuntuFont = Font.loadFont(RequestViewController.class.getResource("/fonts/SourceCodePro-Regular.ttf").toExternalForm(), 12);
            helper.setFont(ubuntuFont);
            helper.setText(url.getEditor().getText());
            helper.setWrappingWidth(0);
            helper.setLineSpacing(0);
            double textWidth = Math.ceil(helper.getLayoutBounds().getWidth());
            if (textWidth == 0 || editorWidth == 0)
                return;
            if (textWidth > editorWidth) {
                setupLongUrlTextArea();
                event.consume();
            }
        });
        url.getEditor().setOnKeyPressed(event -> {
            KeyCombination ctrlC = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
            KeyCombination ctrlZ = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN);
            KeyCombination ctrlY = new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN);
            if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN) {
                if (!url.isShowing())
                    url.show();
                event.consume();
            } else if (ctrlZ.match(event)) {
                String val = urlHistoryService.getPreviousUrl(request.getId());
                if (val != null) {
                    isUndoActive = true;
                    url.setValue(new UrlEntry(val, url.getValue().getHttpMethod()));
                    isUndoActive = false;
                }
                ConsoleLogger.debug("restored previous URL to: " + val);
                event.consume();
            } else if (ctrlY.match(event)) {
                String val = urlHistoryService.getNextUrl(request.getId());
                if (val != null) {
                    isUndoActive = true;
                    url.setValue(new UrlEntry(val, url.getValue().getHttpMethod()));
                    isUndoActive = false;
                }
                ConsoleLogger.debug("restored previous URL to: " + val);
                event.consume();
            } else if (ctrlC.match(event)) {
                copyUrlToClipboard(url.getEditor().getSelectedText());
            }
        });
        url.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null)
                httpMethodsList.getSelectionModel().select(newValue.getHttpMethod());
        });
        url.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                int caretPos = url.getEditor().getCaretPosition();
                executeRequest();
                url.getEditor().positionCaret(caretPos);
            } else if (event.getCode() == KeyCode.ESCAPE) {
                int caretPos = url.getEditor().getCaretPosition();
                url.hide();
                url.getEditor().positionCaret(caretPos);
            }
        });
        url.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (!oldValue && newValue) {
                originalUrlEntry = url.getSelectionModel().getSelectedItem();
            } else if (oldValue && !newValue && originalUrlEntry != null) {
                url.getSelectionModel().select(originalUrlEntry);
                originalUrlEntry = null;
            }
        });
        httpMethodsList.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldVal, newVal) -> {
                    HttpMethod method = HttpMethod.valueOf(newVal.toString());
                    if (request.getHttpMethod() != method)
                        changeListener.setChanged();
                    request.setHttpMethod(method);
                });

        fileFieldName.setText(request.getPostFormFile().getFieldName());
        fileFieldName.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(request.getPostFormFile().getFieldName())) {
                ConsoleLogger.info("POST file set to {}", oldValue, newValue);
                request.getPostFormFile().setFieldName(fileFieldName.getText());
            }
        });
        filePath.setText(request.getPostFormFile().getFieldValue());
        filePath.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(request.getPostFormFile().getFieldValue())) {
                ConsoleLogger.info("POST file set to {}", oldValue, newValue);
                request.getPostFormFile().setFieldValue(filePath.getText());
            }
        });

        authenticationList.getSelectionModel().select(request.getAuthentication().getAuthenticationType());

        /* enable "body" button for POST/PUT requests */
        switchRequestBodyOptionsTab(request.getPayloadContentType());

        /* populate POST forms with data */
        appFormUrlencodedDataTable.setItems(appFormUrlencodedData);
        multipartFormDataTable.setItems(multipartFormData);
        updateTableSize(multipartFormData.size(), multipartFormDataTable);
        updateTableSize(appFormUrlencodedData.size(), appFormUrlencodedDataTable);
    }

    void mapEvents() {
        splitPane.getDividers().get(0).positionProperty().addListener((observable, oldValue, newValue) -> {
            OptionsPaneResizeEvent event = new OptionsPaneResizeEvent(this, rootPane, new double[]{newValue.doubleValue()});
            rootPane.fireEvent(event);
        });
    }

    @FXML
    public void showHeadersOptions(ActionEvent event) {
        ConsoleLogger.debug("received event: " + event);
        switchOptionsTabTo(OptionsTab.HEADER, false);
        preferences.setSelectedRequestOptionsTab(getRequest().getId(), OptionsTab.HEADER.getIndex());
    }

    @FXML
    public void showAuthenticationOptions(ActionEvent event) {
        ConsoleLogger.debug("received event: " + event);
        switchOptionsTabTo(OptionsTab.AUTHENTICATION, false);
        preferences.setSelectedRequestOptionsTab(getRequest().getId(), OptionsTab.AUTHENTICATION.getIndex());
    }

    @FXML
    public void showPayloadOptions(ActionEvent event) {
        ConsoleLogger.debug("received event: " + event);
        switchOptionsTabTo(OptionsTab.PAYLOAD, false);
        preferences.setSelectedRequestOptionsTab(getRequest().getId(), OptionsTab.PAYLOAD.getIndex());
    }

    @FXML
    public void showPayloadTypeFormUrlencoded(ActionEvent event) {
        ConsoleLogger.debug("received event: " + event);
        switchRequestBodyOptionsTab(PayloadContentType.X_WWW_FORM_URLENCODED);
    }

    @FXML
    public void showPayloadTypeFormData(ActionEvent event) {
        ConsoleLogger.debug("received event: " + event);
        switchRequestBodyOptionsTab(PayloadContentType.FORM_DATA);
    }

    @FXML
    public void showPayloadTypeText(ActionEvent event) {
        ConsoleLogger.debug("received event: " + event);
        switchRequestBodyOptionsTab(PayloadContentType.TEXT);
    }

    @FXML
    public void showPayloadTypeFile(ActionEvent event) {
        ConsoleLogger.debug("received event: " + event);
        switchRequestBodyOptionsTab(PayloadContentType.FILE);
    }

    @FXML
    public void showResponsePayloadTab(ActionEvent event) {
        ConsoleLogger.debug("received event: " + event);
        switchResponseOptionsTab(ResponseContentType.RESPONSE);
    }

    @FXML
    public void showResponseHeadersTab(ActionEvent event) {
        ConsoleLogger.debug("received event: " + event);
        switchResponseOptionsTab(ResponseContentType.HEADERS);
    }

    @FXML
    public void showResponseTestsTab(ActionEvent event) {
        ConsoleLogger.debug("received event: " + event);
        switchResponseOptionsTab(ResponseContentType.TESTS);
    }

    @FXML
    public void addFormDataEntry(ActionEvent event) {
        ConsoleLogger.debug("multipart form data -> add new entry started");
        editMultiFormData(event, UserActionMode.ADD, "Add entry");
    }

    @FXML
    public void editFormDataEntry(ActionEvent event) {
        ConsoleLogger.debug("multipart form data -> edit entry started");
        editMultiFormData(event, UserActionMode.EDIT, "Edit entry");
    }

    @FXML
    public void addFormUrlencodedEntry(ActionEvent event) {
        ConsoleLogger.debug("add new x-www-form-urlencoded form entry started");
        editAppFormUrlencodedData(event, UserActionMode.ADD, "Add entry");
    }

    @FXML
    public void editFormUrlencodedEntry(ActionEvent event) {
        ConsoleLogger.debug("urlencoded form data -> add new entry started");
        editAppFormUrlencodedData(event, UserActionMode.EDIT, "Edit entry");
    }

    @FXML
    public void deleteFormUrlencodedEntry(ActionEvent event) {
        ConsoleLogger.debug("received event: " + event);
        if (appFormUrlencodedDataTable.getItems().size() == 0 || appFormUrlencodedDataTable.getSelectionModel().getSelectedIndex() < 0)
            return;
        PostFormEntry h = appFormUrlencodedDataTable.getSelectionModel().getSelectedItem();
        ConsoleLogger.debug("deleting post form entry: {}", h);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete");
        alert.setHeaderText(null);
        alert.setContentText(String.format("Delete entry %s ?", h.getFieldName()));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            appFormUrlencodedData.remove(h);
            request.getApplicationFormUrlEncodedEntries().clear();
            request.getApplicationFormUrlEncodedEntries().addAll(appFormUrlencodedData);
            updateTableSize(request.getApplicationFormUrlEncodedEntries().size(), appFormUrlencodedDataTable);
            changeListener.setChanged();
        }
    }

    @FXML
    private void onSendButtonClick(ActionEvent event) {
        ConsoleLogger.debug("received event: " + event);
        if (urlParent.getChildren().size() == 0)
            return;
        /* if user is editing big request via TextArea, then update url with current editor value
         */
        if (urlParent.getChildren().get(0) instanceof TextArea) {
            url.getEditor().setText(((TextArea) urlParent.getChildren().get(0)).getText());
            urlParent.getChildren().clear();
            urlParent.getChildren().add(url);
            url.setVisible(true);
            gridPane.getRowConstraints().get(0).setMaxHeight(35);
        }
        executeRequest();
    }

    private void editMultiFormData(ActionEvent event, UserActionMode mode, String title) {
        PostFormEntry postFormEntry;
        if (mode == UserActionMode.EDIT) {
            if (multipartFormDataTable.getItems().size() == 0 || multipartFormDataTable.getSelectionModel().getSelectedIndex() < 0)
                return;
            postFormEntry = multipartFormDataTable.getSelectionModel().getSelectedItem();
        } else {
            postFormEntry = new PostFormEntry();
        }
        Stage stage = new Stage();
        Parent root;
        FXMLLoader loader = new FXMLLoader(MultipartFormDataEntryController.class.getResource("/fxml/payload/MultipartFormDataEntry.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("problem with MultipartFormDataEntry", e);
        }
        MultipartFormDataEntryController controller = loader.getController();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        if (mode == UserActionMode.EDIT) {
            controller.setEntryType(postFormEntry.getType());
            controller.setEntryName(postFormEntry.getFieldName());
            if (postFormEntry.getType() == PostFormEntryType.TEXT) {
                controller.setEntryValue(postFormEntry.getFieldValue());
            } else {
                controller.setFilePath(postFormEntry.getFieldValue());
            }
        }
        stage.addEventHandler(KeyEvent.KEY_RELEASED, (KeyEvent ev) -> {
            if (KeyCode.ESCAPE == ev.getCode())
                stage.close();
        });
        stage.showAndWait();
        if (controller.wasSubmitted() &&
                controller.getEntryName() != null &&
                !controller.getEntryName().trim().isEmpty()) {
            if (mode == UserActionMode.ADD) {
                postFormEntry = new PostFormEntry();
                postFormEntry.setFieldName(controller.getEntryName());
                postFormEntry.setFieldValue(controller.getEntryValue());
                postFormEntry.setType(controller.getEntryType());
                //TODO: handle file content
                multipartFormData.add(postFormEntry);
                request.getMultipartFormDataEntries().clear();
                request.getMultipartFormDataEntries().addAll(multipartFormData);
                multipartFormDataTable.refresh();
                changeListener.setChanged();
                multipartFormDataTable.prefHeightProperty().setValue(Control.USE_COMPUTED_SIZE);
            } else {
                /* recognize if any value has been changed */
                boolean hasChanged = false;
                if (!StringUtils.equals(postFormEntry.getFieldName(), controller.getEntryName()))
                    hasChanged = true;
                if (postFormEntry.getType() != controller.getEntryType())
                    hasChanged = true;
                if (!StringUtils.equals(postFormEntry.getFieldValue(), controller.getEntryValue()))
                    hasChanged = true;

                postFormEntry.setFieldName(controller.getEntryName());
                postFormEntry.setFieldValue(controller.getEntryValue());
                postFormEntry.setType(controller.getEntryType());
                //TODO: handle file content
                request.getMultipartFormDataEntries().clear();
                request.getMultipartFormDataEntries().addAll(multipartFormData);
                multipartFormDataTable.refresh();
                if (hasChanged)
                    changeListener.setChanged();
            }
        }
    }

    @FXML
    public void deleteMultiFormDataAction(ActionEvent event) {
        if (multipartFormDataTable.getItems().size() == 0 || multipartFormDataTable.getSelectionModel().getSelectedIndex() < 0)
            return;
        PostFormEntry h = multipartFormDataTable.getSelectionModel().getSelectedItem();
        ConsoleLogger.debug("deleting post form entry: {}", h);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete");
        alert.setHeaderText(null);
        alert.setContentText(String.format("Delete entry %s ?", h.getFieldName()));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            multipartFormData.remove(h);
            request.getMultipartFormDataEntries().clear();
            request.getMultipartFormDataEntries().addAll(multipartFormData);
            changeListener.setChanged();
            updateTableSize(request.getMultipartFormDataEntries().size(), multipartFormDataTable);
        }
    }

    private void editAppFormUrlencodedData(ActionEvent event, UserActionMode mode, String title) {
        PostFormEntry h;
        if (mode == UserActionMode.EDIT) {
            if (appFormUrlencodedDataTable.getItems().size() == 0 || appFormUrlencodedDataTable.getSelectionModel().getSelectedIndex() < 0)
                return;
            h = appFormUrlencodedDataTable.getSelectionModel().getSelectedItem();
        } else {
            h = new PostFormEntry();
        }
        Stage stage = new Stage();
        Parent root;
        FXMLLoader loader = new FXMLLoader(ApplicationFormUrlencodedEntryController.class.getResource("/fxml/payload/ApplicationFormUrlencodedEntry.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("problem with PostFormEntry", e);
        }
        ApplicationFormUrlencodedEntryController controller = loader.getController();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        controller.setEntryName(h.getFieldName());
        controller.setEntryValue(h.getFieldValue());
        stage.addEventHandler(KeyEvent.KEY_RELEASED, (KeyEvent ev) -> {
            if (KeyCode.ESCAPE == ev.getCode())
                stage.close();
        });
        stage.showAndWait();
        if (controller.wasSubmitted() && controller.getEntryName() != null && !controller.getEntryName().trim().isEmpty()) {
            if (mode == UserActionMode.ADD) {
                ConsoleLogger.debug("adding new post Urlencoded field: {} = {}", controller.getEntryName(), controller.getEntryValue());
                h = new PostFormEntry();
                h.setFieldName(controller.getEntryName());
                h.setFieldValue(controller.getEntryValue());
                appFormUrlencodedData.add(h);
                request.getApplicationFormUrlEncodedEntries().clear();
                request.getApplicationFormUrlEncodedEntries().addAll(appFormUrlencodedData);
                changeListener.setChanged();
                appFormUrlencodedDataTable.refresh();
                appFormUrlencodedDataTable.prefHeightProperty().setValue(Control.USE_COMPUTED_SIZE);
            } else if (mode == UserActionMode.EDIT) {
                ConsoleLogger.debug("editing post Urlencoded field: {} = {}", controller.getEntryName(), controller.getEntryValue());
                /* recognize if any value has changed */
                boolean hasChanged = false;
                if (!StringUtils.equals(h.getFieldName(), controller.getEntryName()))
                    hasChanged = true;
                if (!StringUtils.equals(h.getFieldValue(), controller.getEntryValue()))
                    hasChanged = true;
                h.setFieldName(controller.getEntryName());
                h.setFieldValue(controller.getEntryValue());
                request.getApplicationFormUrlEncodedEntries().clear();
                request.getApplicationFormUrlEncodedEntries().addAll(appFormUrlencodedData);
                appFormUrlencodedDataTable.refresh();
                if (hasChanged)
                    changeListener.setChanged();
            }
        }
    }

    private void switchRequestBodyOptionsTab(PayloadContentType payloadContentType) {
        for (PayloadContentType tab : PayloadContentType.values()) {
            if (tab == payloadContentType) {
                requestBodyOptions.getTabs().get(tab.getTabIndex()).setDisable(false);
                requestBodyOptions.getSelectionModel().select(tab.getTabIndex());
            } else {
                if (tab.getTabIndex() > requestBodyOptions.getTabs().size() - 1)
                    continue;
                requestBodyOptions.getTabs().get(tab.getTabIndex()).setDisable(true);
            }
        }
    }

    private void switchResponseOptionsTab(ResponseContentType contentType) {
        for (ResponseContentType tab : ResponseContentType.values()) {
            if (tab == contentType) {
                responseTabs.getTabs().get(tab.getTabIndex()).setDisable(false);
                responseTabs.getSelectionModel().select(tab.getTabIndex());
            } else {
                if (tab.getTabIndex() > responseTabs.getTabs().size() - 1)
                    continue;
                responseTabs.getTabs().get(tab.getTabIndex()).setDisable(true);
            }
        }
    }

    /**
     * Switches between options tabs: headers/authentication/payload.
     * disableSplitHide is used only during initialization when the same options tab will be selected
     * by the initialization mechanism and not by the user
     *
     * @param tabToEnable
     * @param disableSplitHide
     */
    private void switchOptionsTabTo(OptionsTab tabToEnable, boolean disableSplitHide) {
        ConsoleLogger.info("switching request '" + request.getName() + "' to tab " + tabToEnable.name());
        OptionsPaneToggleEvent event;
        boolean hide = false;
        if (optionsPane.getSelectionModel().isSelected(tabToEnable.getIndex()) && !splitPaneIsDisabled && !disableSplitHide) {
            hide = true;
            event = new OptionsPaneToggleEvent(this, rootPane, true, splitPane.getDividerPositions());
            rootPane.fireEvent(event);
        } else if (splitPaneIsDisabled) {
            /* show pane */
            if (lastDividerPositions[0] < 0.1 || lastDividerPositions[0] > 0.95) {
                /* if split pane was resized too close to being hidden/invisible then by default restore
                   the pane using 0.3 split instead of something that is almost invisible to the user */
                lastDividerPositions[0] = 0.3;
            }
            event = new OptionsPaneToggleEvent(this, rootPane, false, lastDividerPositions);
            rootPane.fireEvent(event);
        }
        /* switch to provided OptionsTab */
        for (OptionsTab tab : OptionsTab.values()) {
            /* find out which button triggered the change and color it with red border line */
            ToggleButton btn;
            ToggleButton btn1;
            if (tab == OptionsTab.HEADER) {
                btn = requestHeadersButton;
                btn1 = requestHeadersButton1;
            } else if (tab == OptionsTab.AUTHENTICATION) {
                btn = requestAuthButton;
                btn1 = requestAuthButton1;
            } else if (tab == OptionsTab.PAYLOAD) {
                btn = requestPayloadButton;
                btn1 = requestPayloadButton1;
            } else {
                ConsoleLogger.error("invalid option provided");
                continue;
            }
            if (tab == tabToEnable) {
                optionsPane.getTabs().get(tab.getIndex()).setDisable(false);
                optionsPane.getSelectionModel().select(tab.getIndex());

                if (!hide) {
                    btn.setSelected(true);
                    btn1.setSelected(true);
                }
            } else {
                optionsPane.getTabs().get(tab.getIndex()).setDisable(true);
                btn.setSelected(false);
                btn1.setSelected(false);
            }
        }
    }

    public void splitPaneEnable(double[] newDividerPositions, Orientation orientation) {
        splitPane.setDividerPositions(newDividerPositions);
        RequestViewController.splitPaneIsDisabled = false;
        if (orientation == Orientation.HORIZONTAL) {
            fixedRow.setMaxHeight(0);
            fixedHbox.setVisible(false);
            floatingHbox.setVisible(true);
            floatingHbox.setMinHeight(Control.USE_COMPUTED_SIZE);
            floatingHbox.setMaxHeight(Control.USE_COMPUTED_SIZE);
        } else if (orientation == Orientation.VERTICAL) {
            fixedRow.setMaxHeight(Control.USE_PREF_SIZE);
            fixedHbox.setVisible(true);
            floatingHbox.setVisible(false);
            floatingHbox.setMinHeight(0);
            floatingHbox.setMaxHeight(0);
        }
    }

    public void splitPaneDisable(double[] newDividerPositions, Orientation orientation) {
        lastDividerPositions = newDividerPositions;
        splitPane.setDividerPositions(0.0, 1.0);
        RequestViewController.splitPaneIsDisabled = true;
        if (orientation == Orientation.HORIZONTAL) {
            fixedRow.setMaxHeight(Control.USE_COMPUTED_SIZE);
            fixedHbox.setVisible(true);
            floatingHbox.setVisible(false);
            floatingHbox.setMinHeight(0);
            floatingHbox.setMaxHeight(0);
        } else if (orientation == Orientation.VERTICAL) {
            fixedRow.setMaxHeight(Control.USE_COMPUTED_SIZE);
            fixedHbox.setVisible(true);
            floatingHbox.setVisible(false);
            floatingHbox.setMinHeight(0);
            floatingHbox.setMaxHeight(0);
        }
    }

    public void resizeSplitPane(double[] pos) {
        splitPane.setDividerPositions(pos);
        initialDividerPositions = pos;
    }

    @FXML
    public void selectFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        if (request.getPostFormFile() != null &&
                request.getPostFormFile().getFieldValue() != null &&
                Paths.get(request.getPostFormFile().getFieldValue()).toFile().exists()) {
            fileChooser.setInitialDirectory(Paths.get(request.getPostFormFile().getFieldValue()).getParent().toFile());
        }
        fileChooser.setTitle("Select file");
        File file = fileChooser.showOpenDialog(filePath.getScene().getWindow());
        if (file != null) {
            filePath.setText(file.getAbsolutePath());
            request.getPostFormFile().setFieldValue(file.getAbsolutePath());
        }
    }

    @FXML
    public void formatResponse(ActionEvent event) {
        boolean isJsonContent = responseDetails != null && responseDetails.getHeaders()
                .parallelStream()
                .anyMatch(h -> StringUtils.equalsIgnoreCase(h.getName(), HttpHeader.CONTENT_TYPE.getName()) &&
                        StringUtils.containsIgnoreCase(h.getValue(), MimeType.APPLICATION_JSON.getName()));
        if (responseDetails == null ||
                responseDetails.getPayload() == null ||
                responseDetails.getPayload().isEmpty() ||
                !isJsonContent) {
            formatButton.setSelected(false);
            rootPane.requestFocus();
            return;
        }

        if (formatButton.isSelected()) {
            if (StringUtils.isEmpty(formattedResponse))
                formattedResponse = UIUtils.formatJson(responseDetails.getPayload());
            responseBody.getEngine().executeScript("editor.setValue('" + escapeJson(formattedResponse) + "', -1)");
            editorSearch.calculateNewLines(formattedResponse);
            editorSearch.generatePositions(formattedResponse, responseSearchText.getText());
        } else {
            responseBody.getEngine().executeScript("editor.setValue('" + escapeJson(responseDetails.getPayload()) + "', -1)");
            editorSearch.calculateNewLines(responseDetails.getPayload());
            editorSearch.generatePositions(responseDetails.getPayload(), responseSearchText.getText());
        }
        rootPane.requestFocus();
    }

    @FXML
    public void wrapResponse(ActionEvent event) {
        if (responseDetails == null ||
                responseDetails.getPayload() == null ||
                responseDetails.getPayload().isEmpty()) {
            wrapButton.setSelected(false);
            rootPane.requestFocus();
            return;
        }
        if (wrapButton.isSelected()) {
            responseBody.getEngine().executeScript("editor.setOption('wrap', true)");
        } else {
            responseBody.getEngine().executeScript("editor.setOption('wrap', false)");
        }
        rootPane.requestFocus();
    }

    @FXML
    public void copyResponse(ActionEvent event) {
        if (responseDetails == null ||
                responseDetails.getPayload() == null ||
                responseDetails.getPayload().isEmpty()) {
            rootPane.requestFocus();
            return;
        }

        String response;
        if (formatButton.isSelected()) {
            response = formattedResponse;
        } else {
            response = responseDetails.getPayload();
        }
        ClipboardContent content = new ClipboardContent();
        content.putString(response);
        Clipboard.getSystemClipboard().setContent(content);
        rootPane.requestFocus();
    }

    private void executeRequest() {
        try {

            ConsoleLogger.debug("Request headers: ");
            request.getHeaders().forEach(hh -> ConsoleLogger.debug(hh.toString()));

            /* save URL into global autocomplete list */
            if (StringUtils.isNotBlank(request.getUrl())) {
                url.commitValue();
                autocompleteService.addEntry(new UrlEntry(request.getUrl(), request.getHttpMethod()));
            }

            /* clean UI from previous response */
            Text text = new Text("");
            statusMessage.getChildren().clear();
            statusMessage.getChildren().add(text);
            statusMessageWrapper.getChildren().clear();
            statusMessageWrapper.getChildren().add(progressBar);
            progressBar.setVisible(true);
            responseHeadersList.clear();
            responseHeaders.refresh();
            formattedResponse = null;

            /* clone request and send copy to the executor */
            RuntimeTypeAdapterFactory<Authentication> authAdapterFactory = AuthenticationType.getRuntimeTypeAdapterFactory();
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder
                    .setPrettyPrinting()
                    .registerTypeAdapterFactory(getObservableListTypeAdapterFactory())
                    .registerTypeAdapter(SimpleBooleanProperty.class, new SimpleBooleanPropertySerializer())
                    .registerTypeAdapter(SimpleBooleanProperty.class, new SimpleBooleanPropertyDeserializer())
                    .registerTypeAdapterFactory(authAdapterFactory)
                    .create();
            final String json = gson.toJson(request);
            Request request2 = gson.fromJson(json, Request.class);
            Request requestSource = gson.fromJson(json, Request.class);
            requestExecutor.execute(requestSource, request2, sendButton);
        } catch (Throwable e) {
            ConsoleLogger.error("problem with the request", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Problem");
            alert.setHeaderText(null);
            alert.setContentText("Problem with the request:\n" + e.getMessage());
            alert.showAndWait();
        }
    }

    private TextFlow buildStatusMessage(ResponseEvent responseEvent) {
        Color defaultColor = Color.BLACK;
        Color successColor = Color.GREEN;
        Color failureColor = Color.RED;

        if (preferences.getApplicationSettings().getAppTheme() == AppTheme.Dark) {
            defaultColor = successColor = failureColor = Color.WHITE;
        }

        ResponseDetails det = responseEvent.getResponseDetails();
        Text text0 = new Text("HTTP: ");

        text0.setFill(defaultColor);
        text0.setFont(Font.font("Verdana", FontWeight.NORMAL, 12));

        Text text1 = new Text(String.valueOf(det.getReturnCode()));
        if (det.getReturnCode() < 400)
            text1.setFill(successColor);
        else
            text1.setFill(failureColor);
        text1.setFont(Font.font("Verdana", FontWeight.BOLD, 12));

        Text text2 = new Text("   Time: ");
        text2.setFill(defaultColor);
        text2.setFont(Font.font("Verdana", FontWeight.NORMAL, 12));

        Text text3 = new Text(det.getResponseTimeSummary());
        text3.setFill(defaultColor);
        text3.setFont(Font.font("Verdana", FontWeight.BOLD, 12));

        Text text4 = new Text("   Size: ");
        text4.setFill(defaultColor);
        text4.setFont(Font.font("Verdana", FontWeight.NORMAL, 12));

        String sizeStr = UIUtils.bytesToString(det.getContentLength());
        Text text5 = new Text(sizeStr + " ");
        text5.setFill(defaultColor);
        text5.setFont(Font.font("Verdana", FontWeight.BOLD, 12));

        return new TextFlow(text0, text1, text2, text3, text4, text5);
    }

    /* sets table size to 0 if it's empty so we don't have to deal with default placeholder */
    private void updateTableSize(int elements, TableView<PostFormEntry> table) {
        if (elements == 0) {
            table.prefHeightProperty().setValue(0);
            table.minHeightProperty().bind(table.prefHeightProperty());
            table.maxHeightProperty().bind(table.prefHeightProperty());
        } else {
            table.prefHeightProperty().setValue(Control.USE_COMPUTED_SIZE);
        }
    }

    @FXML
    public void responseSearchFindPrevious(ActionEvent event) {
        if (responseDetails == null || StringUtils.isBlank(responseDetails.getPayload()) || StringUtils.isBlank(responseSearchText.getText())) {
            searchItemCountLabel.setText("");
            rootPane.requestFocus();
            return;
        }
        if (searchItemCount == 0) {
            searchItemCountLabel.setText("found 0");
            rootPane.requestFocus();
            return;
        }
        JSObject pos = (JSObject) responseBody.getEngine().executeScript("editor.getCursorPosition();");
        if (pos == null) {
            rootPane.requestFocus();
            return;
        }
        TextSelection selection = editorSearch.getPreviousPos(
                (int) pos.getMember("row"),
                (int) pos.getMember("column"),
                responseSearchText.getText().length());
        if (selection.isFound()) {
            responseBody.getEngine().executeScript("selectText(" +
                    selection.getStartRow() + "," +
                    selection.getStartCol() + "," +
                    selection.getEndRow() + "," +
                    selection.getEndCol() + ")");
            searchItemCountLabel.setText(String.format("found %d / %d", selection.getIndex(), searchItemCount));
        }
        rootPane.requestFocus();
    }

    @FXML
    public void responseSearchFindNext(ActionEvent event) {
        if (responseDetails == null || StringUtils.isBlank(responseDetails.getPayload()) || StringUtils.isBlank(responseSearchText.getText())) {
            searchItemCountLabel.setText("");
            rootPane.requestFocus();
            return;
        }
        if (searchItemCount == 0) {
            searchItemCountLabel.setText("found 0");
            rootPane.requestFocus();
            return;
        }
        JSObject pos = (JSObject) responseBody.getEngine().executeScript("editor.getCursorPosition();");
        if (pos == null) {
            rootPane.requestFocus();
            return;
        }
        TextSelection selection = editorSearch.getNextPos(
                (int) pos.getMember("row"),
                (int) pos.getMember("column"),
                responseSearchText.getText().length());
        if (selection.isFound()) {
            responseBody.getEngine().executeScript("selectText(" +
                    selection.getStartRow() + "," +
                    selection.getStartCol() + "," +
                    selection.getEndRow() + "," +
                    selection.getEndCol() + ")");
            searchItemCountLabel.setText(String.format("found %d / %d", selection.getIndex(), searchItemCount));
        }
        rootPane.requestFocus();
    }

    @FXML
    public void findTextInResponseAction(ActionEvent event) {
        findTextInResponse(event, false);
    }

    public void findTextInResponse(ActionEvent event, boolean showItemCountOnly) {
        if (responseDetails == null || StringUtils.isBlank(responseDetails.getPayload()) || StringUtils.isBlank(responseSearchText.getText())) {
            searchItemCountLabel.setText("");
            return;
        }
        ConsoleLogger.info("search text: " + responseSearchText.getText());
        responseBody.getEngine().executeScript("editor.setValue(editor.getValue(), -1);");
        searchItemCount = editorSearch.generatePositions(getActualResponse(), responseSearchText.getText());
        if (searchItemCount == 0) {
            searchItemCountLabel.setText("found 0");
        } else {
            if (showItemCountOnly) {
                searchItemCountLabel.setText(String.format("found %d", searchItemCount));
            } else {
                Object o = responseBody.getEngine().executeScript("editor.find('" + escapeJson(responseSearchText.getText()) + "', {\n" +
                        "            wrap: false,\n" +
                        "            caseSensitive: false, \n" +
                        "            wholeWord: false,\n" +
                        "            regExp: false,\n" +
                        "            start: 0" +
                        "        })");
                if (StringUtils.equals(o.toString(), "undefined")) {
                    searchItemCount = 0;
                    searchItemCountLabel.setText("found 0");
                } else {
                    searchItemCountLabel.setText(String.format("found %d", searchItemCount));
                }
                ConsoleLogger.info("found " + searchItemCount + " matching elements: " + o.toString());
            }
        }
    }

    @FXML
    /* copy selected string. If nothing is selected, copy entire value from the editor */
    public void copyPostRawString(ActionEvent event) {
        String selectedText = (String) postRawString.getEngine().executeScript("editor.getCopyText()");
        if (selectedText.length() == 0)
            selectedText = (String) postRawString.getEngine().executeScript("editor.getValue()");
        String textToCopy = selectedText;
        Platform.runLater(() -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(textToCopy);
            Clipboard.getSystemClipboard().setContent(content);
        });
    }

    @FXML
    public void pastePostRawString(ActionEvent event) {
        String clipboard = Clipboard.getSystemClipboard().getString();
        postRawString.getEngine().executeScript("editor.insert('" + escapeJson(clipboard) + "')");
    }

    @FXML
    public void cutPostRawString(ActionEvent event) {
        String selectedText = (String) postRawString.getEngine().executeScript("editor.getCopyText()");
        if (selectedText == null || selectedText.length() == 0)
            return;
        postRawString.getEngine().executeScript("editor.insert('')");
        Platform.runLater(() -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(selectedText);
            Clipboard.getSystemClipboard().setContent(content);
        });
    }

    @FXML
    public void formatPostRawString(ActionEvent event) {
        String payload = (String) postRawString.getEngine().executeScript("editor.getValue()");
        String formatted = UIUtils.formatJson(payload);
        postRawString.getEngine().executeScript("editor.setValue('" + escapeJson(formatted) + "', -1)");
        ConsoleLogger.info("POST text has changed to {}", StringUtils.substring(payload, 0, 1024).replace("\n", ""));
        request.setPostFormRawString(formatted);
        changeListener.setChanged();
    }

    @FXML
    public void undoPostRawString(ActionEvent event) {
        postRawString.getEngine().executeScript("editor.undo()");
    }

    @FXML
    public void redoPostRawString(ActionEvent event) {
        postRawString.getEngine().executeScript("editor.redo()");
    }

    private String escapeJson(String json) {
        if (json == null)
            return "";
        return json
                .replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\"", "\\\"")
                .replace("\'", "\\'");
    }

    @FXML
    public void addResponseTest(ActionEvent event) {
        editResponseTest(event, UserActionMode.ADD, "Add test");
    }

    @FXML
    public void editResponseTest(ActionEvent event) {
        editResponseTest(event, UserActionMode.EDIT, "Edit test");
    }

    @FXML
    public void deleteResponseTest(ActionEvent event) {
        if (responseTests.getItems().size() == 0 || responseTests.getSelectionModel().getSelectedIndex() < 0)
            return;
        ResponseTest test = responseTests.getItems().get(responseTests.getSelectionModel().getSelectedIndex());
        ConsoleLogger.debug("deleting response test: {}", test.getDescription());

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete test");
        alert.setHeaderText(null);
        alert.setContentText(String.format("Delete test?\n\n%s", test.getDescription()));
        Button cancelBtn = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelBtn.setDefaultButton(true);
        Button okBtn = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setDefaultButton(false);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            responseTestsList.remove(test);
            responseTests.refresh();
            request.setResponseTestsList(responseTestsList);
            changeListener.setChanged();
        }
    }

    private void loadDefaultResponseViewContent() {
        try {
            InputStream in = getClass().getResourceAsStream("/html/response.html");
            StringWriter writer = new StringWriter();
            IOUtils.copy(in, writer, StandardCharsets.UTF_8);
            String theString = writer.toString();
            responseBody.getEngine().loadContent(theString);
            responseBody.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == Worker.State.SUCCEEDED) {
                    String themeName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, preferences.getApplicationSettings().getEditorTheme().name());
                    responseBody.getEngine().executeScript("set_theme('" + themeName + "')");
                }
            });
        } catch (IOException e) {
            ConsoleLogger.error(e.getMessage());
        }
    }

    private void editResponseTest(ActionEvent event, UserActionMode mode, String title) {
        ResponseTest responseTest;
        if (mode == UserActionMode.EDIT) {
            if (responseTests.getItems().size() == 0 || responseTests.getSelectionModel().getSelectedIndex() < 0)
                return;
            responseTest = responseTests.getSelectionModel().getSelectedItem();
        } else {
            responseTest = new ResponseTest();
            responseTest.setType(TestType.JSONPATH_EXISTS);
            responseTest.setParams(new ArrayList<>());
        }
        /* tests view is a separate window and user can have both tests view and main window open at the same time */
        if (testsViewStage != null && testViewController != null && testsViewStage.isShowing()) {
            testViewController.setResponseDetails(responseDetails);
            testViewController.setResponseTest(responseTest);
            testsViewStage.requestFocus();
        } else {
            testsViewStage = new Stage();
            Parent root;
            FXMLLoader loader = new FXMLLoader(ResponseTestViewController.class.getResource("/fxml/ResponseTestView.fxml"));
            try {
                root = loader.load();
            } catch (IOException e) {
                throw new RuntimeException("problem with ResponseTestView", e);
            }
            testViewController = loader.getController();
            testViewController.setResponseDetails(responseDetails);
            testViewController.setResponseTest(responseTest);
            testsViewStage.setScene(new Scene(root));
            testsViewStage.setTitle(title);
            testsViewStage.initModality(Modality.WINDOW_MODAL);
            testsViewStage.setOnHidden(event1 -> {
                testsViewStage.close();
                testsViewStage = null;
                if (testViewController.wasSubmitted()) {
                    /* collect details from TestViewController and add new test to the list */
                    ResponseTest testInstance = testViewController.getResponseTest();
                    TestType type = testViewController.getTestType().getValue();

                    int count = type.getLabels().size();
                    List<String> params = Lists.newArrayList();
                    for (int i = 0; i < count; i++) {
                        params.add(testViewController.getParams()[i].getText());
                    }
                    ConsoleLogger.debug("test selected: " + type + ", params: " + params);
                    testInstance.setType(type);
                    testInstance.setParams(params);
                    boolean existing = responseTestsList.parallelStream().anyMatch(e -> StringUtils.equals(e.getId(), testInstance.getId()));
                    if (!existing) {
                        responseTestsList.add(testInstance);
                    }
                    /* refresh test results if response is already available */
                    if (responseDetails != null)
                        testInstance.perform(responseDetails);
                    responseTests.refresh();
                    request.setResponseTestsList(responseTestsList);
                    changeListener.setChanged();
                }
                testViewController = null;
            });
            testsViewStage.requestFocus();
            testsViewStage.showAndWait();
        }
    }

    private String getActualResponse() {
        if (formatButton.isSelected() && formattedResponse != null) {
            return formattedResponse;
        } else {
            return responseDetails.getPayload();
        }
    }

    private void setupLongUrlTextArea() {
        longUrl.setText(url.getEditor().getText());
        longUrl.setPrefRowCount(3);
        longUrl.setWrapText(true);
        longUrl.setMaxHeight(80);
        urlParent.getChildren().clear();
        urlParent.getChildren().add(longUrl);
        url.setVisible(false);
        HBox.setHgrow(longUrl, Priority.ALWAYS);
        gridPane.getRowConstraints().get(0).setMaxHeight(80);
        longUrl.setOnKeyPressed(event -> {
            KeyCombination ctrlC = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
            if (event.getCode() == KeyCode.ESCAPE) {
                url.getEditor().setText(longUrl.getText());
                urlParent.getChildren().clear();
                urlParent.getChildren().add(url);
                url.setVisible(true);
                gridPane.getRowConstraints().get(0).setMaxHeight(35);
            } else if (event.getCode() == KeyCode.ENTER) {
                onSendButtonClick(null);
            } else if (ctrlC.match(event)) {
                copyUrlToClipboard(longUrl.getSelectedText());
            }
        });
        longUrl.requestFocus();
    }

    /**
     * Replaces all parameters in URL string, and copy result
     * into user's clipboard
     *
     * @param urlString
     */
    private void copyUrlToClipboard(String urlString) {
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

        //copy to clipboard
        ClipboardContent content = new ClipboardContent();
        content.putString(urlString);
        Clipboard.getSystemClipboard().setContent(content);
    }
}