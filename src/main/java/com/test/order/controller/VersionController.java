package com.test.order.controller;

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class VersionController {
    private static final String APPLICATION = "application";
    private static final String APPLICATION_VERSION = "version";
    private static final String TIMESTAMP = "timestamp";

    @Value("${timestamp}")
    private String timestamp;

    @Value("${project.version}")
    @NotNull(message = "version can't be null")
    private String version;

    @Value("${spring.application.name}")
    private String applicationName;

    @GetMapping("/version")
    public Map<String, String> version() {
        return Map.of(APPLICATION, applicationName, APPLICATION_VERSION, version, TIMESTAMP, timestamp);
    }
}
