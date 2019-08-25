package com.beproffer.beproffer.data.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


public class UserInfo {

    private String id;
    private String type;
    private String specialization; /*relevant for specialists' only*/
    private String name;
    private String email;
    private String phone;
    private String profileImageUrl;
    private String description;
    /*TODO в будущем добавить локализацию юзера, которую от сам, мануально будет задавать*/

    public UserInfo() {
        /*public no-arg constructor needed for for Firebase*/
    }

    public UserInfo(@NonNull String id,
                    String type,
                    @Nullable String specialization,
                    String name,
                    String email,
                    @Nullable String phone,
                    @Nullable String profileImageUrl,
                    @Nullable String description) {
        this.id = id;
        this.type = type;
        this.specialization = specialization;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.profileImageUrl = profileImageUrl;
        this.description = description;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
