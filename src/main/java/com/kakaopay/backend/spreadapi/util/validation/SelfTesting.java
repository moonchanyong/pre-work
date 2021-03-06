package com.kakaopay.backend.spreadapi.util.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = Validator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SelfTesting {
    String message() default "Not Validated Domain State";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
