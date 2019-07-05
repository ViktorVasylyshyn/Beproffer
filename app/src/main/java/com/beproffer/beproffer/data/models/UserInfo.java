package com.beproffer.beproffer.data.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


public class UserInfo {

    private String userId;
    private String userType;
    private String userName;
    private String userEmail;
    private String userGender;
    private String userPhone;
    private String userProfileImageUrl;
    private String userInfo;

    public UserInfo() {
    }

    public UserInfo(@NonNull String userId, String userType, String userName,
                    String userEmail, @Nullable String userGender, @Nullable String userPhone,
                    @Nullable String userProfileImageUrl, @Nullable String userInfo) {
        this.userId = userId;
        this.userType = userType;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPhone = userPhone;
        this.userGender = userGender;
        this.userProfileImageUrl = userProfileImageUrl;
        this.userInfo = userInfo;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
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

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserGender() {
        return userGender;
    }

    public void setUserGender(String userGender) {
        this.userGender = userGender;
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
