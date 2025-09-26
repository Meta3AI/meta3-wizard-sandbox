package br.com.meta3.java.scaffold.domain.services;

import br.com.meta3.java.scaffold.domain.entities.User;

import java.util.List;
import java.util.Objects;

/**
 * Domain service interface declaring user-related operations.
 * Implementations should contain business rules and orchestrate persistence via repositories.
 *
 * Notes on migration decisions are provided as TODO comments and small default helpers to
 * document how legacy UI-driven concepts (status mapping, operator id aggregation) are
 * intended to be handled by the domain. These helpers are intentionally lightweight and
 * intended for review - concrete implementations should live in the application layer.
 */
public interface UserService {

    // TODO: (REVIEW) Using domain entity User instead of API DTO to keep domain boundaries intact
    // The API layer (controllers) should map incoming DTOs to domain entities and call this interface.
    User createUser(User user);

    /**
     * Alternate creation method that explicitly accepts operator ids.
     * This mirrors the legacy acceptance of a comma-separated operator id collection
     * while keeping the domain model explicit about operator relationships.
     *
     * Implementations may associate the provided operatorIds to the created user.
     *
     * @param user        user information (login, name, cpf, description, etc.)
     * @param operatorIds list of operator identifiers to be associated with the user
     * @return created User with identifiers/associations populated
     * @throws IllegalArgumentException when validation fails
     */
    User createUser(User user, List<Long> operatorIds);

    /* -----------------------------------------------------------------------
       Default helper utilities for review and to document legacy-to-domain decisions.
       These are small, review-oriented helpers. Production behavior should be in
       concrete application/service implementations, not driven from these defaults.
       ----------------------------------------------------------------------- */

    // TODO: (REVIEW) Mapping legacy UI status selection to domain status code ('A' active / 'I' inactive)
    // This helper encapsulates the decision to use single-character status codes as in legacy code.
    default String mapStatusFromActiveFlag(Boolean active) {
        // TODO: (REVIEW) Using 'A' for active and 'I' for inactive to preserve legacy semantics
        return (active != null && active) ? "A" : "I";
    }

    // TODO: (REVIEW) Legacy UI concatenated operator ids into a comma-separated string.
    // We represent it here as a list of Longs in the domain API and provide a helper
    // to produce the legacy string if needed for interop with older persistence or services.
    default String joinOperatorIds(List<Long> operatorIds) {
        // TODO: (REVIEW) Joining operator ids with commas to match legacy storage format when needed
        if (operatorIds == null || operatorIds.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Long id : operatorIds) {
            if (id == null) continue;
            if (sb.length() > 0) sb.append(',');
            sb.append(id);
        }
        return sb.toString();
    }

    // TODO: (REVIEW) Simple validation helper placeholder to document where validation should occur.
    // Implementations should replace/extend this with robust jakarta.validation usage.
    default void requireNonNullUser(User user) {
        // TODO: (REVIEW) Basic null-check to preserve intent of legacy ValidaTela before persistence
        Objects.requireNonNull(user, "user must not be null");
    }
}