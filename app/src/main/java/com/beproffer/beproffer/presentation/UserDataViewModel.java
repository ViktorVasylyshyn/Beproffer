package com.beproffer.beproffer.presentation;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.beproffer.beproffer.data.firebase.UserDataRepository;
import com.beproffer.beproffer.data.models.BrowsingImageItem;
import com.beproffer.beproffer.data.models.ContactItem;
import com.beproffer.beproffer.data.models.ContactRequestItem;
import com.beproffer.beproffer.data.models.UserInfo;

import java.util.List;
import java.util.Map;

public class UserDataViewModel extends AndroidViewModel {

    private final UserDataRepository mRepository;

    public UserDataViewModel(@NonNull Application application) {
        super(application);
        mRepository = new UserDataRepository(application);
    }

    public LiveData<UserInfo> getUserInfoLiveData() {
        return mRepository.getUserInfoLiveData();
    }

    public void updateUserInfo(UserInfo updatedUserInfo, @Nullable Uri updatedImageUri) {
        mRepository.updateUserInfo(updatedUserInfo, updatedImageUri);
    }

    public LiveData<Map<String, BrowsingImageItem>> getServiceItemsList() {
        return mRepository.getServiceItemsList();
    }

    public LiveData<List<BrowsingImageItem>> getServiceItemsList(String specialistId) {
        return mRepository.getServiceItemsList(specialistId);
    }

    public void updateSpecialistGallery(BrowsingImageItem newItem, @Nullable Uri resultUri) {
        mRepository.updateSpecialistGallery(newItem, resultUri);
    }

    public LiveData<BrowsingImageItem> getEditableGalleryItem() {
        return mRepository.getEditableGalleryItem();
    }

    public void setEditableGalleryItem(BrowsingImageItem editableItem) {
        mRepository.setEditableGalleryItem(editableItem);
    }

    public void deleteNotRelevantImageData(BrowsingImageItem updatedItem, String primordialItemType, String primordialItemSubtype) {
        mRepository.deleteNotRelevantImageData(updatedItem, primordialItemType, primordialItemSubtype);
    }

    public LiveData<Map<String, ContactItem>> getContacts() {
        return mRepository.getContacts();
    }

    public LiveData<Map<String, ContactRequestItem>> getIncomingContactRequests() {
        return mRepository.getContactRequests();
    }

    public void handleIncomingContactRequest(ContactRequestItem handledItem, boolean confirm) {
        mRepository.handleIncomingContactRequest(handledItem, confirm);
    }

    public void deleteContact(ContactItem deletedContact) {
        mRepository.deleteContact(deletedContact);
    }

    public LiveData<Boolean> getShowProgress() {
        return mRepository.getShowProgress();
    }

    public LiveData<Boolean> getProcessing() {
        return mRepository.getProcessing();
    }

    public LiveData<Integer> getShowToast() {
        return mRepository.getShowToast();
    }

    public LiveData<Boolean> getHideKeyboard() {
        return mRepository.getHideKeyboard();
    }

    public LiveData<Boolean> getPopBackStack() {
        return mRepository.getPopBackStack();
    }

    public LiveData<Integer> getMessageResId() {
        return mRepository.getMessageResId();
    }

    public void sendContactRequest(String specialistId) {
        mRepository.sendContactRequest(specialistId);
    }

    public LiveData<Map<String, Boolean>> getOutgoingContactRequests() {
        return mRepository.getOutgoingContactRequests();
    }

    public void resetUserData() {
        mRepository.resetLocalUserData();
    }
}
