package com.Ron.tradingApps.exception;

import jakarta.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.io.IOException;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            ResourceNotFoundException.class,
    })
    public ErrorResultDTO handleException(ResourceNotFoundException ex) {
        log.error("[GlobalExceptionHandler] NOT_FOUND error : " + ex.getMessage());
        return new ErrorResultDTO(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            IOException.class
    })
    public ErrorResultDTO handleException(Exception ex) {
        log.error("[GlobalExceptionHandler] BAD_REQUEST error : " + ex.getMessage());
        return new ErrorResultDTO(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({
            DataAccessException.class,
            PersistenceException.class,
    })
    public ErrorResultDTO handleException(RuntimeException ex) {
        log.error("[GlobalExceptionHandler] INTERNAL_SERVER_ERROR error : " + ex.getMessage());
        return new ErrorResultDTO(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({
            UnauthorizedOperationException.class,
    })
    public ErrorResultDTO handleException(UnauthorizedOperationException ex) {
        log.error("[GlobalExceptionHandler] UNAUTHORIZED error : " + ex.getMessage());
        return new ErrorResultDTO(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }
}
