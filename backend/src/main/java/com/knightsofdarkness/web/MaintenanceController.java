package com.knightsofdarkness.web;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MaintenanceController
{
    private volatile boolean ready = false;

    /**
     * the web server answers before the startup seeding has finished, so health stays unavailable until the world is ready
     */
    @EventListener(ApplicationReadyEvent.class)
    void markReady()
    {
        ready = true;
    }

    @GetMapping("/health")
    ResponseEntity<String> healthCheck()
    {
        if (!ready)
        {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("{\"result\": \"STARTING\"}");
        }

        return ResponseEntity.ok("{\"result\": \"OK\"}");
    }
}
