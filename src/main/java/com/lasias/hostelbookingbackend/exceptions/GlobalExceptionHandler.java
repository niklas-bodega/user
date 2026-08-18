package com.lasias.hostelbookingbackend.exceptions;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException exception) {
        log.error("Illegal argument exception: {}", exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exception.getMessage());
    }



    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<String> handleExpiredJwtException(ExpiredJwtException exception) {
        log.error("Expired JWT token: {}", exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Token has expired.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.error("Validation exception: {}", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }


}
