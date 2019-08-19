package com.beproffer.beproffer.data.firebase;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.ContactItem;
import com.beproffer.beproffer.data.models.ContactRequestItem;
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
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class UserDataRepository {

    private final Application mApplication;

    private final DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference();

    private DataSnapshot mUserDataSnapShot;

    private String mCurrentUserId;
    private String mCurrentUserType;

    private String mRequestsObtained = "user";
    private String mContactsObtained = "user";

    private final MutableLiveData<Boolean> mShowProgress = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mProcessing = new MutableLiveData<>();
    private final MutableLiveData<Integer> mShowToast = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mHideKeyboard = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mPopBackStack = new MutableLiveData<>();

    private final MutableLiveData<UserInfo> mUserInfoLiveData = new MutableLiveData<>();

    private Map<String, SpecialistGalleryImageItem> mSpecialistGalleryImageItemsMap = new HashMap<>();
    private final MutableLiveData<Map<String, SpecialistGalleryImageItem>> mSpecialistGalleryImageItemsMapLiveData = new MutableLiveData<>();
    private final MutableLiveData<SpecialistGalleryImageItem> mEditableGalleryItemLiveData = new MutableLiveData<>();

    private Map<String, ContactItem> mContactsMap = new HashMap<>();
    private final MutableLiveData<Map<String, ContactItem>> mContactsMapLiveData = new MutableLiveData<>();
    private Map<String, ContactRequestItem> mContactRequestsMap = new HashMap<>();
    private final MutableLiveData<Map<String, ContactRequestItem>> mContactRequestsMapLiveData = new MutableLiveData<>();
    private final Map<String, Boolean> mOutgoingContactRequests = new HashMap<>();
    private final MutableLiveData<Map<String, Boolean>> mOutgoingContactRequestsLiveData = new MutableLiveData<>();

    private static final DatabaseReference OBTAIN_USER_TYPE_VIA_FIREBASE_REF = FirebaseDatabase.getInstance().getReference()
            .child(Const.USERS)
            .child(Const.SPEC);

    public UserDataRepository(Application application) {
        mApplication = application;
    }

    public LiveData<UserInfo> getUserInfoLiveData() {
        if (mUserInfoLiveData.getValue() == null) {
            mCurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            obtainUserType();
        }
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
                }
            });
        }
    }

    private void loadUserDataSnapShot() {
        mDatabaseRef.child(Const.USERS)
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
        mProcessing.setValue(true);
        if (updatedImageUri != null) {
            saveProfileImageToStorage(updatedUserInfo, updatedImageUri);
        } else {
            saveUserInfoToDb(updatedUserInfo);
        }
    }

    public LiveData<SpecialistGalleryImageItem> getEditableGalleryItem() {
        return mEditableGalleryItemLiveData;
    }

    public void setEditableGalleryItem(SpecialistGalleryImageItem editableItem) {
        mEditableGalleryItemLiveData.setValue(editableItem);
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
            mProcessing.setValue(false);
            feedBackToUi(false, R.string.toast_error_has_occurred, true, null);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        } catch (NullPointerException e) {
            mProcessing.setValue(false);
            feedBackToUi(false, R.string.toast_error_has_occurred, true, null);
        }
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = filepath.putBytes(data);
        uploadTask.addOnFailureListener(e -> {
            mProcessing.setValue(false);
            feedBackToUi(false, R.string.toast_error_has_occurred, true, null);
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
            mDatabaseRef.child(Const.USERS)
                    .child(mCurrentUserType)
                    .child(mCurrentUserId)
                    .child(Const.INFO)
                    .setValue(updatedUserInfo)
                    .addOnSuccessListener(aVoid -> {
                        mUserInfoLiveData.setValue(updatedUserInfo);
                        mProcessing.setValue(false);
                        feedBackToUi(false, R.string.toast_user_data_updated, true, true);
                    });
    }

    public LiveData<Map<String, SpecialistGalleryImageItem>> getSpecialistGalleryImagesList() {
        /*до этого не дойдет, ведь этот медод может запуститься исключительно для специалистов, но мало ли...*/
        if (!mCurrentUserType.equals(Const.SPEC)) {
            mSpecialistGalleryImageItemsMapLiveData.setValue(mSpecialistGalleryImageItemsMap);
            return mSpecialistGalleryImageItemsMapLiveData;
        }

        if (mUserDataSnapShot.hasChild(Const.IMAGES) && mUserDataSnapShot.child(Const.IMAGES).hasChildren() && mSpecialistGalleryImageItemsMap.isEmpty()) {
            obtainSpecialistGalleryImagesData();
        }
        if (!mUserDataSnapShot.hasChild(Const.IMAGES) || !mUserDataSnapShot.child(Const.IMAGES).hasChildren()) {
            mSpecialistGalleryImageItemsMapLiveData.setValue(mSpecialistGalleryImageItemsMap);
        }
        return mSpecialistGalleryImageItemsMapLiveData;
    }

    private void obtainSpecialistGalleryImagesData() {
        mShowProgress.setValue(true);
        for (DataSnapshot data : mUserDataSnapShot.child(Const.IMAGES).getChildren()) {
            try {
                mSpecialistGalleryImageItemsMap.put(data.getKey(), data.getValue(SpecialistGalleryImageItem.class));
            } catch (NullPointerException e) {
                feedBackToUi(false, R.string.toast_error_has_occurred, null, null);
            }
        }
        mSpecialistGalleryImageItemsMapLiveData.setValue(mSpecialistGalleryImageItemsMap);
        mShowProgress.setValue(false);
    }

    /*сохранение измененного или добавленного изображения проходит в несколько шагов*/
    public void updateSpecialistGallery(SpecialistGalleryImageItem updatedItem, @Nullable Uri resultUri) {
        mShowProgress.setValue(true);
        mProcessing.setValue(true);
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
            mProcessing.setValue(false);
            feedBackToUi(false, R.string.toast_error_has_occurred, true, null);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        } catch (NullPointerException e) {
            mProcessing.setValue(false);
            feedBackToUi(false, R.string.toast_error_has_occurred, true, null);
        }
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = filepath.putBytes(data);
        uploadTask.addOnFailureListener(e -> {
            mProcessing.setValue(false);
            feedBackToUi(false, R.string.toast_error_has_occurred, true, null);
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
        mDatabaseRef.child(Const.SERVICES)
                .child(updatedItem.getType())
                .child(updatedItem.getSubtype())
                .child(mCurrentUserId)
                .child(updatedItem.getKey())
                .setValue(updatedItem).addOnSuccessListener(aVoid -> {
        });
        /*3. Сохраняем данные в раздел юзера "images" */
        mDatabaseRef.child(Const.USERS)
                .child(Const.SPEC)
                .child(mCurrentUserId)
                .child(Const.IMAGES)
                .child(updatedItem.getKey())
                .setValue(updatedItem).addOnSuccessListener(aVoid -> updateSpecialistGalleryImagesData(updatedItem));
    }

    public void deleteNotRelevantImageData(SpecialistGalleryImageItem updatedItem, String primordialItemType, String primordialItemSubtype) {
        mDatabaseRef.child(Const.SERVICES)
                .child(primordialItemType)
                .child(primordialItemSubtype)
                .child(mCurrentUserId)
                .child(mSpecialistGalleryImageItemsMap.get(updatedItem.getKey()).getKey())
                .removeValue().addOnCompleteListener(task -> updateSpecialistGalleryImagesData(updatedItem));
    }

    private void updateSpecialistGalleryImagesData(SpecialistGalleryImageItem updatedItem) {
        /*4. Добавляем или заменяем данные в списке, который подается локально на RecyclerView*/
        mSpecialistGalleryImageItemsMap.put(updatedItem.getKey(), updatedItem);
        mSpecialistGalleryImageItemsMapLiveData.setValue(mSpecialistGalleryImageItemsMap);
        mProcessing.setValue(false);
        feedBackToUi(false, R.string.toast_image_data_updated, true, true);
    }

    public LiveData<Map<String, ContactItem>> getContacts() {
        mShowProgress.setValue(true);
        /*проблема в том, что когда мэп пустеет(например в следствии удаления всех контактов), то эта
        проверка опять срабатывает положительно. данные на сервере изменились, но мы, пока что не
        делаем повторного запроса, потому что имеем общий слепок юзер даты. и если, юзер, не обновляя
        данные из бд возвратится на активити контактов - то опять увидит у себя список тех контактов,
        которыч по сути в бд уже нет, они остались только в неактуальном слепке юзер даты. Поэтому,
        незная как пофиксить другим методом это говно - ввел переменную, которая будет триггером того,
        прочитаны ли данные из актуального слепка.*/
        if (mUserDataSnapShot.hasChild(Const.CONTACT) && mContactsMap.isEmpty() && !mContactsObtained.equals(mCurrentUserId)) {
            try {
                for (DataSnapshot data : mUserDataSnapShot.child(Const.CONTACT).getChildren()) {
                    mContactsMap.put(data.getValue(ContactItem.class).getContactUid(), data.getValue(ContactItem.class));
                }
                mContactsMapLiveData.setValue(mContactsMap);
                mContactsObtained = mCurrentUserId;
            } catch (NullPointerException e) {
                feedBackToUi(false, R.string.toast_error_has_occurred, null, null);
            }
        } else {
            mContactsMapLiveData.setValue(mContactsMap);
        }
        mShowProgress.setValue(false);
        return mContactsMapLiveData;
    }

    public void deleteContact(ContactItem deletedContact) {
        mShowProgress.setValue(true);
        mDatabaseRef.child(Const.USERS)
                .child(mCurrentUserType)
                .child(mCurrentUserId)
                .child(Const.CONTACT)
                .child(deletedContact.getContactUid()).removeValue().addOnSuccessListener(aVoid -> {
            mContactsMap.remove(deletedContact.getContactUid());
            mContactsMapLiveData.setValue(mContactsMap);
            mShowProgress.setValue(false);
            feedBackToUi(false, R.string.toast_contact_deleted, null, null);
        });
    }

    public LiveData<Map<String, ContactRequestItem>> getContactRequests() {
        mShowProgress.setValue(true);
        /*проблема в том, что когда мэп пустеет(например в следствии обработки специалистом всех запросов,
        что будет случатся намного чаще, чем в случае с удалением контактов), то эта
        проверка опять срабатывает положительно. данные на сервере изменились, но мы, пока что не
        делаем повторного запроса, потому что имеем общий слепок юзер даты. и если, юзер, не обновляя
        данные из бд возвратится на активити контактов - то опять увидит у себя список тех контактов,
        которые по сути в бд уже нет, они остались только в неактуальном слепке юзер даты. Поэтому,
        незная как пофиксить другим методом это говно - ввел переменную, которая будет триггером того,
        прочитаны ли данные из актуального слепка.*/
        if (mUserDataSnapShot.hasChild(Const.INREQUEST) && mContactRequestsMap.isEmpty() && !mRequestsObtained.equals(mCurrentUserId)) {
            for (DataSnapshot data : mUserDataSnapShot.child(Const.INREQUEST).getChildren()) {
                try {
                    ContactRequestItem item = data.getValue(ContactRequestItem.class);
                    mContactRequestsMap.put(item.getRequestUid(), item);
                } catch (NullPointerException e) {
                    feedBackToUi(false, R.string.toast_error_has_occurred, null, null);
                }
            }
            mContactRequestsMapLiveData.setValue(mContactRequestsMap);
            mRequestsObtained = mCurrentUserId;

        } else {
            mContactRequestsMapLiveData.setValue(mContactRequestsMap);
        }
        mShowProgress.setValue(false);
        return mContactRequestsMapLiveData;
    }

    public void handleIncomingContactRequest(ContactRequestItem handledItem, boolean confirm) {
        UserInfo currentUserInfo = mUserInfoLiveData.getValue();
        mShowProgress.setValue(true);
        if (confirm) {
            mDatabaseRef.child(Const.USERS).
                    child(handledItem.getRequestType()).
                    child(handledItem.getRequestUid()).
                    child(Const.CONTACT).
                    child(currentUserInfo.getUserId()).
                    setValue(new ContactItem(currentUserInfo.getUserId(),
                            currentUserInfo.getUserName(),
                            currentUserInfo.getUserPhone(),
                            currentUserInfo.getUserSpecialistType(),
                            currentUserInfo.getUserInfo(),
                            currentUserInfo.getUserProfileImageUrl())).addOnSuccessListener(aVoid ->
                    deleteIncomingContactRequestData(handledItem, R.string.toast_contact_confirmed))
                    .addOnFailureListener(e -> feedBackToUi(false, R.string.toast_error_has_occurred, null, null));
        } else {
            deleteIncomingContactRequestData(handledItem, R.string.toast_contact_denied);
        }
    }

    private void deleteIncomingContactRequestData(ContactRequestItem handledItem, int toastRes) {
        mDatabaseRef.child(Const.USERS)
                .child(Const.SPEC)
                .child(mCurrentUserId)
                .child(Const.INREQUEST)
                .child(handledItem.getRequestUid()).removeValue().addOnSuccessListener(aVoid -> {
                    mContactRequestsMap.remove(handledItem.getRequestUid());
                    mContactRequestsMapLiveData.setValue(mContactRequestsMap);
                    feedBackToUi(false, toastRes, null, null);
                }
        );
    }

    public void sendContactRequest(ContactRequestItem contactRequestItem, String specialistId) {
        mShowProgress.setValue(true);
        try {
            mDatabaseRef.child(Const.USERS)
                    .child(Const.SPEC)
                    .child(specialistId)
                    .child(Const.INREQUEST)
                    .child(mCurrentUserId)
                    .setValue(contactRequestItem).addOnSuccessListener(aVoid -> {
                mOutgoingContactRequests.put(specialistId, true);
                /*исходящие запросы сохраняются только в течении текущего сеанса. стоит только
                 * пользователю перезайти в приложение, и история обновляется. возможно это мы
                 * как то доработаем или измения, но это со временем*/
                mOutgoingContactRequestsLiveData.setValue(mOutgoingContactRequests);
                mShowProgress.setValue(false);
            });
        } catch (NullPointerException e) {
            mShowProgress.setValue(false);
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
        mContactRequestsMap = new HashMap<>();
        mContactRequestsMapLiveData.setValue(mContactRequestsMap);
        mContactsMap = new HashMap<>();
        mContactsMapLiveData.setValue(mContactsMap);
        mSpecialistGalleryImageItemsMap = new HashMap<>();
        mSpecialistGalleryImageItemsMapLiveData.setValue(mSpecialistGalleryImageItemsMap);
        mShowProgress.setValue(false);
    }

    public LiveData<Boolean> getShowProgress() {
        return mShowProgress;
    }

    public LiveData<Boolean> getProcessing() {
        return mProcessing;
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

    public void resetTrigger(@Nullable Boolean resetToastValue, @Nullable Boolean resetHideKeyboardValue, @Nullable Boolean resetBackStackValue) {
        if (resetToastValue != null && resetToastValue) {
            mShowToast.setValue(null);
        }
        if (resetHideKeyboardValue != null && resetHideKeyboardValue) {
            mHideKeyboard.setValue(null);
        }
        if (resetBackStackValue != null && resetBackStackValue) {
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
        /*null - не трогать, true - скрыть*/
        if (hideKeyboard != null && hideKeyboard) {
            mHideKeyboard.setValue(true);
        }
        if (popBackStack != null && popBackStack) {
            mPopBackStack.setValue(true);
        }
    }
}