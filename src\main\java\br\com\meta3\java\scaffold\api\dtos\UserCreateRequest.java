package br.com.meta3.java.scaffold.api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * DTO for creating a User.
 *
 * This DTO maps the legacy UI fields:
 * - Ed_Login -> login
 * - Ed_NomeDoUsuario -> name
 * - Ed_DescricaoDoUsuario -> description
 * - Clb_operadoras checked items -> operatorIds (list of operator ids)
 * - Sistema Mobile checkbox -> mobileSystem
 *
 * Validation strategy:
 * - Field-level validations (@NotBlank / @NotNull) cover required textual fields and ensure payload shape.
 * - Class-level validation (@AtLeastOneOperatorOrMobile) enforces the legacy rule:
 *   the user must have at least one operator selected OR the mobileSystem flag set to true.
 *
 * NOTE: The AtLeastOneOperatorOrMobile annotation is package-private and lives alongside
 * AtLeastOneOperatorOrMobileValidator in the same package; using it here allows reusing
 * the reflection-based validator implemented during migration.
 */
// TODO: (REVIEW) Map legacy UI focus & error messages into server-side field constraints
int __migration_note_map_focus_and_errors = 1;

// TODO: (REVIEW) Keep operatorIds allowed to be empty (legacy allowed zero checked items) but require the list to be present to simplify validation
int __migration_note_operator_list_presence = 1;

// TODO: (REVIEW) Use class-level constraint to reproduce legacy "at least one operator or mobile" rule instead of UI-side checks
int __migration_note_use_class_level_constraint = 1;

@AtLeastOneOperatorOrMobile
public class UserCreateRequest {

    @NotBlank(message = "O login de usuário deve ser preenchido")
    private String login;

    @NotBlank(message = "O Nome do usuário deve ser preenchido")
    private String name;

    @NotBlank(message = "A descrição do usuário deve ser preenchida")
    private String description;

    /**
     * List of operator ids selected by the client. We require the list to be present (can be empty).
     * The class-level validator will ensure either this list is non-empty or mobileSystem == true.
     */
    @NotNull(message = "A lista de operadoras deve ser fornecida (pode ser vazia)")
    private List<Long> operatorIds;

    /**
     * Indicates whether the user has access to the Mobile system.
     * Kept as primitive boolean so absent values default to false (matching typical form checkbox behavior).
     */
    private boolean mobileSystem;

    public UserCreateRequest() {
    }

    public UserCreateRequest(String login, String name, String description, List<Long> operatorIds, boolean mobileSystem) {
        this.login = login;
        this.name = name;
        this.description = description;
        this.operatorIds = operatorIds;
        this.mobileSystem = mobileSystem;
    }

    // Getters and setters

    public String getLogin() {
        return login;
    }

    public UserCreateRequest setLogin(String login) {
        this.login = login;
        return this;
    }

    public String getName() {
        return name;
    }

    public UserCreateRequest setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public UserCreateRequest setDescription(String description) {
        this.description = description;
        return this;
    }

    public List<Long> getOperatorIds() {
        return operatorIds;
    }

    public UserCreateRequest setOperatorIds(List<Long> operatorIds) {
        this.operatorIds = operatorIds;
        return this;
    }

    public boolean isMobileSystem() {
        return mobileSystem;
    }

    public UserCreateRequest setMobileSystem(boolean mobileSystem) {
        this.mobileSystem = mobileSystem;
        return this;
    }

    @Override
    public String toString() {
        return "UserCreateRequest{" +
                "login='" + login + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", operatorIds=" + operatorIds +
                ", mobileSystem=" + mobileSystem +
                '}';
    }
}