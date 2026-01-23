package com.demo.common.validation.annoatation;

import com.demo.common.validation.validator.NoForbiddenWordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {NoForbiddenWordValidator.class})
public @interface NoForbiddenWord {

    String message() default "有违禁词";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
