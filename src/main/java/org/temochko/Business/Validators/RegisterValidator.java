package org.temochko.Business.Validators;

import java.util.regex.Pattern;

public class RegisterValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
            + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$");

    public static ValidationResult validate(String username, String email, String password, String confirmPassword) {
        // username
        if (username == null || username.trim().isEmpty()) {
            return ValidationResult.error("Username cannot be empty.");
        }
        if (username.trim().length() < 3) {
            return ValidationResult.error("Username must be at least 3 characters long.");
        }
        if (username.trim().length() > 20) {
            return ValidationResult.error("Username cannot exceed 20 characters.");
        }

        // email
        if (email == null || email.trim().isEmpty()) {
            return ValidationResult.error("Email cannot be empty.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ValidationResult.error("Please enter a valid email address.");
        }

        // password
        if (password == null || password.isEmpty()) {
            return ValidationResult.error("Password cannot be empty.");
        }
        if (password.length() < 6) {
            return ValidationResult.error("Password must be at least 6 characters long.");
        }
        if (!password.equals(confirmPassword)) {
            return ValidationResult.error("Passwords do not match.");
        }

        return ValidationResult.success();
    }
}
