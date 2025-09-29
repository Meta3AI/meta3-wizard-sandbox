package br.com.meta3.java.scaffold.api.controllers;

import br.com.meta3.java.scaffold.api.dtos.UserCreateRequest;
import br.com.meta3.java.scaffold.domain.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller responsible for user-related HTTP endpoints.
 *
 * Responsibility of this migration:
 * - Accept validated UserCreateRequest (@Valid) instead of relying on UI-side checks.
 * - Convert validation failures into HTTP 400 responses with field-level messages.
 * - Forward valid requests to the application layer (UserService).
 *
 * Notes:
 * - The legacy code performed UI-level validation (focus setting, concatenated error string).
 *   That behavior is migrated to server-side using jakarta.validation annotations on the DTO
 *   and a structured error response produced by this controller.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Creates a new user.
     *
     * - Validates the incoming payload using @Valid and the constraints declared in UserCreateRequest.
     * - On validation errors, a structured map of field->message is returned with HTTP 400.
     * - On success, forwards the request to UserService and returns HTTP 201 Created.
     */
    @PostMapping
    public ResponseEntity<Void> createUser(@Valid @RequestBody UserCreateRequest request) {
        // TODO: (REVIEW) Migrate legacy UI validation (focus + concatenated errors) into server-side DTO validation
        // UserCreateRequest validation is performed by @Valid and jakarta.validation annotations on the DTO
        userService.create(request);

        // TODO: (REVIEW) Return 201 Created without body to mimic a simple create endpoint and keep responses minimal
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Handles validation errors produced by @Valid on request bodies.
     *
     * Returns a JSON object where keys are field names and values are human readable messages.
     * This maps legacy concatenated error messages into a structured API-friendly format.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // TODO: (REVIEW) Convert binding result errors into a map to provide field-specific messages to API clients
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = (error instanceof FieldError) ? ((FieldError) error).getField() : error.getObjectName();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    /**
     * Generic fallback for unexpected exceptions to avoid leaking internal details.
     * In a larger project this would be centralized in a @ControllerAdvice.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleOtherExceptions(Exception ex) {
        // TODO: (REVIEW) Provide a minimal, non-sensitive error response for unexpected exceptions
        Map<String, String> body = new HashMap<>();
        body.put("error", "Internal server error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}