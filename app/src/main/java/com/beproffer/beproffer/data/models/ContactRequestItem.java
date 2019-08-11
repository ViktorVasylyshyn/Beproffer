package com.beproffer.beproffer.data.models;

public class ContactRequestItem {

    private String requestUid;
    private String requestType;
    private String requestName;
    private String requestImageUrl;

    /*empty constructor for Firebase*/
    private ContactRequestItem() {
    }

    public ContactRequestItem(String requestUid,
                              String requestType,
                              String requestName,
                              String requestImageUrl) {
        this.requestUid = requestUid;
        this.requestType = requestType;
        this.requestName = requestName;
        this.requestImageUrl = requestImageUrl;
    }

    public String getRequestUid() {
        return requestUid;
    }

    public String getRequestType() {
        return requestType;
    }

    public String getRequestName() {
        return requestName;
    }

    public String getRequestImageUrl() {
        return requestImageUrl;
    }
}

