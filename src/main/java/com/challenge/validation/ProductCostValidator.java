package com.challenge.validation;

import com.challenge.validation.constraints.ProductCost;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ProductCostValidator implements ConstraintValidator<ProductCost, Integer> {

    @Override
    public void initialize(ProductCost constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        return value != null && value != 0 && value % 5 == 0;
    }
}
