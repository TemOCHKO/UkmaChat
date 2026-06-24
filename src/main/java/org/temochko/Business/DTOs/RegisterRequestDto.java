package org.temochko.Business.DTOs;

import java.io.Serializable;

public class RegisterRequestDto implements Serializable {
    public String username;
    public String password;
    public String email;

    public RegisterRequestDto(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
}
