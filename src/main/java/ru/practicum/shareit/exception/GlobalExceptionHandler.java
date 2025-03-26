package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException e) {
        return new ResponseEntity<>(
                new ErrorResponse("Validation Error", e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(ConflictException e) {
        return new ResponseEntity<>(
                new ErrorResponse("Conflict Error", e.getMessage()),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException e) {
        return new ResponseEntity<>(
                new ErrorResponse("Not Found Error", e.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException e) {
        return new ResponseEntity<>(
                new ErrorResponse("Forbidden Error", e.getMessage()),
                HttpStatus.FORBIDDEN
        );
    }
}