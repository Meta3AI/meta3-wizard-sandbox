package br.com.meta3.java.scaffold.domain.repositories;

import br.com.meta3.java.scaffold.domain.entities.Operator;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Domain-level repository abstraction for Operator persistence and lookups.
 *
 * Responsibilities:
 * - Provide minimal, intention-revealing methods the application/service layer will use
 *   to resolve operatorIds coming from DTOs (e.g. mapping selected operator IDs from the frontend).
 * - Keep this interface free of framework-specific details (no Spring Data annotations).
 *
 * Rationale & migration decisions (see TODOs below):
 * - We intentionally expose methods for bulk lookup by id and by code to support common flows:
 *   - Validate that supplied operatorIds exist and load their domain objects.
 *   - Resolve operators by external code (useful when importing legacy operator references).
 *
 * Note: Implementations live under infrastructure/repositories (e.g. JpaUserRepository-like implementations).
 */
public interface OperatorRepository {

    /**
     * Find an operator by its database id.
     *
     * @param id operator id
     * @return Optional containing the Operator if found, otherwise empty
     */
    Optional<Operator> findById(Long id);

    /**
     * Find an operator by its unique code.
     *
     * @param code unique operator code
     * @return Optional containing the Operator if found, otherwise empty
     */
    Optional<Operator> findByCode(String code);

    /**
     * Find all operators.
     *
     * @return list of all operators (may be empty)
     */
    List<Operator> findAll();

    /**
     * Bulk lookup: find operators whose ids are in the provided collection.
     *
     * This is particularly useful when mapping a collection of operatorIds received from the API
     * into domain entities. Implementations should preserve the returned list order if possible,
     * or document otherwise.
     *
     * @param ids collection of operator ids
     * @return list of operators matching the given ids (may be empty)
     */
    List<Operator> findAllByIdIn(Collection<Long> ids);

    /**
     * Persist or update an operator.
     *
     * @param operator operator to save
     * @return saved operator with any persistence-generated fields populated
     */
    Operator save(Operator operator);

    /**
     * Delete operator by id.
     *
     * @param id operator id to delete
     */
    void deleteById(Long id);

    /**
     * Check existence by id.
     *
     * @param id operator id
     * @return true if an operator with the given id exists
     */
    boolean existsById(Long id);

    // TODO: (REVIEW) Expose minimal repository API to map legacy checklist of operadoras to domain entities
    // OperatorRepository.findAllByIdIn(selectedIds)

    // TODO: (REVIEW) Keep domain abstraction free from Spring Data to allow testing and multiple implementations
    // new JpaOperatorRepository(entityManager)

    // TODO: (REVIEW) Provide findByCode to support legacy systems that reference operators by external codes
    // OperatorRepository.findByCode("LEGACY_CODE_123")
}