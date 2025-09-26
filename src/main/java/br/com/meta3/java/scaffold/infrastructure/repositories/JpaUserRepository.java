package br.com.meta3.java.scaffold.infrastructure.repositories;

import br.com.meta3.java.scaffold.domain.entities.User;
import br.com.meta3.java.scaffold.domain.repositories.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/*
 TODO: (REVIEW) The legacy code performed user inclusion from a form, building fields like 'status' and a
 comma-separated list of operator IDs before calling IncluiUsuario(...). That UI-specific assembly is intentionally
 NOT migrated to the repository layer. Instead, the application service layer should build a User entity and
 call this repository.save(user) to persist it.
 
 TODO: (REVIEW) This interface extends both the domain abstraction (UserRepository) and Spring Data's JpaRepository.
 Doing so allows the application layer to depend on the domain-level interface while Spring provides the JPA
 implementation at runtime.
 
 TODO: (REVIEW) The legacy logic validated uniqueness (e.g. by login) and displayed messages. Here we expose a
 derived query findByLogin to assist the application service in performing existence/uniqueness checks before saving.
 Additional derived queries or @Query methods should be added as needed (for example, findByCpf or findByOperators).
*/
@Repository
public interface JpaUserRepository extends UserRepository, JpaRepository<User, Long> {

    // TODO: (REVIEW) Derived query to locate a user by login. This supports checks that mirror the legacy form's
    // "Ed_Login" uniqueness/lookup behavior. If the User entity uses a different property name, adjust accordingly.
    Optional<User> findByLogin(String login);

    // NOTE: JpaRepository already exposes standard methods like save, findById, delete, findAll, etc.
    // Application services should use those methods via the domain UserRepository abstraction.
}