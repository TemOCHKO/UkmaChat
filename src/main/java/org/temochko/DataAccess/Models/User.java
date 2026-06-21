package org.temochko.DataAccess.Models;

import java.io.Serializable;
import java.time.LocalDateTime;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String username;
    private String email;
    private boolean online;
    private LocalDateTime lastSeen;

    public User() {}

    public User(int id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public int getId()                  { return id; }
    public void setId(int id)           { this.id = id; }
    public String getUsername()         { return username; }
    public void setUsername(String u)   { this.username = u; }
    public String getEmail()            { return email; }
    public void setEmail(String e)      { this.email = e; }
    public boolean isOnline()           { return online; }
    public void setOnline(boolean o)    { this.online = o; }
    public LocalDateTime getLastSeen()  { return lastSeen; }
    public void setLastSeen(LocalDateTime t) { this.lastSeen = t; }

    @Override public String toString()  { return username; }
}