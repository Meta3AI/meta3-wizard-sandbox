package br.com.meta3.java.scaffold.api.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * DTO used to receive user creation payload.
 *
 * Supports accepting operadoras either as a CSV string (operadorasCsv) or as a list of integers (operadoras).
 * Validation annotations (Jakarta Validation) are applied so the controller can validate input before delegating
 * to the application/business layer.
 */
public class UserCreateRequest {

    @JsonProperty("login")
    @NotBlank(message = "login must not be blank")
    @Size(max = 50, message = "login must not exceed 50 characters")
    private String login;

    @JsonProperty("name")
    @NotBlank(message = "name must not be blank")
    @Size(max = 100, message = "name must not exceed 100 characters")
    private String name;

    // Note: CPF validation here is simplified to require 11 digits. Depending on business rules,
    // a more thorough CPF validation (checksum) can be added in the service layer.
    @JsonProperty("cpf")
    @NotBlank(message = "cpf must not be blank")
    @Pattern(regexp = "\\d{11}", message = "cpf must contain exactly 11 digits")
    private String cpf;

    // Status expected to be 'A' (active) or 'I' (inactive) as in legacy code.
    @JsonProperty("status")
    @NotBlank(message = "status must not be blank")
    @Pattern(regexp = "A|I", message = "status must be 'A' or 'I'")
    private String status;

    @JsonProperty("description")
    @Size(max = 255, message = "description must not exceed 255 characters")
    private String description;

    // Accept operadoras either as a list of integers...
    @JsonProperty("operadoras")
    private List<Integer> operadoras;

    // ...or as a CSV string, for compatibility with clients that send a comma-separated value.
    // Example: "12,34,56"
    @JsonProperty("operadorasCsv")
    @Size(max = 2000, message = "operadorasCsv is too long")
    private String operadorasCsv;

    public UserCreateRequest() {
        // default constructor for deserialization
    }

    // Getters and setters

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

    public List<Integer> getOperadoras() {
        return operadoras;
    }

    public void setOperadoras(List<Integer> operadoras) {
        this.operadoras = operadoras;
    }

    public String getOperadorasCsv() {
        return operadorasCsv;
    }

    public void setOperadorasCsv(String operadorasCsv) {
        this.operadorasCsv = operadorasCsv;
    }

    /**
     * Returns a normalized list of operadora IDs combining both possible input forms:
     * - If operadoras (List<Integer>) is present and non-empty, it is returned as-is.
     * - Otherwise, operadorasCsv (String) is parsed into integers and returned.
     *
     * This makes it easy for controllers/services to retrieve a single canonical representation.
     */
    @JsonIgnore
    public List<Integer> getNormalizedOperadoras() {
        if (operadoras != null && !operadoras.isEmpty()) {
            return Collections.unmodifiableList(operadoras);
        }
        if (operadorasCsv == null || operadorasCsv.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> parsed = parseCsvToIntegers(operadorasCsv);
        return Collections.unmodifiableList(parsed);
    }

    /**
     * Validates that at least one source of operadoras is provided (either the list or the CSV).
     * This prevents calls that would result in no associated operadoras, matching legacy expectations
     * where a trailing comma was removed but presence was assumed.
     */
    @AssertTrue(message = "At least one operadora must be provided (operadoras list or operadorasCsv)")
    @JsonIgnore
    public boolean isOperadorasPresent() {
        return (operadoras != null && !operadoras.isEmpty()) ||
               (operadorasCsv != null && !operadorasCsv.trim().isEmpty());
    }

    // Helper: parse CSV into List<Integer>, ignoring empty segments and trimming spaces.
    @JsonIgnore
    private List<Integer> parseCsvToIntegers(String csv) {
        if (csv == null) {
            return Collections.emptyList();
        }

        String[] parts = csv.split(",");
        List<Integer> result = new ArrayList<>(parts.length);
        for (String p : parts) {
            String t = p == null ? "" : p.trim();
            if (t.isEmpty()) {
                continue;
            }
            try {
                // In legacy code the operadora ID extraction relied on substring operations and conversions.
                // Here we assume the CSV already contains numeric IDs.
                result.add(Integer.parseInt(t));
            } catch (NumberFormatException ex) {
                // TODO: (REVIEW) Legacy code silently assumed well-formed numeric IDs; here we skip invalid entries.
                // skip invalid number - business layer can reject if strict behavior is required
                // parseCsvToIntegers: skipping invalid token -> t
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "UserCreateRequest{" +
                "login='" + login + '\'' +
                ", name='" + name + '\'' +
                ", cpf='" + (cpf == null ? null : "***REDACTED***") + '\'' +
                ", status='" + status + '\'' +
                ", description='" + description + '\'' +
                ", operadoras=" + operadoras +
                ", operadorasCsv='" + operadorasCsv + '\'' +
                '}';
    }

    // TODO: (REVIEW) Decided to provide getNormalizedOperadoras to unify CSV and List inputs for service layer
    // getNormalizedOperadoras();

    // TODO: (REVIEW) Simplified CPF validation to digit-count only; a strict checksum can be applied in the service layer
    // validateCpfChecksum(cpf);

    // TODO: (REVIEW) When parsing CSV we skip invalid number tokens instead of failing fast; adjust to strict mode if required
    // parseCsvToIntegers(operadorasCsv);
}