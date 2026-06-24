package org.temochko.Business.DTOs.Message;

import java.io.Serializable;

public class ChatHistoryRequestDto implements Serializable {

    public String targetUsername;

    public ChatHistoryRequestDto(String targetUsername) {
        this.targetUsername = targetUsername;
    }
}
