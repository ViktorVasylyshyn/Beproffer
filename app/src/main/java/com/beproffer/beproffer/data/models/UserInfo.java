package com.beproffer.beproffer.data.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


public class UserInfo {

    private String userId;
    private String userType;
    private String userSpecialistType;
    private String userName;
    private String userEmail;
    private String userPhone;
    private String userProfileImageUrl;
    private String userInfo;

    /*constructor without params for Firebase*/
    public UserInfo() {}

    public UserInfo(@NonNull String userId,
                    String userType,
                    @Nullable String userSpecialistType,
                    String userName,
                    String userEmail,
                    @Nullable String userPhone,
                    @Nullable String userProfileImageUrl,
                    @Nullable String userInfo) {
        this.userId = userId;
        this.userType = userType;
        this.userSpecialistType = userSpecialistType;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPhone = userPhone;
        this.userProfileImageUrl = userProfileImageUrl;
        this.userInfo = userInfo;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public String getUserType() {
        return userType;
    }

    public String getUserSpecialistType() {
        return userSpecialistType;
    }

    public void setUserSpecialistType(String userSpecialistType) {
        this.userSpecialistType = userSpecialistType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserProfileImageUrl() {
        return userProfileImageUrl;
    }

    public void setUserProfileImageUrl(String userProfileImageUrl) {
        this.userProfileImageUrl = userProfileImageUrl;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }
}
