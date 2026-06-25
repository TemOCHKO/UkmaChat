package org.temochko.Business.Validators;

public class ValidationResult {
    public final boolean isValid;
    public final String errorMessage;

    private ValidationResult(boolean isValid, String errorMessage) {
        this.isValid = isValid;
        this.errorMessage = errorMessage;
    }

    public static ValidationResult success() {
        return new ValidationResult(true, "");
    }

    public static ValidationResult error(String message) {
        return new ValidationResult(false, message);
    }
}