package org.temochko.Business.DTOs.User;


import java.io.Serializable;

public class SearchUserRequestDto implements Serializable {
    private static final long serialVersionUID = 1L;

    public String searchQuery;

    public SearchUserRequestDto(String searchQuery) {
        this.searchQuery = searchQuery;
    }
}
