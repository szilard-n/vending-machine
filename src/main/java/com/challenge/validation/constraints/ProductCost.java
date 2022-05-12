package com.challenge.validation.constraints;

import com.challenge.validation.ProductCostValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ProductCostValidator.class)
public @interface ProductCost {

    String message() default "{validation.productPrice}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
