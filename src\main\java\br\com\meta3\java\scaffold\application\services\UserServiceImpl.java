package br.com.meta3.java.scaffold.application.services;

import br.com.meta3.java.scaffold.api.dtos.UserCreateRequest;
import br.com.meta3.java.scaffold.domain.entities.Operator;
import br.com.meta3.java.scaffold.domain.entities.User;
import br.com.meta3.java.scaffold.domain.repositories.OperatorRepository;
import br.com.meta3.java.scaffold.infrastructure.repositories.JpaUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation responsible for creating Users based on the API DTO.
 *
 * Responsibilities implemented here:
 * - Map API DTO (UserCreateRequest) -> domain entity (User).
 * - Resolve operatorIds using OperatorRepository and attach resolved Operator entities to the User.
 * - Persist the resulting User using the existing JpaUserRepository.
 *
 * Migration notes / decisions are documented inline as TODOs.
 */
@Service
public class UserServiceImpl implements br.com.meta3.java.scaffold.domain.services.UserService {

    private final JpaUserRepository jpaUserRepository;
    private final OperatorRepository operatorRepository;

    public UserServiceImpl(JpaUserRepository jpaUserRepository, OperatorRepository operatorRepository) {
        this.jpaUserRepository = jpaUserRepository;
        this.operatorRepository = operatorRepository;
    }

    /**
     * Create a new user from the provided request.
     *
     * - This method intentionally accepts the API DTO rather than a primitive set of parameters to keep
     *   the controller/service contract simple and align with the migration goal of moving legacy UI
     *   checks into server-side validations and orchestration.
     */
    @Override
    @Transactional
    public void create(UserCreateRequest request) {
        // TODO: (REVIEW) Accepting DTO directly to centralize mapping and avoid duplicating field extraction logic in controllers
        // The controller already validated basic constraints (@NotBlank, class-level constraint), this method focuses on orchestration.
        // mapDtoToDomain(request)
        Objects.requireNonNull(request, "request must not be null");

        // TODO: (REVIEW) Normalize operatorIds to an empty collection when null to simplify downstream logic.
        // normalizeOperatorIds(request)
        List<Long> requestedOperatorIds = request.getOperatorIds() == null ? Collections.emptyList() : request.getOperatorIds();

        // Resolve provided operatorIds into Operator domain entities.
        // TODO: (REVIEW) Use OperatorRepository.findAllByIdIn to perform a bulk lookup instead of calling findById repeatedly.
        // This mirrors the legacy behavior of validating that at least one operator was selected (or mobileSystem true),
        // but here we also ensure that supplied operatorIds correspond to existing domain operators.
        List<Operator> resolvedOperators = requestedOperatorIds.isEmpty()
                ? Collections.emptyList()
                : operatorRepository.findAllByIdIn(new HashSet<>(requestedOperatorIds));

        // TODO: (REVIEW) Validate that all requested operatorIds exist.
        // If some operator ids are missing, fail-fast with an IllegalArgumentException so callers can handle it appropriately.
        // validateResolvedOperators(requestedOperatorIds, resolvedOperators)
        if (!requestedOperatorIds.isEmpty()) {
            Set<Long> foundIds = resolvedOperators.stream()
                    .map(Operator::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            List<Long> missing = requestedOperatorIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .distinct()
                    .collect(Collectors.toList());

            if (!missing.isEmpty()) {
                // TODO: (REVIEW) Throwing IllegalArgumentException to indicate client-supplied operator ids are invalid.
                // In a larger application we would use a domain-specific exception that maps to HTTP 400 via an exception handler.
                // failOnMissingOperators(missing)
                throw new IllegalArgumentException("Operadora(s) não encontrada(s): " + missing);
            }
        }

        // Map DTO -> domain User
        User user = new User();
        // TODO: (REVIEW) Mapping straightforward textual fields from DTO to domain entity preserving legacy messages/semantics.
        // mapBasicFields(request, user)
        user.setLogin(request.getLogin());
        user.setName(request.getName());
        user.setDescription(request.getDescription());

        // TODO: (REVIEW) Preserve mobileSystem flag from DTO onto domain user to reflect legacy "Sistema Mobile" checkbox.
        // setMobileFlag(request, user)
        user.setMobileSystem(request.isMobileSystem());

        // TODO: (REVIEW) Attach resolved operators to the user entity.
        // We convert the list to a Set to avoid duplicates and to reflect a typical association shape between user and operators.
        // attachOperators(user, resolvedOperators)
        if (!resolvedOperators.isEmpty()) {
            user.setOperators(new HashSet<>(resolvedOperators));
        } else {
            user.setOperators(Collections.emptySet());
        }

        // Persist the user using the existing JpaUserRepository implementation.
        // TODO: (REVIEW) Delegate persistence to JpaUserRepository to reuse existing infrastructure layer and keep this class framework-agnostic.
        // Note: JpaUserRepository is expected to handle both insert and update semantics.
        // persistUser(user)
        jpaUserRepository.save(user);

        // TODO: (REVIEW) We intentionally return void to match the simplified controller contract (201 Created without body).
        // Additional behavior such as returning the created resource id or location header can be added later if required.
    }
}