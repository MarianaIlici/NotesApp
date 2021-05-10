package com.example.notesapp.entities;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class Note implements Serializable {

    private String id;
    private String title;
    private String dateTime;
    private String subtitle;
    private String noteText;
    private String imagePath;
    private String color;
    private String webLink;
    private String publisher;

    public Note() {

    }

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

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getWebLink() {
        return webLink;
    }

    public void setWebLink(String webLink) {
        this.webLink = webLink;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Map<String, Object> dataToDataBase() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", this.id);
        hashMap.put("title", this.title);
        hashMap.put("dateTime", this.dateTime);
        hashMap.put("subtitle", this.subtitle);
        hashMap.put("noteText", this.noteText);
        hashMap.put("imagePath", this.imagePath);
        hashMap.put("color", this.color);
        hashMap.put("webLink", this.webLink);
        hashMap.put("publisher", this.publisher);
        return hashMap;
    }
}
