package no.aurel.backend.customer.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import no.aurel.backend.customer.PhoneNumber;

public class ValidPhoneValidator implements ConstraintValidator<ValidPhone, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Missing is @NotBlank's job, so this check only judges a number that was actually typed
        return value == null || value.isBlank() || PhoneNumber.isValid(value);
    }
}
