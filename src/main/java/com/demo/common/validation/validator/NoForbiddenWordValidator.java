package com.demo.common.validation.validator;

import com.demo.common.validation.annoatation.NoForbiddenWord;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class NoForbiddenWordValidator
        implements ConstraintValidator<NoForbiddenWord, String> {

    //todo: use redis or db to store and check forbidden words
    static Set<String> forbiddenWords = Set.of("傻逼", "sb", "牛逼");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        for (String forbiddenWord : forbiddenWords) {
            if (value.contains(forbiddenWord)) {
                return false;
            }
        }
        return true;
    }

}
