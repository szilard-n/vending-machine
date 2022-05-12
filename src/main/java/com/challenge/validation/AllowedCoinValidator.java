package com.challenge.validation;

import com.challenge.validation.constraints.AllowedCoin;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.springframework.beans.factory.annotation.Value;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Set;

public class AllowedCoinValidator implements ConstraintValidator<AllowedCoin, Integer> {

    @Value("#{'${user.deposit.allowedCoins}'.split(',')}")
    private Set<Integer> allowedCoins;

    @Override
    public void initialize(AllowedCoin constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value != null & allowedCoins.contains(value)) {
            return true;
        } else {
            ((ConstraintValidatorContextImpl) context).addMessageParameter("allowedCoins", allowedCoins);
            return false;
        }
    }
}
