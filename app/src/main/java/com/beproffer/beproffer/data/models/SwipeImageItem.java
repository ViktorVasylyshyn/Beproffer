package com.beproffer.beproffer.data.models;

public class SwipeImageItem {
    private String uid;
    private String url;
    private String price;
    private String duration;
    private String description;
    private String type;
    private String subtype;
    private String gender;

    public SwipeImageItem(){

    }

    public SwipeImageItem(String url,
                          String uid,
                          String price,
                          String duration,
                          String description,
                          String type,
                          String subtype,
                          String gender) {
        this.uid = uid;
        this.url = url;
        this.price = price;
        this.duration = duration;
        this.description = description;
        this.gender = gender;
        this.type = type;
        this.subtype = subtype;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

}
