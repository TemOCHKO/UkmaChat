package org.temochko.Business.DTOs.User;

import java.io.Serializable;
import java.util.List;

public class SearchUserResponseDto implements Serializable {
    private static final long serialVersionUID = 1L;

    public boolean success;
    public String message;
    public List<UserSummaryDto> foundUsers;

    public SearchUserResponseDto(boolean success, String message, List<UserSummaryDto> foundUsers) {
        this.success = success;
        this.message = message;
        this.foundUsers = foundUsers;
    }
}