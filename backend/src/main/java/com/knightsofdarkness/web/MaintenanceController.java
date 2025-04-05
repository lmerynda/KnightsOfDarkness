package com.knightsofdarkness.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MaintenanceController
{
    private static final Logger log = LoggerFactory.getLogger(MaintenanceController.class);

    @GetMapping("/health")
    ResponseEntity<String> healthCheck()
    {
        log.info("Health check requested");

        return ResponseEntity.ok("{\"result\": \"OK\"}");
    }
}
