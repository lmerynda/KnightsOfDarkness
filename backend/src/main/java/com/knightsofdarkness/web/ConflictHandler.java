package com.knightsofdarkness.web;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ConflictHandler {
    private static final Logger log = LoggerFactory.getLogger(ConflictHandler.class);

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> handleConcurrentChange(ObjectOptimisticLockingFailureException exception)
    {
        log.info("Concurrent change rejected: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Your kingdom changed at the same moment, try again"));
    }
}
