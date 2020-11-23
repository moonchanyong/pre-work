package com.kakaopay.backend.spreadapi.util.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class Validator implements ConstraintValidator<SelfTesting, Object> {
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value instanceof Testable) {
            ((Testable) value).isValid();
        }

        return true;
    }
}
