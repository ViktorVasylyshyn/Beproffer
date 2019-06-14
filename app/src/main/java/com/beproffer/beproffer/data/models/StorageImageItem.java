package com.beproffer.beproffer.data.models;

import android.os.Parcel;
import android.os.Parcelable;

public class StorageImageItem implements Parcelable {
    private String key;
    private String url;
    private String uid;
    private String type;
    private String subtype;
    private String price;
    private String duration;
    private String description;
    private String gender;

    public StorageImageItem(){

    }

    public StorageImageItem(String url, String key, String uid, String type, String subtype, String price, String duration, String description, String gender) {
        this.url = url;
        this.key = key;
        this.uid = uid;
        this.type = type;
        this.subtype = subtype;
        this.price = price;
        this.duration = duration;
        this.description = description;
        this.gender = gender;
    }

    protected StorageImageItem(Parcel in) {
        key = in.readString();
        url = in.readString();
        uid = in.readString();
        type = in.readString();
        subtype = in.readString();
        price = in.readString();
        duration = in.readString();
        description = in.readString();
        gender = in.readString();
    }

    public static final Creator<StorageImageItem> CREATOR = new Creator<StorageImageItem>() {
        @Override
        public StorageImageItem createFromParcel(Parcel in) {
            return new StorageImageItem(in);
        }

        @Override
        public StorageImageItem[] newArray(int size) {
            return new StorageImageItem[size];
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

    public void setKey(String key) {
        this.key = key;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(key);
        parcel.writeString(url);
        parcel.writeString(uid);
        parcel.writeString(type);
        parcel.writeString(subtype);
        parcel.writeString(price);
        parcel.writeString(duration);
        parcel.writeString(description);
        parcel.writeString(gender);
    }

}
