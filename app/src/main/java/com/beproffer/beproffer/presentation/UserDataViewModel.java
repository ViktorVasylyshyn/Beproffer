package com.beproffer.beproffer.presentation;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.beproffer.beproffer.data.firebase.UserDataRepository;
import com.beproffer.beproffer.data.models.ContactItem;
import com.beproffer.beproffer.data.models.ContactRequestItem;
import com.beproffer.beproffer.data.models.SpecialistGalleryImageItem;
import com.beproffer.beproffer.data.models.UserInfo;

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

    public LiveData<Map<String, SpecialistGalleryImageItem>> getSpecialistGalleryData() {
        return mRepository.getSpecialistGalleryImagesList();
    }

    public void updateSpecialistGallery(SpecialistGalleryImageItem newItem, @Nullable Uri resultUri) {
        mRepository.updateSpecialistGallery(newItem, resultUri);
    }

    public LiveData<SpecialistGalleryImageItem> getEditableGalleryItem() {
        return mRepository.getEditableGalleryItem();
    }

    public void setEditableGalleryItem(SpecialistGalleryImageItem editableItem) {
        mRepository.setEditableGalleryItem(editableItem);
    }

    public void deleteNotRelevantImageData(SpecialistGalleryImageItem updatedItem, String primordialItemType, String primordialItemSubtype) {
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

    public void resetTrigger(@Nullable Boolean resetToastValue, @Nullable Boolean resetHideKeyboardValue, @Nullable Boolean resetBackStackValue) {
        /*параметр может быть или null(не трогать) или true(обнулить значение)*/
        mRepository.resetTrigger(resetToastValue, resetHideKeyboardValue, resetBackStackValue);
    }

    public void sendContactRequest(ContactRequestItem contactRequestItem, String specialistId) {
        mRepository.sendContactRequest(contactRequestItem, specialistId);
    }

    public LiveData<Map<String, Boolean>> getOutgoingContactRequests() {
        return mRepository.getOutgoingContactRequests();
    }


    public void resetUserData() {
        mRepository.resetLocalUserData();
    }
}
