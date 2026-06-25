package org.temochko.Business.DTOs.Message;

import java.io.Serializable;
import java.security.SecureRandomParameters;

public class ChatMessage implements Serializable {
    public String message;
    public String username;
    public String timestamp;
    public String from;

    public ChatMessage(String target, String from, String message, String timestamp) {
        this.message = message;
        this.username = target;
        this.timestamp = timestamp;
        this.from = from;
    }
}
