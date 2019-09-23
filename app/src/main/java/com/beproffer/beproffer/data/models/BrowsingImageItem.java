package com.beproffer.beproffer.data.models;

import android.os.Parcel;
import android.os.Parcelable;

public class BrowsingImageItem implements Parcelable {
    private String key;
    private String url;
    private String id;
    private String type;
    private String subtype;
    private String price;
    private String duration;
    private String description;
    private String gender;
    private String profileImageUrl;
    private String name;

    public BrowsingImageItem(){

    }

    public BrowsingImageItem(String url,
                             String key,
                             String id,
                             String type,
                             String subtype,
                             String price,
                             String duration,
                             String description,
                             String gender,
                             String profileImageUrl,
                             String name) {
        this.url = url;
        this.key = key;
        this.id = id;
        this.type = type;
        this.subtype = subtype;
        this.price = price;
        this.duration = duration;
        this.description = description;
        this.gender = gender;
        this.profileImageUrl = profileImageUrl;
        this.name = name;
    }

    protected BrowsingImageItem(Parcel in) {
        key = in.readString();
        url = in.readString();
        id = in.readString();
        type = in.readString();
        subtype = in.readString();
        price = in.readString();
        duration = in.readString();
        description = in.readString();
        gender = in.readString();
        profileImageUrl = in.readString();
        name = in.readString();
    }

    public static final Creator<BrowsingImageItem> CREATOR = new Creator<BrowsingImageItem>() {
        @Override
        public BrowsingImageItem createFromParcel(Parcel in) {
            return new BrowsingImageItem(in);
        }

        @Override
        public BrowsingImageItem[] newArray(int size) {
            return new BrowsingImageItem[size];
        }
    };

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getKey() {
        return key;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(key);
        parcel.writeString(url);
        parcel.writeString(id);
        parcel.writeString(type);
        parcel.writeString(subtype);
        parcel.writeString(price);
        parcel.writeString(duration);
        parcel.writeString(description);
        parcel.writeString(gender);
        parcel.writeString(profileImageUrl);
        parcel.writeString(name);
    }

}
