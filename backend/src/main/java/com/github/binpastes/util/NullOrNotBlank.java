package com.github.binpastes.util;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target( {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = NullOrNotBlank.NullOrNotBlankValidator.class)
public @interface NullOrNotBlank {
    String message() default "{jakarta.validation.constraints.NotBlank.message}";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default {};

    class NullOrNotBlankValidator implements ConstraintValidator<NullOrNotBlank, String> {

        public void initialize(NullOrNotBlank parameters) {
            // nothing to do here
        }

        public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
            return value == null || !value.isBlank();
        }
    }
}
