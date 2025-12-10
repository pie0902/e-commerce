package org.example.tentrilliondollars.global.exception.controller;

import org.example.tentrilliondollars.global.exception.AccessDeniedException;
import org.example.tentrilliondollars.global.exception.BadRequestException;
import org.example.tentrilliondollars.global.exception.ConflictException;
import org.example.tentrilliondollars.global.exception.NotFoundException;
import org.example.tentrilliondollars.global.exception.UnauthorizedAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler(BadRequestException.class)
    @ResponseBody // 400
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequestException(BadRequestException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    @ResponseBody // 401
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleUnauthorizedUserException(UnauthorizedAccessException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody // 403
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleUnauthorizedProductOwnerException(AccessDeniedException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseBody // 404
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleProductNotFoundException(NotFoundException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseBody // 409
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<Object> handleConflictException(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}
