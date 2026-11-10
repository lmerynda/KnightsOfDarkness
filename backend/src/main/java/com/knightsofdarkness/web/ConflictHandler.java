package com.knightsofdarkness.web;

import java.sql.SQLException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ConflictHandler {
    private static final Logger log = LoggerFactory.getLogger(ConflictHandler.class);
    /** the SQL state of a unique key violation, the same in H2 and PostgreSQL */
    private static final String uniqueViolation = "23505";

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> handleConcurrentChange(ObjectOptimisticLockingFailureException exception)
    {
        log.info("Concurrent change rejected: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Your kingdom changed at the same moment, try again"));
    }

    /**
     * two requests locking the same kingdoms in opposite orders, as two kingdoms buying each other's offers at once: the
     * database rolls one back whole, so its player may simply try again
     */
    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> handleDeadlock(PessimisticLockingFailureException exception)
    {
        log.info("Concurrent change rolled back: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Your kingdom changed at the same moment, try again"));
    }

    /** two players claiming one name at the same moment: the second learns the name is taken once it tries again */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleNameTaken(DataIntegrityViolationException exception)
    {
        // any other broken constraint is a bug, and stays a server error
        if (!(exception.getMostSpecificCause() instanceof SQLException cause && uniqueViolation.equals(cause.getSQLState())))
        {
            throw exception;
        }
        // a warning, so that a key claimed twice anywhere but a name shows in the log instead of passing for a race
        log.warn("Name claimed at the same moment: {}", exception.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Someone claimed that name at the same moment, try again"));
    }
}
