package br.com.meta3.java.scaffold.domain.repositories;

import br.com.meta3.java.scaffold.domain.entities.User;

/**
 * Domain-level repository abstraction for User persistence operations.
 * Implementations (infrastructure layer) should provide the actual persistence logic
 * (e.g., JPA/Hibernate).
 *
 * This interface intentionally keeps operations minimal and focussed on the needs
 * migrated from legacy code: saving a user and checking for existence by login.
 */
public interface UserRepository {

    /**
     * Persist a user.
     *
     * Implementations should return the persisted entity (with identifiers populated
     * if generated). Exceptions may be thrown for failure cases and should be handled
     * by application services.
     *
     * @param user the user to persist
     * @return the persisted user
     */
    User save(User user);

    /**
     * Check if a user exists by login.
     *
     * @param login the login to check
     * @return true if a user with the given login exists, false otherwise
     */
    boolean existsByLogin(String login);

    // TODO: (REVIEW) Legacy code aggregated selected operator IDs as a comma-separated string.
    // TODO: (REVIEW) parseOperators(operadorasSelecionadas)
    // TODO: (REVIEW) Decision: Keep parsing/validation in application service layer rather than repository.
}