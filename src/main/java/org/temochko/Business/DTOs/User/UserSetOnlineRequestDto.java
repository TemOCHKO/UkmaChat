package org.temochko.Business.DTOs.User;

import java.io.Serializable;

public class UserSetOnlineRequestDto implements Serializable {

    public String username;
    public boolean online;
    public UserSetOnlineRequestDto(String username, boolean online) throws Exception {
        this.username = username;
        this.online = online;
    }
}
