/*
 * Copyright (C) 2020 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.cli;

import com.wsojka.hammerui.driver.RequestExecutor;
import com.wsojka.hammerui.driver.RequestExecutorFactory;
import com.wsojka.hammerui.dto.*;
import com.wsojka.hammerui.persintence.ProjectFileWrapper;
import com.wsojka.hammerui.persintence.ProjectFileWrapperFactory;
import com.wsojka.hammerui.utils.ConsoleLogger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CliExecutor {

    private ProjectFileWrapper project = ProjectFileWrapperFactory.getInstance();

    private RequestExecutor requestExecutor = RequestExecutorFactory.getInstance();

    public boolean execute(CommandLine line) {
        String projectFile = line.getOptionValue(CliParams.PROJECT_FILE_OPT);
        File file = new File(projectFile);
        boolean loaded = project.loadProject(file);

        if (!loaded) {
            return false;
        }
        if (line.hasOption(CliParams.ENV_OPT)) {
            final String env = line.getOptionValue(CliParams.ENV_OPT);
            Optional<Environment> environment = project.getEnvironments()
                    .stream()
                    .filter(e -> StringUtils.equalsIgnoreCase(e.getName(), env))
                    .findFirst();
            environment.ifPresent(value -> project.setDefaultEnvironmentUuid(value.getUuid()));
        }

        if (line.hasOption(CliParams.PROXY_OPT)) {
            final String prx = line.getOptionValue(CliParams.PROXY_OPT);
            Optional<Proxy> proxy = project.getProxies()
                    .stream()
                    .filter(e -> StringUtils.equalsIgnoreCase(e.getName(), prx))
                    .findFirst();
            proxy.ifPresent(value -> project.setDefaultProxyUuid(value.getUuid()));
        }

        boolean testsPassed = true;
        for (Item item : project.getTreeItems()) {
            if (item instanceof Request) {
                Request request = (Request) item;
                try {
                    ConsoleLogger.info("--------------------------------------------------------------------------------");
                    ConsoleLogger.info("executing request '{}' with URL '{}'", request.getName(), request.getUrl());
                    ResponseDetails response = requestExecutor.execute(request);
                    ConsoleLogger.info("STATUS: HTTP " + response.getReturnCode());
                    if (request.getResponseTestsList() != null && !request.getResponseTestsList().isEmpty()) {
                        boolean results = true;
                        List<Boolean> tests = request.getResponseTestsList()
                                .stream()
                                .map(responseTest -> {
                                    return responseTest.perform(response);
                                })
                                .collect(Collectors.toList());
                        boolean testsResult = tests.stream().allMatch(test -> test);
                        testsPassed = testsPassed && testsResult;
                        ConsoleLogger.info("TESTS: " + (testsResult ? "PASS" : "FAIL"));
                    } else {
                        ConsoleLogger.info("TESTS: none");
                    }
                } catch (Exception e) {
                    ConsoleLogger.error(e.toString());
                }
            }
        }
        return testsPassed;
    }
}
