package org.temochko.Business.DTOs;

import java.io.Serializable;

public class LoginResponseDto implements Serializable {
    public boolean success;
    public String message;
    public String sessionToken;

    public LoginResponseDto(boolean success, String message, String sessionToken) {
        this.success = success;
        this.message = message;
        this.sessionToken = sessionToken;
    }
}
