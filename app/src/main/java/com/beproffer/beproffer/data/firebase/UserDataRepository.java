package com.beproffer.beproffer.data.firebase;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.ContactItem;
import com.beproffer.beproffer.data.models.IncomingContactRequestItem;
import com.beproffer.beproffer.data.models.SpecialistGalleryImageItem;
import com.beproffer.beproffer.data.models.UserInfo;
import com.beproffer.beproffer.util.Const;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class UserDataRepository {

    private Application mApplication;

    private DataSnapshot mUserDataSnapShot;

    private String mCurrentUserId;
    private String mCurrentUserType;

    private MutableLiveData<Boolean> mShowProgress = new MutableLiveData<>();
    private MutableLiveData<Integer> mShowToast = new MutableLiveData<>();
    private MutableLiveData<Boolean> mHideKeyboard = new MutableLiveData<>();
    private MutableLiveData<Boolean> mPopBackStack = new MutableLiveData<>();

    private MutableLiveData<UserInfo> mUserInfoLiveData = new MutableLiveData<>();

    private List<SpecialistGalleryImageItem> mSpecialistGalleryImageItemsList = new ArrayList<>();
    private MutableLiveData<List<SpecialistGalleryImageItem>> mSpecialistGalleryImageItemsListLiveData = new MutableLiveData<>();

    private Map<String, ContactItem> mContactsMap = new HashMap<>();
    private MutableLiveData<Map<String, ContactItem>> mContactsMapLiveData = new MutableLiveData<>();
    private List<IncomingContactRequestItem> mIncomingContactRequestsList = new ArrayList<>();
    private MutableLiveData<List<IncomingContactRequestItem>> mIncomingContactRequestsListLiveData = new MutableLiveData<>();
    private Map<String, Boolean> mOutgoingContactRequests = new HashMap<>();
    private MutableLiveData<Map<String, Boolean>> mOutgoingContactRequestsLiveData = new MutableLiveData<>();

    private static final DatabaseReference OBTAIN_USER_TYPE_VIA_FIREBASE_REF = FirebaseDatabase.getInstance().getReference()
            .child(Const.USERS)
            .child(Const.SPEC);

    public UserDataRepository(Application application) {
        mApplication = application;
        mCurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        obtainUserType();
    }

    public LiveData<UserInfo> getUserInfoLiveData() {
        return mUserInfoLiveData;
    }

    private void obtainUserType() {
        mShowProgress.setValue(true);
        // получать тип юзера приходится для того, чтобы правильно построить путь в базу данных к ветке с данными юзера.
        mCurrentUserType = mApplication.getApplicationContext().getSharedPreferences(mCurrentUserId, MODE_PRIVATE)
                .getString(Const.USERTYPE, null);

        if (mCurrentUserType != null) {
            loadUserDataSnapShot();
        } else {
            OBTAIN_USER_TYPE_VIA_FIREBASE_REF.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.hasChild(mCurrentUserId)) {
                            mCurrentUserType = Const.SPEC;
                            saveUserType(Const.SPEC);
                        } else {
                            mCurrentUserType = Const.CUST;
                            saveUserType(Const.CUST);
                        }
                        if (mCurrentUserType != null)
                            loadUserDataSnapShot();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    mShowProgress.setValue(false);
                    Log.d(Const.ERROR, "Cant define currentUserType via firebase");
                }
            });
        }
    }

    private void loadUserDataSnapShot() {
        FirebaseDatabase.getInstance().getReference()
                .child(Const.USERS)
                .child(mCurrentUserType)
                .child(mCurrentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(Const.INFO)) {
                    mUserDataSnapShot = dataSnapshot;
                    mUserInfoLiveData.setValue(mUserDataSnapShot.child(Const.INFO).getValue(UserInfo.class));
                    mShowProgress.setValue(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mShowProgress.setValue(false);
            }
        });
    }

    private void saveUserType(String currentUserType) {
        /*тип юзера сохраняется, чтобы при повторном входе не приходилось его определять через бд,
         а можно было взять из SharedPref.*/
        mApplication.getApplicationContext().getApplicationContext()
                .getSharedPreferences(mCurrentUserId, MODE_PRIVATE).edit()
                .putString(Const.USERTYPE, currentUserType).apply();
    }

    public void updateUserInfo(UserInfo updatedUserInfo, @Nullable Uri updatedImageUri) {
        mShowProgress.setValue(true);
        if (updatedImageUri != null) {
            saveProfileImageToStorage(updatedUserInfo, updatedImageUri);
        } else {
            saveUserInfoToDb(updatedUserInfo);
        }
    }

    private void saveProfileImageToStorage(UserInfo updatedUserInfo, Uri updatesImageUri) {
        /*в сохранении изображения сервиса для специалиста и изображения профайла присутствует много
         * одинакового кода. пока что я незнаю как решить этот вопрос, так как в илучае успешного сохранения
         * изображения дальше должен исполнятся разный код а на вход должны подаваться разные объекты*/
        StorageReference filepath = FirebaseStorage.getInstance().getReference()
                .child(Const.PROF)
                .child(updatedUserInfo.getUserType())
                .child(updatedUserInfo.getUserId())
                .child(updatedUserInfo.getUserId());
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(mApplication.getContentResolver(), updatesImageUri);
        } catch (IOException e) {
            feedBackToUi(false, R.string.toast_error_has_occurred, true, false);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        } catch (NullPointerException e) {
            feedBackToUi(false, R.string.toast_error_has_occurred, true, false);
        }
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = filepath.putBytes(data);
        uploadTask.addOnFailureListener(e -> {
            feedBackToUi(false, R.string.toast_error_has_occurred, true, false);
        });
        uploadTask.addOnSuccessListener(taskSnapshot -> filepath.getDownloadUrl()
                .addOnSuccessListener(url -> {
                    updatedUserInfo.setUserProfileImageUrl(url.toString());
                    saveUserInfoToDb(updatedUserInfo);
                }));
    }

    private void saveUserInfoToDb(UserInfo updatedUserInfo) {
        /*изменяем инфо юзера в базе данных. если из базы приходит ответ об удачной замене, то меняем
         инфо юзера локально, чтобы не делать повторный запрос в базу за обновленными данными*/
        if (mCurrentUserId != null && mCurrentUserType != null)
            FirebaseDatabase.getInstance().getReference()
                    .child(Const.USERS)
                    .child(mCurrentUserType)
                    .child(mCurrentUserId)
                    .child(Const.INFO)
                    .setValue(updatedUserInfo)
                    .addOnSuccessListener(aVoid -> {
                        mUserInfoLiveData.setValue(updatedUserInfo);
                        feedBackToUi(false, R.string.toast_user_data_updated, true, null);
                    });
    }

    public LiveData<List<SpecialistGalleryImageItem>> getSpecialistGalleryImagesList() {
        mShowProgress.setValue(true);
        if (mCurrentUserType.equals(Const.SPEC)
                && mUserDataSnapShot.hasChild(Const.IMAGES)
                && mSpecialistGalleryImageItemsList.isEmpty()) {
            obtainSpecialistGalleryImagesData();
        }

        return mSpecialistGalleryImageItemsListLiveData;
    }

    private void obtainSpecialistGalleryImagesData() {
        for (DataSnapshot data : mUserDataSnapShot.child(Const.IMAGES).getChildren()) {
            mSpecialistGalleryImageItemsList.add(data.getValue(SpecialistGalleryImageItem.class));
        }
        mSpecialistGalleryImageItemsListLiveData.setValue(mSpecialistGalleryImageItemsList);
        mShowProgress.setValue(false);
    }

    /*сохранение измененного или добавленного изображения проходит в несколько шагов*/
    public void updateSpecialistGallery(SpecialistGalleryImageItem updatedItem, @Nullable Uri resultUri) {
        mShowProgress.setValue(true);
        /*1. Если изображение изменялось и Uri != null,то сначала сохраняем новое изображение в Storage*/
        if (resultUri != null) {
            saveServiceImageToStorage(updatedItem, resultUri);
        } else {
            saveImageDataToRealtimeDb(updatedItem, null);
        }
    }

    private void saveServiceImageToStorage(SpecialistGalleryImageItem updatedItem, Uri resultUri) {
        /*в сохранении изображения сервиса для специалиста и изображения профайла присутствует много
         * одинакового кода. пока что я незнаю как решить этот вопрос, так как в илучае успешного сохранения
         * изображения дальше должен исполнятся разный код а на вход должны подаваться разные объекты*/
        StorageReference filepath = FirebaseStorage.getInstance().getReference()
                .child(Const.SERV)
                .child(mCurrentUserId)
                .child(updatedItem.getKey());
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(mApplication.getContentResolver(), resultUri);
        } catch (IOException e) {
            feedBackToUi(false, R.string.toast_error_has_occurred, true, false);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        } catch (NullPointerException e) {
            feedBackToUi(false, R.string.toast_error_has_occurred, true, false);
        }
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = filepath.putBytes(data);
        uploadTask.addOnFailureListener(e -> {
            feedBackToUi(false, R.string.toast_error_has_occurred, true, false);
        });
        uploadTask.addOnSuccessListener(taskSnapshot -> filepath.getDownloadUrl()
                .addOnSuccessListener(url -> saveImageDataToRealtimeDb(updatedItem, url.toString())));
    }

    private void saveImageDataToRealtimeDb(SpecialistGalleryImageItem updatedItem, @Nullable String url) {
        /*выключени прогресс бара. по сути - запроса отправляется 2, но выключение крутилки подвязано
         только под завершение одного из них */
        if (url != null) {
            updatedItem.setUrl(url);
        }
        if (updatedItem.getUid() == null) {
            updatedItem.setUid(mCurrentUserId);
        }
        /*2. Сохраняем данные в раздел "services"*/
        FirebaseDatabase.getInstance().getReference()
                .child(Const.SERVICES)
                .child(updatedItem.getType())
                .child(updatedItem.getSubtype())
                .child(mCurrentUserId)
                .child(updatedItem.getKey())
                .setValue(updatedItem).addOnSuccessListener(aVoid -> {
        });
        /*3. Сохраняем данные в раздел юзера "images" */
        FirebaseDatabase.getInstance().getReference()
                .child(Const.USERS)
                .child(Const.SPEC)
                .child(mCurrentUserId)
                .child(Const.IMAGES)
                .child(updatedItem.getKey())
                .setValue(updatedItem).addOnSuccessListener(aVoid -> updateSpecialistGalleryImagesData(updatedItem));
    }

    private void updateSpecialistGalleryImagesData(SpecialistGalleryImageItem updatedItem) {
        /*4. Добавляем или заменяем данные в списке, который подается локально на RecyclerView*/
        /*может со временем это дело переделать через ХэшМэп и просто удалять или заменять по ключу*/
        boolean replacementOccured = false;
        for (int i = 0; i < mSpecialistGalleryImageItemsList.size(); i++) {
            if (mSpecialistGalleryImageItemsList.get(i).getKey().equals(updatedItem.getKey())) {
                mSpecialistGalleryImageItemsList.set(i, updatedItem);
                replacementOccured = true;
            }
        }
        if (!replacementOccured && mSpecialistGalleryImageItemsList.size() < 5) {
            mSpecialistGalleryImageItemsList.add(updatedItem);
            return;
        }
        mSpecialistGalleryImageItemsListLiveData.setValue(mSpecialistGalleryImageItemsList);
        feedBackToUi(false, R.string.toast_image_data_updated, true, true);
    }

    public LiveData<Map<String, ContactItem>> getContacts() {
        mShowProgress.setValue(true);
        if (mUserDataSnapShot.hasChild(Const.CONTACT) && mContactsMap.isEmpty()) {
            try {
                for (DataSnapshot data : mUserDataSnapShot.child(Const.CONTACT).getChildren()) {
                    mContactsMap.put(data.getValue(ContactItem.class).getContactUid(), data.getValue(ContactItem.class));
                }
                mContactsMapLiveData.setValue(mContactsMap);
            } catch (NullPointerException e) {
                feedBackToUi(false, R.string.toast_error_has_occurred, false, false);
            }
        }
        mShowProgress.setValue(false);
        return mContactsMapLiveData;
    }

    public void deleteContact(ContactItem deletedContact) {
        mShowProgress.setValue(true);
        FirebaseDatabase.getInstance().getReference()
                .child(Const.USERS)
                .child(mCurrentUserType)
                .child(mCurrentUserId)
                .child(Const.CONTACT)
                .child(deletedContact.getContactUid()).removeValue().addOnSuccessListener(aVoid -> {
            mContactsMap.remove(deletedContact.getContactUid());
            mContactsMapLiveData.setValue(mContactsMap);
            mShowProgress.setValue(false);
        });
    }

    public LiveData<List<IncomingContactRequestItem>> getIncomingContactRequests() {
        mShowProgress.setValue(true);
        if (mUserDataSnapShot.hasChild(Const.INREQUEST) && mIncomingContactRequestsList.isEmpty()) {
            for (DataSnapshot data : mUserDataSnapShot.child(Const.INREQUEST).getChildren()) {
                mIncomingContactRequestsList.add(data.getValue(IncomingContactRequestItem.class));
            }
            mIncomingContactRequestsListLiveData.setValue(mIncomingContactRequestsList);
        }
        mShowProgress.setValue(false);
        return mIncomingContactRequestsListLiveData;
    }

    public void handleIncomingContactRequest(UserInfo currentUserInfo, IncomingContactRequestItem handledItem, boolean confirm) {
        mShowProgress.setValue(true);
        if (confirm) {
            FirebaseDatabase.getInstance().getReference().child(Const.USERS).
                    child(handledItem.getRequestType()).
                    child(handledItem.getRequestUid()).
                    child(Const.CONTACT).
                    child(currentUserInfo.getUserId()).
                    setValue(new ContactItem(currentUserInfo.getUserId(),
                            currentUserInfo.getUserName(),
                            currentUserInfo.getUserPhone(),
                            currentUserInfo.getUserInfo(),
                            currentUserInfo.getUserProfileImageUrl())).addOnSuccessListener(aVoid -> deleteIncomingContactRequestData(handledItem));
        } else {
            deleteIncomingContactRequestData(handledItem);
        }
    }

    private void deleteIncomingContactRequestData(IncomingContactRequestItem handledItem) {
        FirebaseDatabase.getInstance().getReference()
                .child(Const.USERS)
                .child(Const.SPEC)
                .child(mCurrentUserId)
                .child(Const.INREQUEST)
                .child(handledItem.getRequestUid()).removeValue().addOnSuccessListener(aVoid -> {
                    for (int i = 0; i < mIncomingContactRequestsList.size(); i++) {
                        if (mIncomingContactRequestsList.get(i).getRequestUid().equals(handledItem.getRequestUid())) {
                            mIncomingContactRequestsList.remove(i);
                            return;
                        }
                    }
                    mIncomingContactRequestsListLiveData.setValue(mIncomingContactRequestsList);
                    mShowProgress.setValue(false);
                }
        );
    }

    public void sendContactRequest(IncomingContactRequestItem incomingContactRequestItem, String specialistId) {
        mShowProgress.setValue(true);
        try {
            FirebaseDatabase.getInstance().getReference()
                    .child(Const.USERS)
                    .child(Const.SPEC)
                    .child(specialistId)
                    .child(Const.INREQUEST)
                    .child(mCurrentUserId)
                    .setValue(incomingContactRequestItem).addOnSuccessListener(aVoid -> {
                mOutgoingContactRequests.put(specialistId, true);
                /*исходящие запросы сохраняются только в течении текущего сеанса. стоит только
                 * пользователю перезайти в приложение, и история обновляется. возможно это мы
                 * как то доработаем или измения, но это со временем*/
                mOutgoingContactRequestsLiveData.setValue(mOutgoingContactRequests);
                mShowProgress.setValue(false);
            });
        } catch (NullPointerException e) {
            mShowProgress.setValue(false);
            Log.d(Const.ERROR, "UserDataRepository sendContactRequest");
        }
    }

    public LiveData<Map<String, Boolean>> getOutgoingContactRequests() {
        return mOutgoingContactRequestsLiveData;
    }

    public void resetLocalUserData() {
        mShowProgress.setValue(true);
        mCurrentUserId = null;
        mCurrentUserType = null;
        mUserInfoLiveData.setValue(null);
        mUserDataSnapShot = null;
        mIncomingContactRequestsList = new ArrayList<>();
        mIncomingContactRequestsListLiveData.setValue(mIncomingContactRequestsList);
        mContactsMap = new HashMap<>();
        mContactsMapLiveData.setValue(mContactsMap);
        mSpecialistGalleryImageItemsList = new ArrayList<>();
        mSpecialistGalleryImageItemsListLiveData.setValue(mSpecialistGalleryImageItemsList);
        mShowProgress.setValue(false);
    }

    public LiveData<Boolean> getShowProgress() {
        return mShowProgress;
    }

    public LiveData<Integer> getShowToast() {
        return mShowToast;
    }

    public LiveData<Boolean> getHideKeyboard() {
        return mHideKeyboard;
    }

    public LiveData<Boolean> getPopBackStack() {
        return mPopBackStack;
    }

    public void resetTrigger(@Nullable Boolean toast, @Nullable Boolean keyboard, @Nullable Boolean backStack) {
        /*параметр может быть или null(не трогать) или false(обнулить значение)*/
        if (toast != null) {
            mShowToast.setValue(null);
        }
        if (keyboard != null) {
            mHideKeyboard.setValue(null);
        }
        if (backStack != null) {
            mPopBackStack.setValue(null);
        }
    }

    private void feedBackToUi(@Nullable Boolean showProgress,
                              @Nullable Integer toastResId,
                              @Nullable Boolean hideKeyboard,
                              @Nullable Boolean popBackStack) {
        if (showProgress != null) {
            mShowProgress.setValue(showProgress);
        }
        if (toastResId != null) {
            mShowToast.setValue(toastResId);
        }
        if (hideKeyboard != null) {
            mHideKeyboard.setValue(true);
        }
        if (popBackStack != null) {
            mPopBackStack.setValue(true);
        }
    }
}