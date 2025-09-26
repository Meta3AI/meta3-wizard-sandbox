package br.com.meta3.java.scaffold.api.controllers;

import br.com.meta3.java.scaffold.api.dtos.UserCreateRequest;
import br.com.meta3.java.scaffold.domain.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * REST controller that exposes an endpoint to create users.
 *
 * This controller replaces the legacy form action that performed:
 *  - validation of the UI fields,
 *  - determination of user status (active/inactive),
 *  - collection of selected operator IDs into a CSV,
 *  - invocation of IncluiUsuario(...)
 *
 * Migration decisions and mappings are explained inline as TODO (REVIEW) comments.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Create a new user.
     *
     * Expected behavior (mapped from legacy code):
     *  - Accepts a UserCreateRequest (validation annotations should be on the DTO).
     *  - Delegates creation to UserService.
     *  - Returns:
     *      201 Created on success,
     *      400 Bad Request on validation or known business errors,
     *      500 Internal Server Error on persistence/unexpected errors.
     *
     * Notes:
     *  - The legacy UI assembled a comma-separated list of operator IDs. Here we accept a DTO and
     *    let the service layer perform any necessary translation (CSV <-> collection). This keeps
     *    controller responsibilities minimal.
     */
    @PostMapping
    public ResponseEntity<Object> createUser(@Valid @RequestBody UserCreateRequest request) {
        // TODO: (REVIEW) Legacy code returned a boolean from IncluiUsuario; map that boolean to HTTP statuses.
        // We call the domain/service and assume it returns a boolean indicating success.
        // If the real UserService returns an entity or different type, adapt the handling here.
        boolean created;
        try {
            // TODO: (REVIEW) Legacy front-end concatenated operator IDs into CSV. Ensure the service
            // accepts the DTO shape. If the service expects CSV, conversion should be done in ServiceImpl.
            // userService.createUser may throw IllegalArgumentException for business validation.
            created = userService.createUser(request);
        } catch (IllegalArgumentException ex) {
            // Known validation/business error from service layer -> 400
            // TODO: (REVIEW) Preserve meaningful error messages from service for the client.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        } catch (DataAccessException ex) {
            // Persistence layer error -> 500
            // TODO: (REVIEW) Surface a generic message and avoid leaking internal DB details.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error persisting user data", "detail", ex.getMessage()));
        } catch (Exception ex) {
            // Unexpected error -> 500
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Unexpected error occurred", "detail", ex.getMessage()));
        }

        if (created) {
            // TODO: (REVIEW) Legacy UI showed a modal success message and reset the form. For REST API,
            // return 201 Created with a simple success payload. If needed, Location header can be added
            // when the created resource ID is available from the service.
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "User created successfully"));
        } else {
            // TODO: (REVIEW) Service returned false indicating a creation failure analogous to IncluiUsuario = false.
            // Treat as a server/persistence problem unless service provides more info.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to create user"));
        }
    }
}