package com.beproffer.beproffer.presentation;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.beproffer.beproffer.data.firebase.UserDataRepository;
import com.beproffer.beproffer.data.models.ContactItem;
import com.beproffer.beproffer.data.models.IncomingContactRequestItem;
import com.beproffer.beproffer.data.models.SpecialistGalleryImageItem;
import com.beproffer.beproffer.data.models.UserInfo;
import com.beproffer.beproffer.data.firebase.auth.FirebaseAuthLiveData;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Map;

public class UserDataViewModel extends AndroidViewModel {

    private UserDataRepository mRepository;

    private LiveData<UserInfo> mUserInfoLiveData;

    private FirebaseAuthLiveData mFirebaseAuthLiveData = new FirebaseAuthLiveData();

    public UserDataViewModel(@NonNull Application application) {
        super(application);
        mRepository = new UserDataRepository(application);

        if(mUserInfoLiveData == null)
            mUserInfoLiveData = mRepository.getUserInfoLiveData();
    }

    public LiveData<FirebaseUser> getFirebaseAuthLiveData() {
        return mFirebaseAuthLiveData;
    }

    public LiveData<UserInfo> getUserInfoLiveData() {
        return mUserInfoLiveData;
    }

    public void updateUserInfo(UserInfo updatedUserInfo, @Nullable Uri updatedImageUri) {
        mRepository.updateUserInfo(updatedUserInfo, updatedImageUri);
    }

    public LiveData<List<SpecialistGalleryImageItem>> getSpecialistGalleryData(){
        return mRepository.getSpecialistGalleryImagesList();
    }

    public void updateSpecialistGallery(SpecialistGalleryImageItem newItem, @Nullable Uri resultUri){
        mRepository.updateSpecialistGallery(newItem, resultUri);
    }

    public LiveData<Map<String, ContactItem>> getContacts(){
        return mRepository.getContacts();
    }

    public LiveData<List<IncomingContactRequestItem>> getIncomingContactRequests(){
        return mRepository.getIncomingContactRequests();
    }

    public void handleIncomingContactRequest(UserInfo currentUserInfo, IncomingContactRequestItem handledItem, boolean confirm){
        mRepository.handleIncomingContactRequest(currentUserInfo, handledItem, confirm);
    }

    public void deleteContact(ContactItem deletedContact){
        mRepository.deleteContact(deletedContact);
    }

    public LiveData<Boolean> getShowProgress(){
        return mRepository.getShowProgress();
    }

    public LiveData<Integer> getShowToast() {
        return mRepository.getShowToast();
    }

    public LiveData<Boolean> getHideKeybourd() {
        return mRepository.getHideKeyboard();
    }

    public LiveData<Boolean> getPopBackStack() {
        return mRepository.getPopBackStack();
    }

    public void resetTrigger(@Nullable Boolean toast, @Nullable Boolean keyboard, @Nullable Boolean backStack){
        /*параметр может быть или null(не трогать) или false(обнулить значение)*/
        mRepository.resetTrigger(toast, keyboard, backStack);
    }

    public void sendContactRequest(IncomingContactRequestItem incomingContactRequestItem, String specialistId){
        mRepository.sendContactRequest(incomingContactRequestItem, specialistId);
    }

    public LiveData<Map<String, Boolean>> getOutgoingContactRequests(){
        return mRepository.getOutgoingContactRequests();
    }


    public void resetUserData() {
        mRepository.resetLocalUserData();
    }

}
