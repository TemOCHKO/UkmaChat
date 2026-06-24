package org.temochko.Business.DTOs.Register;

import java.io.Serializable;

public class RegisterResponseDto implements Serializable {
    public boolean success;
    public String message;
    public String sessionToken;

    public RegisterResponseDto(boolean success, String message, String sessionToken) {
        this.success = success;
        this.message = message;
        this.sessionToken = sessionToken;
    }
}
