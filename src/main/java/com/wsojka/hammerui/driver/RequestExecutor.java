/*
 * Copyright (C) 2018 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.driver;

import com.google.common.collect.Lists;
import com.wsojka.hammerui.Constants;
import com.wsojka.hammerui.dto.*;
import com.wsojka.hammerui.dto.authentication.BasicAuthentication;
import com.wsojka.hammerui.dto.authentication.BearerAuthentication;
import com.wsojka.hammerui.dto.payload.PostFormEntry;
import com.wsojka.hammerui.enums.*;
import com.wsojka.hammerui.events.RequestFailedEvent;
import com.wsojka.hammerui.events.ResponseEvent;
import com.wsojka.hammerui.log.InMemoryLog;
import com.wsojka.hammerui.log.InMemoryLogFactory;
import com.wsojka.hammerui.persintence.PreferencesWrapper;
import com.wsojka.hammerui.persintence.PreferencesWrapperFactory;
import com.wsojka.hammerui.persintence.ProjectFileWrapper;
import com.wsojka.hammerui.persintence.ProjectFileWrapperFactory;
import com.wsojka.hammerui.utils.ConsoleLogger;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.fluent.Executor;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RequestExecutor {

    private Map<String, Task<ResponseDetails>> tasks = new HashMap<>();

    private ProjectFileWrapper project = null;

    private PreferencesWrapper preferences = PreferencesWrapperFactory.getInstance();

    private InMemoryLog inMemoryLog = InMemoryLogFactory.getInstance();

    private List<Parameter> variables = new ArrayList<>();

    RequestExecutor() {
    }

    public ResponseDetails execute(Request request) {
        project = ProjectFileWrapperFactory.getInstance();
        return buildAndExecute(request);
    }

    /**
     *
     * @param source source of the request to execute (this will be read-only copy)
     * @param request request to execute (where all parameters repalcement will happen)
     * @param sendButton Node trigerring the request
     */
    public void execute(Request source, Request request, Button sendButton) {
        project = ProjectFileWrapperFactory.getInstance();
        ConsoleLogger.info("running tasks: {} with URL", tasks.toString(), request.getUrl());
        if (tasks.containsKey(request.getId())) {
            ConsoleLogger.info("canceling previous task for request: {}", request.getName());
            tasks.get(request.getId()).cancel();
        }

        Task<ResponseDetails> responseTask = new Task<>() {
            @Override
            protected ResponseDetails call() {
                return buildAndExecute(request);
            }
        };

        responseTask.setOnSucceeded(event -> {
            ConsoleLogger.info("request task completed successfully");
            ResponseEvent responseEvent;
            try {
                responseEvent = new ResponseEvent(source, this, sendButton, request, responseTask.get());
                sendButton.fireEvent(responseEvent);
            } catch (InterruptedException | ExecutionException e) {
                ConsoleLogger.error("problem !!!!!!!!!", e);
            }
            tasks.remove(request.getId());
        });
        responseTask.setOnFailed(event -> {
            Throwable rootCause = event.getSource().getException().getCause();
            while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
                rootCause = rootCause.getCause();
            }
            ConsoleLogger.error("problem with request task: {}", rootCause.getMessage());
            if (event.getSource().getException() != null) {
                RequestFailedEvent requestEvent = new RequestFailedEvent(source, this, sendButton, request, rootCause);
                sendButton.fireEvent(requestEvent);
            }
            tasks.remove(request.getId());
        });
        tasks.put(request.getId(), responseTask);
        Thread th = new Thread(responseTask);
        th.setDaemon(true);
        th.start();
    }

    private ResponseDetails buildAndExecute(Request request) {
        inMemoryLog.clearLog();
        ResponseDetails responseDetails = new ResponseDetails();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(1);
        cm.setDefaultMaxPerRoute(1);

        /* configure request settings */
        //TODO: override request settings with project settings
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        if (request.getRequestSettings().isOverrideProjectSettings()) {
            requestConfigBuilder.setConnectTimeout(request.getRequestSettings().getConnectionTimeout());
            requestConfigBuilder.setSocketTimeout(request.getRequestSettings().getSocketReadTimeout());
            requestConfigBuilder.setRedirectsEnabled(request.getRequestSettings().isFollowRedirects());
        } else {
            requestConfigBuilder.setConnectTimeout(project.getProjectSettings().getConnectionTimeout());
            requestConfigBuilder.setSocketTimeout(project.getProjectSettings().getSocketReadTimeout());
            requestConfigBuilder.setRedirectsEnabled(project.getProjectSettings().isFollowRedirects());
        }

        SocketConfig.Builder socketConfig = SocketConfig.custom();
        socketConfig.setTcpNoDelay(request.getRequestSettings().isUseTcpNodelay());

        /* build client with all the settings */
        HttpClientBuilder clientBuilder = HttpClients
                .custom()
                .setDefaultSocketConfig(socketConfig.build())
                .setDefaultRequestConfig(requestConfigBuilder.build())
                .setConnectionManager(cm);

        /* turn SSL off if requested by user */
        TrustStrategy acceptAll = (cert, authType) -> true;
        SSLConnectionSocketFactory sslSocketFactory = null;
        try {
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptAll).build();
            sslSocketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        } catch (Exception e) {
            ConsoleLogger.error("problem with SSLConnectionSocketFactory");
        }

        if (request.getRequestSettings().isOverrideProjectSettings()) {
            if (request.getRequestSettings().isIgnoreSslErrors()) {
                clientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
                if (sslSocketFactory != null) {
                    clientBuilder.setSSLSocketFactory(sslSocketFactory);
                }
            }
        } else {
            if (project.getProjectSettings().isIgnoreSslErrors()) {
                clientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
                if (sslSocketFactory != null) {
                    clientBuilder.setSSLSocketFactory(sslSocketFactory);
                }
            }
        }


        CloseableHttpClient httpClient = clientBuilder.build();
        try {

            Executor executor = Executor.newInstance(httpClient);

            /* replace placeholders with parameters from selected environment */
            processParameters(request);

            org.apache.http.client.fluent.Request requestSpec;

            switch (request.getHttpMethod()) {
                case GET:
                    requestSpec = org.apache.http.client.fluent.Request.Get(request.getUrl());
                    break;
                case POST:
                    requestSpec = org.apache.http.client.fluent.Request.Post(request.getUrl());
                    break;
                case PUT:
                    requestSpec = org.apache.http.client.fluent.Request.Put(request.getUrl());
                    break;
                case PATCH:
                    requestSpec = org.apache.http.client.fluent.Request.Patch(request.getUrl());
                    break;
                case DELETE:
                    requestSpec = org.apache.http.client.fluent.Request.Delete(request.getUrl());
                    break;
                case HEAD:
                    requestSpec = org.apache.http.client.fluent.Request.Head(request.getUrl());
                    break;
                case OPTIONS:
                    requestSpec = org.apache.http.client.fluent.Request.Options(request.getUrl());
                    break;
                default:
                    throw new IllegalArgumentException("unsupported request type: " + request.getHttpMethod());
            }

            if (request.getRequestSettings().isOverrideProjectSettings()) {
                requestSpec.connectTimeout(request.getRequestSettings().getConnectionTimeout());
                requestSpec.socketTimeout(request.getRequestSettings().getSocketReadTimeout());
            } else {
                requestSpec.connectTimeout(project.getProjectSettings().getConnectionTimeout());
                requestSpec.socketTimeout(project.getProjectSettings().getSocketReadTimeout());
            }

            /* setup proxy details */
            setupProxy(clientBuilder, executor);

            /* add headers defined by the user */
            processCustomHeaders(requestSpec, request);

            /* add authentication details */
            processAuthentication(requestSpec, request, executor);

            /* add POST MultiPart form-data payload */
            processPOSTMultiPartFormDataEntries(requestSpec, request);

            /* add POST x-www-form-urlencoded payload */
            processPOSTFormUrlencoded(requestSpec, request);

            /* setup custom User-Agent header */
            setupCustomUserAgentHeader(requestSpec, request);

            /* add POST file payload */
            processPOSTFile(requestSpec, request);

            /* add POST payload */
            processPOSTPayload(requestSpec, request);

            /* execute request */
            long startTime = System.nanoTime();

            org.apache.http.client.fluent.Response resp = executor.execute(requestSpec);
            org.apache.http.HttpResponse response = resp.returnResponse();

            long estimatedTime = System.nanoTime() - startTime;
            long durationInMs = TimeUnit.MILLISECONDS.convert(estimatedTime, TimeUnit.NANOSECONDS);

            if (response == null) {
                ConsoleLogger.error("problem with response");
                throw new RuntimeException("problem with the response");
            }

            /* return response back to the UI */
            HttpEntity responseEntity = response.getEntity();
            List<Header> headers = Lists.newArrayListWithCapacity(response.getAllHeaders().length);
            for (org.apache.http.Header h : response.getAllHeaders()) {
                headers.add(new Header(h.getName(), h.getValue()));
            }
            if (responseEntity != null) {
                String payload = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
                responseDetails.setPayload(payload);
                responseDetails.setContentLength(responseEntity.getContentLength());
            }
            responseDetails.setReturnCode(response.getStatusLine().getStatusCode());
            responseDetails.setResponseTimeInMs(durationInMs);
            responseDetails.setHeaders(headers);
        } catch (Exception e) {
            ConsoleLogger.error("exception during request/response", e);
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

    private void processCustomHeaders(org.apache.http.client.fluent.Request requestSpec, Request req) {
        req.getHeaders().stream().filter(Header::getEnabled).forEach(header -> requestSpec.addHeader(header.getName(), header.getValue()));
    }

    private void processPOSTMultiPartFormDataEntries(org.apache.http.client.fluent.Request requestSpec, Request req) {
        if (req.getPayloadContentType() != PayloadContentType.FORM_DATA)
            return;
        if (req.getHttpMethod() == HttpMethod.GET ||
                req.getHttpMethod() == HttpMethod.HEAD ||
                req.getHttpMethod() == HttpMethod.OPTIONS)
            return;
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        for (PostFormEntry entry : req.getMultipartFormDataEntries()) {
            if (entry.getType() == PostFormEntryType.TEXT) {
                builder.addTextBody(entry.getFieldName(), entry.getFieldValue());
            } else if (entry.getType() == PostFormEntryType.FILE) {
                File file = new File(entry.getFieldValue());
                if (!file.exists()) {
                    throw new IllegalArgumentException("file not found: " + file.getName());
                }
                builder.addBinaryBody(entry.getFieldName(), file, ContentType.create("application/octet-stream"), file.getName());
            }
        }
        requestSpec.body(builder.build());
    }

    private void processPOSTPayload(org.apache.http.client.fluent.Request requestSpec, Request req) {
        if (req.getPayloadContentType() != PayloadContentType.TEXT)
            return;
        if (req.getHttpMethod() == HttpMethod.GET ||
                req.getHttpMethod() == HttpMethod.HEAD ||
                req.getHttpMethod() == HttpMethod.OPTIONS)
            return;
        StringEntity payloadEntity;
        try {
            payloadEntity = new StringEntity(req.getPostFormRawString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not supported");
        }
        requestSpec.body(payloadEntity);
    }

    private void processPOSTFormUrlencoded(org.apache.http.client.fluent.Request requestSpec, Request req) {
        if (req.getPayloadContentType() != PayloadContentType.X_WWW_FORM_URLENCODED)
            return;
        if (req.getHttpMethod() == HttpMethod.GET ||
                req.getHttpMethod() == HttpMethod.HEAD ||
                req.getHttpMethod() == HttpMethod.OPTIONS)
            return;
        List<NameValuePair> entries = Lists.newArrayList();
        for (PostFormEntry entry : req.getApplicationFormUrlEncodedEntries()) {
            entries.add(new BasicNameValuePair(entry.getFieldName(), entry.getFieldValue()));
        }
        requestSpec.bodyForm(entries);
    }

    private void processPOSTFile(org.apache.http.client.fluent.Request requestSpec, Request req) {
        if (req.getPayloadContentType() != PayloadContentType.FILE)
            return;
        if (req.getHttpMethod() == HttpMethod.GET ||
                req.getHttpMethod() == HttpMethod.HEAD ||
                req.getHttpMethod() == HttpMethod.OPTIONS)
            return;
        File file = new File(req.getPostFormFile().getFieldValue());
        if (!file.exists()) {
            throw new IllegalArgumentException("file not found: " + file.getName());
        }
        String contentTypeText = "text/html";
        if (StringUtils.isNotBlank(req.getPostFormFile().getFieldName()))
            contentTypeText = req.getPostFormFile().getFieldName();
        requestSpec.bodyFile(file, ContentType.parse(contentTypeText));
    }


    private String getHostname(String url) {
        String host = "";
        try {
            host = (new URL(url)).getHost();
        } catch (Exception ignored) {
        }
        return host;
    }

    private int getPort(String url) {
        int port = 80;
        try {
            port = (new URL(url)).getPort();
        } catch (Exception ignored) {
        }
        return port;
    }

    private String getProtocol(String url) {
        String protocol = "http";
        try {
            protocol = (new URL(url)).getProtocol();
        } catch (Exception ignored) {
        }
        return protocol;
    }

    private void processAuthentication(
            org.apache.http.client.fluent.Request requestSpec,
            Request request,
            Executor executor) {
        if (request.getAuthentication().getAuthenticationType() == AuthenticationType.NONE)
            return;

        if (request.getAuthentication().getAuthenticationType() == AuthenticationType.BASIC) {
            String hostname = getHostname(request.getUrl());
            int port = getPort(request.getUrl());
            String protocol = getProtocol(request.getUrl());
            BasicAuthentication auth = (BasicAuthentication) request.getAuthentication();
            executor.auth(new HttpHost(hostname, port, protocol), auth.getUsername(), auth.getPassword());
            executor.authPreemptive(new HttpHost(hostname, port, protocol));
            return;
        }

        if (request.getAuthentication().getAuthenticationType() == AuthenticationType.BEARER) {
            BearerAuthentication auth = (BearerAuthentication) request.getAuthentication();
            requestSpec.addHeader("Authorization", "Bearer " + replace(auth.getToken()));
        }
    }

    private void processParameters(Request req) {
        /* retrieve variables assigned to currently selected environment */

        Optional<Environment> env = project.getEnvironments()
                .parallelStream()
                .filter(e -> e.getUuid().equals(project.getDefaultEnvironmentUuid()))
                .findFirst();
        Environment environment;
        if (env.isPresent())
            environment = env.get();
        else
            return;
        variables = environment.getParameters();

        /* replace variables in all request attributes */
        req.setUrl(replace(req.getUrl()));
        req.setHeaders(req.getHeaders()
                .stream()
                .peek(h -> {
                    h.setName(replace(h.getName()));
                    h.setValue(replace(h.getValue()));
                })
                .collect(Collectors.toCollection(FXCollections::observableArrayList)));

        if (req.getPayloadContentType() == PayloadContentType.TEXT) {
            req.setPostFormRawString(replace(req.getPostFormRawString()));
        } else if (req.getPayloadContentType() == PayloadContentType.FILE) {
            req.getPostFormFile().setFieldName(replace(req.getPostFormFile().getFieldName()));
            req.getPostFormFile().setFieldValue(replace(req.getPostFormFile().getFieldValue()));
        } else if (req.getPayloadContentType() == PayloadContentType.X_WWW_FORM_URLENCODED) {
            req.getApplicationFormUrlEncodedEntries().forEach(e -> {
                e.setFieldName(replace(e.getFieldName()));
                e.setFieldValue(replace(e.getFieldValue()));
            });
        } else if (req.getPayloadContentType() == PayloadContentType.FORM_DATA) {
            req.getMultipartFormDataEntries().forEach(e -> {
                e.setFieldName(replace(e.getFieldName()));
                e.setFieldValue(replace(e.getFieldValue()));
            });
        }
    }

    private void setupProxy(
            HttpClientBuilder clientBuilder,
            Executor executor) {
        Optional<Proxy> prx = project.getProxies()
                .parallelStream()
                .filter(e -> e.getUuid().equals(project.getDefaultProxyUuid()))
                .findFirst();
        Proxy proxy;
        if (prx.isPresent())
            proxy = prx.get();
        else
            return;

        if (proxy.getProxySettingsType() == ProxySettingsType.NONE)
            return;
        if (proxy.getProxySettingsType() == ProxySettingsType.AUTODETECT) {
            //TODO: implement
            return;
        }

        if (proxy.getProxySettingsType() == ProxySettingsType.MANUAL) {
            if (StringUtils.isBlank(proxy.getHostName()))
                throw new IllegalArgumentException("proxy hostname may not be blank");
            HttpHost proxyHost = new HttpHost(proxy.getHostName(), proxy.getPortNumber(), proxy.getManualProxySettingsType().name().toLowerCase());
            DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxyHost);
            if (proxy.isProxyAuthenticationEnabled()) {
                executor.auth(new AuthScope(proxyHost), new UsernamePasswordCredentials(proxy.getProxyUsername(), proxy.getProxyPassword()));
                executor.authPreemptiveProxy(proxyHost);
            }
            clientBuilder.setRoutePlanner(routePlanner);
        }
    }

    private String replace(String text) {
        if (variables.isEmpty() || text == null || text.isEmpty())
            return text;
        for (Parameter var : variables) {
            text = StringUtils.replace(text, Constants.PARAM_OPENING_TAG + var.getName() + Constants.PARAM_CLOSING_TAG, var.getValue());
        }
        return text;
    }

    private void setupCustomUserAgentHeader(org.apache.http.client.fluent.Request requestSpec, Request request) {
        boolean exists = request.getHeaders()
                .parallelStream()
                .anyMatch(header -> StringUtils.equalsIgnoreCase(HTTP.USER_AGENT, header.getName()));
        if (!exists) {
            requestSpec.userAgent(preferences.getApplicationSettings().getUserAgentString());
        }
    }
}
