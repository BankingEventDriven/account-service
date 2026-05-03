package com.bank.account_service.interfaces.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = CurrencyValidator.class)   // links annotation → validator logic
@Target({ElementType.FIELD, ElementType.PARAMETER})   // valid on fields and method params
@Retention(RetentionPolicy.RUNTIME)                   // must be RUNTIME for reflection
public @interface ValidCurrency {

    String message() default "Invalid ISO 4217 currency code";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
