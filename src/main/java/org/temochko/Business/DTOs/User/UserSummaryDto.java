package org.temochko.Business.DTOs.User;

import java.io.Serializable;

public class UserSummaryDto implements Serializable {
    private static final long serialVersionUID = 1L;

    public int id;
    public String username;
    public boolean isOnline;
    // Можеш додати avatarId або lastSeen, якщо це потрібно для UI

    public UserSummaryDto(int id, String username, boolean isOnline) {
        this.id = id;
        this.username = username;
        this.isOnline = isOnline;
    }
}
