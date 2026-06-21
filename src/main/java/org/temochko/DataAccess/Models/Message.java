package org.temochko.DataAccess.Models;
import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int senderId;
    private String senderUsername;
    private int receiverId;
    private String content;
    private LocalDateTime sentAt;

    public Message() {}

    public Message(int senderId, String senderUsername, int receiverId, String content) {
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.receiverId = receiverId;
        this.content = content;
        this.sentAt = LocalDateTime.now();
    }

    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }
    public int getSenderId()                { return senderId; }
    public void setSenderId(int id)         { this.senderId = id; }
    public String getSenderUsername()       { return senderUsername; }
    public void setSenderUsername(String u) { this.senderUsername = u; }
    public int getReceiverId()              { return receiverId; }
    public void setReceiverId(int id)       { this.receiverId = id; }
    public String getContent()             { return content; }
    public void setContent(String c)       { this.content = c; }
    public LocalDateTime getSentAt()       { return sentAt; }
    public void setSentAt(LocalDateTime t) { this.sentAt = t; }
}