package com.beproffer.beproffer.data.models;

public class IncomingContactRequestItem {

    private String requestUid;
    private String requestType;
    private String requestName;
    private String requestImageUrl;

    private IncomingContactRequestItem(){

    }

    public IncomingContactRequestItem(String requestUid,
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

    public void setRequestUid(String requestUid) {
        this.requestUid = requestUid;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getRequestName() {
        return requestName;
    }

    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    public String getRequestImageUrl() {
        return requestImageUrl;
    }

    public void setRequestImageUrl(String requestImageUrl) {
        this.requestImageUrl = requestImageUrl;
    }

}
