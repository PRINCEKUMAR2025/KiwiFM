package com.example.kiwifm.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Contribution {

    private String email;
    private String content;
    private long timestamp;

    public Contribution() {
        // Default constructor required for Firebase
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
