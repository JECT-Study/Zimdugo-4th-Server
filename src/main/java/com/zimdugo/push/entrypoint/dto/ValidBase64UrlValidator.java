package com.zimdugo.push.entrypoint.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Base64;

public class ValidBase64UrlValidator implements ConstraintValidator<ValidBase64Url, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        try {
            Base64.getUrlDecoder().decode(value);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
