package no.aurel.backend.customer.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

// A phone number we can send a text message to. Used like @NotBlank on a request field.
@Documented
@Constraint(validatedBy = ValidPhoneValidator.class)
@Target({ ElementType.FIELD, ElementType.RECORD_COMPONENT, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhone {

    String message() default "Ugyldig telefonnummer. Skriv et norsk mobilnummer, for eksempel 912 34 567.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
