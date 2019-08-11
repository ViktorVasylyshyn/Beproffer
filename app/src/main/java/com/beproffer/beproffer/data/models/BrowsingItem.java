package com.beproffer.beproffer.data.models;

import java.util.Objects;

public class BrowsingItem {
    private String uid;
    private String url;
    private String price;
    private String duration;
    private String description;
    private String type;
    private String subtype;
    private String gender;

    /*конструктор без параметров нужен для Firebase*/
    public BrowsingItem(){

    }

    public BrowsingItem(String url,
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

    public String getPrice() {
        return price;
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


    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof BrowsingItem)) {
            return false;
        }
        BrowsingItem image = (BrowsingItem) o;
        return  Objects.equals(uid, image.uid) &&
                Objects.equals(url, image.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, url);
    }
}
