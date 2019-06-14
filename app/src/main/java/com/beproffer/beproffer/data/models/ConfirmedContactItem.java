package com.beproffer.beproffer.data.models;

public class ConfirmedContactItem {

    private String contactUid;
    private String contactName;
    private String contactPhone;
    private String contactImageUrl;
    private String contactInfo;

    private ConfirmedContactItem(){

    }

    public ConfirmedContactItem(String contactUid, String contactName, String contactPhone, String contactInfo, String contactImageUrl) {
        this.contactUid = contactUid;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.contactInfo = contactInfo;
        this.contactImageUrl = contactImageUrl;
    }

    public String getContactUid() {
        return contactUid;
    }

    public void setContactUid(String contactUid) {
        this.contactUid = contactUid;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }


    public String getContactImageUrl() {
        return contactImageUrl;
    }

    public void setContactImageUrl(String contactImageUrl) {
        this.contactImageUrl = contactImageUrl;
    }

}
