package com.beproffer.beproffer.data.models;

public class ContactItem {

    private String contactUid;
    private String contactName;
    private String contactPhone;
    private String contactSpecialistType;
    private String contactImageUrl;
    private String contactInfo;

    /*constructor without parameters for Firebase*/
    private ContactItem() {
    }

    public ContactItem(String contactUid,
                       String contactName,
                       String contactPhone,
                       String contactSpecialistType,
                       String contactInfo,
                       String contactImageUrl) {
        this.contactUid = contactUid;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.contactSpecialistType = contactSpecialistType;
        this.contactInfo = contactInfo;
        this.contactImageUrl = contactImageUrl;
    }

    public String getContactUid() {
        return contactUid;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public String getContactSpecialistType() {
        return contactSpecialistType;
    }

    public String getContactImageUrl() {
        return contactImageUrl;
    }
}


