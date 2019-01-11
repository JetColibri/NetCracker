package com.netcracker.superproject.entity;

import com.netcracker.superproject.entity.annotations.Attribute;
import com.netcracker.superproject.entity.annotations.Entity;

@Entity(type = "2")
public class Event extends BaseEntity {

    @Attribute(type = "2001")
    private String title;

    @Attribute(type = "2002")
    private String description;

    @Attribute(type = "2003")
    private String location;

    @Attribute(type = "2004")
    private String contacts;

    @Attribute(type = "2005")
    private String tags;

    @Attribute(type = "2006")
    private String photos;

    @Attribute(type = "2007")
    private String date;

    public Event() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getContacts() {
        return contacts;
    }

    public void setContacts(String contacts) {
        this.contacts = contacts;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getPhotos() {
        return photos;
    }

    public void setPhotos(String photos) {
        this.photos = photos;
    }

}
