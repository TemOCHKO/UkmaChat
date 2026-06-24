package org.temochko.Business.DTOs.Message;

import java.io.Serializable;
import java.util.List;

public class ChatHistoryResponseDto implements Serializable {

    public String targetUsername;
    public List<ChatMessage> history;

    public ChatHistoryResponseDto(String targetUsername, List<ChatMessage> history) {
        this.targetUsername = targetUsername;
        this.history = history;
    }
}
