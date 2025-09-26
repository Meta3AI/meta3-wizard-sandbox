package br.com.meta3.java.scaffold.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * JPA entity representing a user migrated from legacy system.
 *
 * Mapping decisions:
 * - operadorasCsv stores the list of selected "operadoras" as a comma-separated string,
 *   preserving the legacy behavior where operadoras were concatenated into a CSV.
 * - status stores 'A' (active) or 'I' (inactive) to match legacy semantics.
 *
 * NOTE: Keep this class focused on persistence concerns. Any higher-level business rules
 * or validations beyond simple constraints should be implemented in application/services.
 */
@Entity
@Table(name = "users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    // Primary key with auto-generated value
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Legacy Ed_Login.text -> login
    @NotBlank
    @Size(max = 100)
    @Column(name = "login", nullable = false, length = 100, unique = true)
    private String login;

    // Legacy Ed_NomeDoUsuario.text -> name
    @NotBlank
    @Size(max = 200)
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    // Legacy Ed_cpf.Text -> cpf
    // Keep as plain string but add a simple pattern to ensure digits and common formatting if needed.
    // Validation rules can be extended in service layer if stricter checks (like checksum) are required.
    @NotBlank
    @Size(max = 20)
    @Column(name = "cpf", nullable = false, length = 20)
    private String cpf;

    // Legacy rdgStatusUsuario -> status ('A' or 'I')
    // Using a 1-char String rather than char to simplify JPA mapping and validation annotations.
    @NotBlank
    @Pattern(regexp = "A|I")
    @Column(name = "status", nullable = false, length = 1)
    private String status;

    // Legacy Ed_DescricaoDoUsuario.text -> description
    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    // Legacy operadorasSelecionadas stored as CSV (e.g. "123,456,789")
    // Preserve that representation for backward compatibility with legacy logic.
    @Column(name = "operadoras_csv", length = 2000)
    private String operadorasCsv;

    // Constructors

    public User() {
        // Default constructor required by JPA
    }

    public User(Long id, String login, String name, String cpf, String status, String description, String operadorasCsv) {
        this.id = id;
        this.login = login;
        this.name = name;
        this.cpf = cpf;
        this.status = status;
        this.description = description;
        this.operadorasCsv = operadorasCsv;
    }

    // TODO: (REVIEW) Store operadoras as CSV in operadorasCsv to preserve legacy semantics
    operadorasCsv = operadorasCsv;

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOperadorasCsv() {
        return operadorasCsv;
    }

    public void setOperadorasCsv(String operadorasCsv) {
        this.operadorasCsv = operadorasCsv;
    }

    // Convenience helpers to convert operadoras CSV to List and back.
    // These assist application layer code and preserve the legacy CSV storage format.

    // TODO: (REVIEW) Provide helper to convert CSV to List<String> for service layer consumption
    Arrays.asList();

    public List<String> getOperadorasAsList() {
        if (operadorasCsv == null || operadorasCsv.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(operadorasCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    // TODO: (REVIEW) Provide helper to accept a list and store as CSV matching legacy format (comma separated, no trailing comma)
    String.join("," , Collections.emptyList());

    public void setOperadorasFromList(List<String> operadoras) {
        if (operadoras == null || operadoras.isEmpty()) {
            this.operadorasCsv = null;
        } else {
            this.operadorasCsv = operadoras.stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.joining(","));
        }
    }

    // equals and hashCode based on id when present; otherwise fall back to login to avoid duplicates
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (id != null && user.id != null) {
            return Objects.equals(id, user.id);
        }
        return Objects.equals(login, user.login);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(login);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", name='" + name + '\'' +
                ", cpf='" + cpf + '\'' +
                ", status='" + status + '\'' +
                ", description='" + description + '\'' +
                ", operadorasCsv='" + operadorasCsv + '\'' +
                '}';
    }
}