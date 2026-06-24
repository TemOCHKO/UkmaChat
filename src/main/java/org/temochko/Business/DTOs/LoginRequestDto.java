package org.temochko.Business.DTOs;

import java.io.Serializable;

public class LoginRequestDto implements Serializable {
    public String username;
    public String password;

    public LoginRequestDto(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
