package br.com.meta3.java.scaffold.api.dtos;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Objects;

/**
 * Validator that ensures an incoming DTO has at least one operator selected (operatorIds list non-empty)
 * or the mobileSystem flag set to true.
 *
 * The validator uses reflection to avoid tight coupling with a specific DTO class structure.
 */
// Package-private annotation so it can live in the same file as the public validator class.
// Keeping it package-private avoids creating extra top-level files while still usable by validators.
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@interface AtLeastOneOperatorOrMobile {
    String message() default "O usuário deve possuir pelo menos 1 operadora ou o Sistema Mobile";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

/**
 * ConstraintValidator implementation that inspects DTO instances via reflection.
 */
public class AtLeastOneOperatorOrMobileValidator implements ConstraintValidator<AtLeastOneOperatorOrMobile, Object> {

    // TODO: (REVIEW) Using reflection to access operatorIds/mobileSystem to decouple from DTO shape
    // reflectFields(value)
    // Rationale: we intentionally call helper reflectHasOperatorOrMobile(value) rather than depending on a concrete DTO interface.

    @Override
    public void initialize(AtLeastOneOperatorOrMobile constraintAnnotation) {
        // No initialization required for this validator
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        // TODO: (REVIEW) Returning true on null to let @NotNull handle nullability if needed
        // allowNullValidation(value)
        if (value == null) {
            return true;
        }

        try {
            boolean ok = reflectHasOperatorOrMobile(value);
            if (!ok) {
                // keep default message; no additional context needed here
            }
            return ok;
        } catch (Exception ex) {
            // In case of unexpected reflection errors, fail the validation to be safe.
            // TODO: (REVIEW) Fail fast on reflection exceptions to avoid silent acceptance of invalid DTOs
            // handleReflectionError(ex)
            return false;
        }
    }

    /**
     * Uses reflection to determine if DTO has non-empty operatorIds collection or mobileSystem == true.
     *
     * Strategy:
     *  - Prefer getter methods following JavaBean conventions (getX/isX). We search for methods whose name contains "operator"
     *    and return a Collection, and for methods whose name contains "mobile" and return boolean/Boolean.
     *  - If no suitable getters are found, we inspect fields directly (operator* and mobile*).
     *
     * This approach is intentionally generic to avoid tight coupling with specific DTO classes.
     */
    private boolean reflectHasOperatorOrMobile(Object dto) {
        Class<?> cls = dto.getClass();

        // 1) Check methods (preferred)
        Method[] methods = cls.getMethods();
        Boolean mobileResult = null;
        Boolean operatorsResult = null;

        for (Method m : methods) {
            String name = m.getName().toLowerCase();
            Class<?> returnType = m.getReturnType();

            // Operator collection detection
            if (name.contains("operator") && Collection.class.isAssignableFrom(returnType)) {
                try {
                    Object ret = m.invoke(dto);
                    if (ret instanceof Collection) {
                        Collection<?> coll = (Collection<?>) ret;
                        if (coll != null && !coll.isEmpty()) {
                            // TODO: (REVIEW) Found operator collection via getter method
                            // reflectOperatorFound()
                            operatorsResult = Boolean.TRUE;
                            break; // satisfied requirement
                        } else {
                            operatorsResult = Boolean.FALSE;
                        }
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    // ignore this method and continue searching; we'll try fields later
                }
            }

            // Mobile flag detection
            if (mobileResult == null && (name.contains("mobile") || name.contains("ismobile") || name.contains("hasmobile"))) {
                if (returnType == boolean.class || returnType == Boolean.class) {
                    try {
                        Object ret = m.invoke(dto);
                        if (ret instanceof Boolean) {
                            mobileResult = (Boolean) ret;
                            if (mobileResult) {
                                // TODO: (REVIEW) Found mobile flag via getter method and it's true
                                // reflectMobileFound()
                                return true;
                            }
                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        // ignore and continue
                    }
                }
            }
        }

        if (Boolean.TRUE.equals(operatorsResult)) {
            return true;
        }

        // 2) If methods didn't satisfy, check fields directly
        Field[] fields = cls.getDeclaredFields();
        for (Field f : fields) {
            String fname = f.getName().toLowerCase();
            Class<?> ftype = f.getType();
            f.setAccessible(true);

            // Operator collection detection in fields
            if (fname.contains("operator") && Collection.class.isAssignableFrom(ftype)) {
                try {
                    Object ret = f.get(dto);
                    if (ret instanceof Collection) {
                        Collection<?> coll = (Collection<?>) ret;
                        if (coll != null && !coll.isEmpty()) {
                            // TODO: (REVIEW) Found operator collection via field access
                            // reflectOperatorFieldFound()
                            return true;
                        }
                    }
                } catch (IllegalAccessException e) {
                    // ignore and continue
                }
            }

            // Mobile flag detection in fields
            if ((fname.contains("mobile") || fname.contains("ismobile") || fname.contains("hasmobile"))
                    && (ftype == boolean.class || ftype == Boolean.class)) {
                try {
                    Object ret = f.get(dto);
                    if (ret instanceof Boolean && (Boolean) ret) {
                        // TODO: (REVIEW) Found mobile flag via field access and it's true
                        // reflectMobileFieldFound()
                        return true;
                    }
                } catch (IllegalAccessException e) {
                    // ignore and continue
                }
            }
        }

        // As a last attempt, if methods found a mobileResult false and operatorsResult false, return false.
        return false;
    }
}