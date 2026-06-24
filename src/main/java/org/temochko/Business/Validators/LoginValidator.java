package org.temochko.Business.Validators;

public class LoginValidator {

    public static ValidationResult validate(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return ValidationResult.error("Username cannot be empty.");
        }

        if (password == null || password.isEmpty()) {
            return ValidationResult.error("Password cannot be empty.");
        }

        return ValidationResult.success();
    }
}