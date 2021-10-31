/*
 * Copyright (C) 2018 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.controllers;

import com.wsojka.hammerui.dto.Proxy;
import com.wsojka.hammerui.dto.Request;
import com.wsojka.hammerui.dto.ResponseDetails;
import com.wsojka.hammerui.enums.AppTheme;
import com.wsojka.hammerui.enums.HttpMethod;
import com.wsojka.hammerui.enums.ManualProxySettingsType;
import com.wsojka.hammerui.enums.ProxySettingsType;
import com.wsojka.hammerui.events.RequestFailedEvent;
import com.wsojka.hammerui.events.ResponseEvent;
import com.wsojka.hammerui.persintence.PreferencesWrapper;
import com.wsojka.hammerui.persintence.PreferencesWrapperFactory;
import com.wsojka.hammerui.utils.ConsoleLogger;
import com.wsojka.hammerui.utils.UIUtils;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.fluent.Executor;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ProxiesViewController {

    private ObservableList<Proxy> proxies = FXCollections.observableArrayList();

    private ObservableList<Proxy> savedProxies = FXCollections.observableArrayList();

    private Proxy selectedProxy;

    private static final PreferencesWrapper preferences = PreferencesWrapperFactory.getInstance();

    private ResourceBundle bundle = ResourceBundle.getBundle("bundles.MainView", preferences.getLocale());

    private boolean submitted = false;

    private BooleanProperty changed = new SimpleBooleanProperty(false);

    @FXML
    private AnchorPane root;

    @FXML
    private TextField proxyName;

    @FXML
    private ListView<Proxy> proxiesList;

    @FXML
    private Button cancelButton;

    @FXML
    private Button submitButton;

    @FXML
    private Button applyButton;

    /* no proxy UI elements */

    @FXML
    private RadioButton noProxy;

    @FXML
    private GridPane proxyDetailsNode;

    /* manual proxy UI elements */

    @FXML
    private RadioButton manualProxy;

    @FXML
    private VBox manualProxyNode;

    @FXML
    private RadioButton manualConfigHttp;

    @FXML
    private RadioButton manualConfighttps;

    @FXML
    private TextField proxyHostname;

    @FXML
    private TextField proxyPort;

    @FXML
    private CheckBox proxyAuthenticationEnabled;

    @FXML
    private GridPane proxyAuthenticationNode;

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private Button testConnectionButton;

    @FXML
    private TextField testUrl;

    @FXML
    private ProgressIndicator testConnectionProgress;

    @FXML
    private Label testConnectionStatus;

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
            proxyName.textProperty().addListener((observable, oldValue, newValue) -> {
                if (selectedProxy == null)
                    return;
                if (newValue == null || selectedProxy.getName() == null ||
                        newValue.equals(selectedProxy.getName()))
                    return;
                ConsoleLogger.info("changing environment name from: {} to: {}", oldValue, newValue);
                selectedProxy.setName(newValue);
                proxiesList.refresh();
                changed.set(true);
            });
            proxiesList.setItems(proxies);
            proxiesList
                    .getSelectionModel()
                    .selectedItemProperty()
                    .addListener((observable, oldValue, newValue) -> {
                        ConsoleLogger.info("selected proxy {}", newValue);
                        selectedProxy = newValue;
                        loadProxyDetails();
                    });
            proxies.addListener((ListChangeListener<Proxy>) (c -> {
                changed.set(true);
                ConsoleLogger.info("proxies list changed");
            }));

            /* listeners to enable/disable options when switching between different proxy types */
            noProxy.selectedProperty().addListener((observable, oldValue, newValue) -> {
                ConsoleLogger.info("noProxy set from {} into {}", oldValue, newValue);
                if (newValue) {
                    manualProxyNode.getChildren().forEach(n -> n.setDisable(true));
                    if (selectedProxy != null) {
                        selectedProxy.setProxySettingsType(ProxySettingsType.NONE);
                    }
                }
            });
            manualProxy.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    manualProxyNode.getChildren().forEach(n -> n.setDisable(false));
                    if (selectedProxy != null) {
                        selectedProxy.setProxySettingsType(ProxySettingsType.MANUAL);
                    }
                }
            });
            manualConfigHttp.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (selectedProxy == null)
                    return;
                selectedProxy.setManualProxySettingsType(ManualProxySettingsType.HTTP);
            });
            manualConfighttps.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (selectedProxy == null)
                    return;
                selectedProxy.setManualProxySettingsType(ManualProxySettingsType.HTTPS);
            });
            proxyHostname.textProperty().addListener((observable, oldValue, newValue) -> {
                if (selectedProxy == null)
                    return;
                selectedProxy.setHostName(newValue);
            });
            proxyPort.textProperty().addListener((observable, oldValue, newValue) -> {
                if (selectedProxy == null)
                    return;
                selectedProxy.setPortNumber(Integer.valueOf(newValue));
            });
            proxyAuthenticationEnabled.selectedProperty().addListener((observable, oldValue, newValue) -> {
                ConsoleLogger.info("proxyAuthenticationEnabled: {} {} {}", observable, oldValue, newValue);
                if (newValue) {
                    proxyAuthenticationNode.getChildren().forEach(n -> n.setDisable(false));
                } else {
                    proxyAuthenticationNode.getChildren().forEach(n -> n.setDisable(true));
                }
                if (selectedProxy != null) {
                    selectedProxy.setProxyAuthenticationEnabled(newValue);
                }
            });
            username.textProperty().addListener((observable, oldValue, newValue) -> {
                if (selectedProxy == null)
                    return;
                selectedProxy.setProxyUsername(newValue);
            });
            password.textProperty().addListener((observable, oldValue, newValue) -> {
                if (selectedProxy == null)
                    return;
                selectedProxy.setProxyPassword(newValue);
            });
            /* open EnvironmentsView window and load details of the environment selected in the main window dropdown */
            if (selectedProxy != null) {
                proxiesList.getSelectionModel().select(selectedProxy);
            } else {
                proxyDetailsNode.getChildren().forEach(e -> e.setDisable(true));
                proxyName.setDisable(true);
            }
        });
        /* event handler for making request call */
        EventHandler<ResponseEvent> responseEventHandler = responseEvent -> {
            ConsoleLogger.info("(Proxy test connection) ResponseEvent received for url {}", responseEvent.getRequest().getUrl());
            testConnectionStatus.setText("Connection successful (HTTP " + responseEvent.getResponseDetails().getReturnCode() + ")");
            testConnectionProgress.setVisible(false);
            testConnectionStatus.setVisible(true);
            PauseTransition delay = new PauseTransition(Duration.seconds(5));
            delay.setOnFinished(event -> testConnectionStatus.setVisible(false));
            delay.play();
        };
        testConnectionButton.addEventHandler(ResponseEvent.RESPONSE_EVENT, responseEventHandler);

        /* event handler for exception being thrown during request call */
        EventHandler<RequestFailedEvent> requestFailedEventHandler = event -> {
            ConsoleLogger.info("(Proxy test connection) Request failed with exception for url ", event.getRequest().getUrl());
            testConnectionProgress.setVisible(false);
            testConnectionStatus.setText(event.getException().getMessage());
            testConnectionStatus.setVisible(true);
            PauseTransition delay = new PauseTransition(Duration.seconds(5));
            delay.setOnFinished(delayEvent -> testConnectionStatus.setVisible(false));
            delay.play();
        };
        testConnectionButton.addEventHandler(RequestFailedEvent.REQUEST_FAILED_EVENT, requestFailedEventHandler);
        testConnectionProgress.setVisible(false);
    }

    public ObservableList<Proxy> getProxies() {
        return proxies;
    }

    public void setProxies(ObservableList<Proxy> proxies) {
        this.proxies = proxies;
    }

    public ObservableList<Proxy> getSavedProxies() {
        return savedProxies;
    }

    public void setSavedProxies(ObservableList<Proxy> savedProxies) {
        this.savedProxies = savedProxies;
    }

    public Proxy getSelectedProxy() {
        return selectedProxy;
    }

    public void setSelectedProxy(Proxy selectedProxy) {
        this.selectedProxy = selectedProxy;
    }

    public void setDefaultProxy(Proxy defaultSelect) {
        if (defaultSelect == null)
            return;
        selectedProxy = defaultSelect;
    }

    public boolean wasSubmitted() {
        return submitted;
    }

    @FXML
    public void addProxy(ActionEvent event) {
        ConsoleLogger.info("adding new proxy");
        String name = UIUtils.getValueFromUser("name", bundle.getString("add_new_proxy_title"));
        if (name == null || name.trim().isEmpty())
            return;
        ConsoleLogger.info("adding new proxy {}", name);
        Proxy p = new Proxy();
        p.setName(name);
        p.setUuid(UUID.randomUUID().toString());
        proxies.add(p);
        proxiesList.setItems(proxies);
        /* select newly added environment */
        proxiesList.getSelectionModel().select(p);
    }

    @FXML
    public void deleteProxy(ActionEvent event) {
        if (proxiesList.getItems().size() == 0 || proxiesList.getSelectionModel().getSelectedIndex() < 0)
            return;
        Proxy env = proxiesList.getSelectionModel().getSelectedItem();
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
            ConsoleLogger.info("deleting proxy {}", env.getName());
            proxies.remove(env);
        }
        if (proxies.isEmpty())
            clearProxyDetails();
    }

    @FXML
    public void cloneProxy(ActionEvent event) {
        if (proxiesList.getItems().isEmpty() || proxiesList.getSelectionModel().getSelectedIndex() < 0)
            return;
        ConsoleLogger.info("cloning proxy " + selectedProxy.getName());

        Proxy p = selectedProxy.copy();
        p.setName(selectedProxy.getName() + " copy");
        p.setUuid(UUID.randomUUID().toString());
        proxies.add(p);
        proxiesList.setItems(proxies);
        /* select newly added environment */
        proxiesList.getSelectionModel().select(p);
    }

    @FXML
    public void submitDialog(ActionEvent event) {
        Stage stage = (Stage) submitButton.getScene().getWindow();
        stage.close();
        submitted = true;
    }

    @FXML
    public void cancelDialog(ActionEvent event) {
        ConsoleLogger.info("canceling proxies dialog");
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
        submitted = false;
    }

    @FXML
    public void applyDialog(ActionEvent event) {
        ConsoleLogger.info("applying changes of proxies list");
        savedProxies.clear();
        savedProxies.addAll(proxies);
        submitted = true;
        changed.set(false);
    }

    private void loadProxyDetails() {
        if (selectedProxy == null)
            return;
        proxyDetailsNode.getChildren().forEach(e -> {
            e.setDisable(false);
        });
        proxyName.setDisable(false);
        proxyName.setText(selectedProxy.getName());

        /* select proxy type: no proxy/auto-detect/manual */
        switch (selectedProxy.getProxySettingsType()) {
            case NONE:
                noProxy.selectedProperty().set(true);
                break;
            case MANUAL:
                manualProxy.selectedProperty().set(true);
                break;
            default:
                noProxy.selectedProperty().set(true);
                break;
        }

        /* load manual proxy settings */
        if (selectedProxy.getManualProxySettingsType() == ManualProxySettingsType.HTTP)
            manualConfigHttp.setSelected(true);
        else
            manualConfighttps.setSelected(true);
        proxyHostname.setText(selectedProxy.getHostName());
        proxyPort.setText(String.valueOf(selectedProxy.getPortNumber()));
        username.setText(selectedProxy.getProxyUsername());
        password.setText(selectedProxy.getProxyPassword());
        if (selectedProxy.isProxyAuthenticationEnabled()) {
            proxyAuthenticationEnabled.setSelected(true);
            proxyAuthenticationNode.getChildren().forEach(c -> c.setDisable(false));
        } else {
            proxyAuthenticationEnabled.setSelected(false);
            proxyAuthenticationNode.getChildren().forEach(c -> c.setDisable(true));
        }
    }

    private void clearProxyDetails() {
        if (!proxies.isEmpty())
            return;
        noProxy.getToggleGroup().getToggles().forEach(toggle -> {
            Node node = (Node) toggle;
            node.setDisable(true);
        });
        proxyName.setText("");
        proxyName.setDisable(true);
    }

    @FXML
    public void testConnection(ActionEvent event) {
        testConnectionProgress.setVisible(true);
        testConnectionStatus.setText("");
        testConnectionStatus.setVisible(false);

        Request request = new Request();
        request.setHttpMethod(HttpMethod.GET);
        if (StringUtils.isBlank(testUrl.getText()))
            return;
        String url;
        if (!StringUtils.startsWithIgnoreCase(testUrl.getText(), "http://") &&
                !StringUtils.startsWithIgnoreCase(testUrl.getText(), "https://")) {
            url = "http://" + testUrl.getText();
        } else {
            url = testUrl.getText();
        }
        request.setUrl(url);
        request.setName(url);

        Task<ResponseDetails> responseTask = new Task<>() {
            @Override
            protected ResponseDetails call() {
                ResponseDetails responseDetails = new ResponseDetails();

                HttpClientBuilder clientBuilder = HttpClients.custom();
                CloseableHttpClient httpClient = clientBuilder.build();
                try {
                    Executor executor = Executor.newInstance(httpClient);
                    org.apache.http.client.fluent.Request requestSpec = org.apache.http.client.fluent.Request.Get(request.getUrl());

                    requestSpec.connectTimeout(30000);
                    requestSpec.socketTimeout(30000);

                    if (selectedProxy.getProxySettingsType() == ProxySettingsType.MANUAL) {
                        HttpHost proxyHost = new HttpHost(selectedProxy.getHostName(), selectedProxy.getPortNumber(), selectedProxy.getManualProxySettingsType().name().toLowerCase());
                        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxyHost);
                        if (selectedProxy.isProxyAuthenticationEnabled()) {
                            executor.auth(new AuthScope(proxyHost), new UsernamePasswordCredentials(selectedProxy.getProxyUsername(), selectedProxy.getProxyPassword()));
                            executor.authPreemptiveProxy(proxyHost);
                        }
                        clientBuilder.setRoutePlanner(routePlanner);
                    }
                    org.apache.http.client.fluent.Response resp = executor.execute(requestSpec);
                    org.apache.http.HttpResponse response = resp.returnResponse();
                    if (response == null) {
                        ConsoleLogger.error("problem with response");
                        throw new RuntimeException("problem with the response");
                    }
                    HttpEntity responseEntity = response.getEntity();
                    if (responseEntity != null) {
                        String payload = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
                        responseDetails.setPayload(payload);
                        responseDetails.setContentLength(responseEntity.getContentLength());
                        ConsoleLogger.debug("proxy test connection response: " + payload);
                    }
                    responseDetails.setReturnCode(response.getStatusLine().getStatusCode());
                    responseDetails.setResponseTimeInMs(0);
                    //TODO: check why httpclient always returns correct response even though proxy setup in invalid
                } catch (Exception e) {
                    ConsoleLogger.error("exception during request/response: {}", e);
                    throw new RuntimeException(e.getMessage(), e);
                } finally {
                    if (httpClient != null) {
                        try {
                            httpClient.close();
                        } catch (IOException e) {
                            ConsoleLogger.error("problem with closing http client", e);
                        }
                    }
                }
                return responseDetails;
            }
        };

        responseTask.setOnSucceeded(successEvent -> {
            ConsoleLogger.info("request task completed successfully");
            ResponseEvent responseEvent;
            try {
                responseEvent = new ResponseEvent(request, this, testConnectionButton, request, responseTask.get());
                testConnectionButton.fireEvent(responseEvent);
            } catch (InterruptedException | ExecutionException e) {
                ConsoleLogger.error("problem !!!!!!!!!", e);
            }
        });
        responseTask.setOnFailed(failureEvent -> {
            ConsoleLogger.error("problem with request task: {}", failureEvent.getSource().getException());
            if (failureEvent.getSource().getException() != null) {
                RequestFailedEvent requestEvent = new RequestFailedEvent(request, this, testConnectionButton, request, failureEvent.getSource().getException());
                testConnectionButton.fireEvent(requestEvent);
            }
        });
        Thread th = new Thread(responseTask);
        th.setDaemon(true);
        th.start();
    }

    public void setTheme(AppTheme theme) {
        if (theme == AppTheme.Dark) {
            root.getStyleClass().add(AppTheme.Dark.getStyleName());
        } else {
            root.getStyleClass().remove(AppTheme.Dark.getStyleName());
        }
    }
}
