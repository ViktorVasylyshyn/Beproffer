package com.beproffer.beproffer.data.models;

public class ContactItem {
    /*it always contains only specialists' data*/

    private String id;
    private String name;
    private String phone;
    private String specialization;
    private String profileImageUrl;
    private String description;

    public ContactItem() {
        /*private no-arg constructor for Firebase*/
    }

    public ContactItem(String id,
                       String name,
                       String phone,
                       String specialization,
                       String description,
                       String profileImageUrl) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.specialization = specialization;
        this.description = description;
        this.profileImageUrl = profileImageUrl;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getDescription() {
        return description;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}


