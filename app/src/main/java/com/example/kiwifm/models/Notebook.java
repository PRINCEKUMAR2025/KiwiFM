package com.example.kiwifm.models;


import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

@IgnoreExtraProperties
public class Notebook {

    private String id;
    private String title;
    private String creatorEmail;
    private long creationTime;
    private long lastUpdateTime;
    private String fullContent; // This will store the complete story as one text
    private List<Contribution> contributions;

    public Notebook() {
        // Default constructor required for Firebase
        fullContent = "";
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreatorEmail() {
        return creatorEmail;
    }

    public void setCreatorEmail(String creatorEmail) {
        this.creatorEmail = creatorEmail;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getFullContent() {
        return fullContent;
    }

    public void setFullContent(String fullContent) {
        this.fullContent = fullContent;
    }

    public void appendContent(String newContent, String contributorEmail) {
        // Add a separator and contributor info before appending new content
        if (fullContent == null) {
            fullContent = "";
        }

        if (!fullContent.isEmpty()) {
            fullContent += "\n\n";
        }

        fullContent += newContent + "\n-- Contributed by: " + contributorEmail;
    }

    public List<Contribution> getContributions() {
        return contributions;
    }

    public void setContributions(List<Contribution> contributions) {
        this.contributions = contributions;
    }
}


