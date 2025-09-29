package br.com.meta3.java.scaffold.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

/**
 * Placeholder domain entity representing an "Operadora" (operator).
 *
 * This entity is intentionally simple: it provides an identity, a unique code and
 * human-friendly fields. It's a starting point to map operator selections that the
 * legacy UI used to present as a checklist. The class is prepared for JPA persistence
 * and kept decoupled from other domain types to avoid introducing cycles prematurely.
 */
@Entity
@Table(name = "operators")
public class Operator {

    // TODO: (REVIEW) Using JPA Entity placeholder for legacy 'operadoras' so the domain model can persist operators
    private static final String TODO_REVIEW_OPERATOR_PLACEHOLDER = "jpa-operator-placeholder";

    // TODO: (REVIEW) Using GenerationType.IDENTITY as a simple auto-increment strategy suitable for H2 and common RDBMS
    private static final String TODO_REVIEW_ID_STRATEGY = "identity-strategy";

    // TODO: (REVIEW) Making 'code' unique to help identify operators from legacy systems (e.g. integration keys)
    private static final String TODO_REVIEW_CODE_UNIQUENESS = "code-unique";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * A short unique code for the operator (e.g. abbreviation or external id).
     * Marked unique and non-null to help deduplicate operators when importing legacy data.
     */
    @Column(nullable = false, unique = true)
    private String code;

    /**
     * Human-friendly operator name.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Optional description for the operator.
     */
    @Column
    private String description;

    // Constructors

    public Operator() {
        // Default constructor required by JPA
    }

    public Operator(Long id, String code, String name, String description) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
    }

    // TODO: (REVIEW) Avoid creating bidirectional associations with User at this stage to prevent tight coupling / circular dependencies
    private static final String TODO_REVIEW_RELATIONSHIP_DECISION = "defer-user-association";

    // Getters and setters

    public Long getId() {
        return id;
    }

    public Operator setId(Long id) {
        this.id = id;
        return this;
    }

    public String getCode() {
        return code;
    }

    public Operator setCode(String code) {
        this.code = code;
        return this;
    }

    public String getName() {
        return name;
    }

    public Operator setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Operator setDescription(String description) {
        this.description = description;
        return this;
    }

    // equals / hashCode based on id (if present) otherwise code to allow stable identity before persistence

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Operator operator = (Operator) o;

        // Prefer DB identity when available
        if (id != null && operator.id != null) {
            return Objects.equals(id, operator.id);
        }

        // Fallback to code to allow comparisons prior to persistence
        return Objects.equals(code, operator.code);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return "Operator{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}