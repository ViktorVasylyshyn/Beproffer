package com.beproffer.beproffer.data.firebase;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.BrowsingImageItem;
import com.beproffer.beproffer.data.models.BrowsingItemRef;
import com.beproffer.beproffer.data.models.ContactItem;
import com.beproffer.beproffer.data.models.ContactRequestItem;
import com.beproffer.beproffer.data.models.UserInfo;
import com.beproffer.beproffer.util.Const;
import com.beproffer.beproffer.util.LocalizationConstants;
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

/**
 * Репозиторий, отвечающий за подгрузку снимка данных пользователя. На данный момент нет слушателя и
 * данные подгружаются один раз, при старте сеанса. Содержит персональную информацию, ссылки на
 * подтвержденные контакты + если пользователь специалист - ссылки на запросы контактов и объекты сервисов.
 * Содержит методы позволяющие редактировать данные пользователя и данные сервисов(если специалист),
 * отсылать и подтверждать запросы о контактах.
 */

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
    private final MutableLiveData<Integer> mMessageResId = new MutableLiveData<>();

    private final MutableLiveData<UserInfo> mUserInfoLiveData = new MutableLiveData<>();

    private Map<String, BrowsingImageItem> mLocalImageItemsMap = new HashMap<>();
    private final MutableLiveData<Map<String, BrowsingImageItem>> mSpecialistGalleryImageItemsMapLiveData = new MutableLiveData<>();
    private final MutableLiveData<BrowsingImageItem> mEditableGalleryItemLiveData = new MutableLiveData<>();

    private Map<String, ContactItem> mContactsMap = new HashMap<>();
    private final MutableLiveData<Map<String, ContactItem>> mContactsMapLiveData = new MutableLiveData<>();
    private Map<String, ContactRequestItem> mContactRequestsMap = new HashMap<>();
    private final MutableLiveData<Map<String, ContactRequestItem>> mContactRequestsMapLiveData = new MutableLiveData<>();
    private final Map<String, Boolean> mOutgoingContactRequests = new HashMap<>();
    private final MutableLiveData<Map<String, Boolean>> mOutgoingContactRequestsLiveData = new MutableLiveData<>();

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

    /*Данные о специалистах и клиентая находятся в разных ветках Realtime Database. тип юзера нужен
     для правильного построения пути к его данным. Если по каким то причинам типа нет в SharedPref
    * то нужно его определить по базе данных.*/
    private void obtainUserType() {
        mShowProgress.setValue(true);
        mCurrentUserType = mApplication.getApplicationContext().getSharedPreferences(mCurrentUserId, MODE_PRIVATE)
                .getString(Const.USERTYPE, null);

        if (mCurrentUserType != null) {
            loadUserDataSnapShot();
        } else {
            mDatabaseRef.child(Const.USERS).child(Const.SPEC).addListenerForSingleValueEvent(new ValueEventListener() {
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
                    feedBackToUi(false, R.string.toast_error_has_occurred,
                            false, false, false, R.string.message_error_has_occurred);
                    Log.d(Const.ERROR, "obtainUserType: " + databaseError.getMessage());
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
                feedBackToUi(false, R.string.toast_error_has_occurred,
                        false, false, false, R.string.message_error_has_occurred);
                Log.d(Const.ERROR, "loadUserDataSnapShot: " + databaseError.getMessage());
            }
        });
    }

    private void saveUserType(String currentUserType) {
        /* чтобы постоянно не искать по базе после первого поиска - сохраняем в SharedPref, чтобы значение было локально*/
        mApplication.getApplicationContext().getApplicationContext()
                .getSharedPreferences(mCurrentUserId, MODE_PRIVATE).edit()
                .putString(Const.USERTYPE, currentUserType).apply();
    }

    public void updateUserInfo(UserInfo updatedUserInfo, @Nullable Uri updatedImageUri) {
        mShowProgress.setValue(true);
        mProcessing.setValue(true);
        if (updatedImageUri != null) {
            saveImageToStorage(FirebaseStorage.getInstance().getReference()
                            .child(Const.PROF)
                            .child(updatedUserInfo.getType())
                            .child(updatedUserInfo.getId())
                            .child(updatedUserInfo.getId()),
                    updatedUserInfo,
                    null,
                    updatedImageUri,
                    Const.PROFILE_IMAGE_BITMAP_QUALITY);
        } else {
            saveUserInfoToRealTimeDb(updatedUserInfo);
        }
    }

    /*метод обрабатывает сохранение как изображений профайла, так и изображений сервисов, зависит от поданных параметров*/
    /*только один объект может одновременно подаваться как аргумент - или UserInfo или BrowsingImageItem*/
    private void saveImageToStorage(StorageReference filepath,
                                    @Nullable UserInfo updatedUserInfo,
                                    @Nullable BrowsingImageItem updatedServiceItem,
                                    Uri updatesImageUri,
                                    int bitmapQuality) {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(mApplication.getContentResolver(), updatesImageUri);
        } catch (IOException e) {
            feedBackToUi(false, R.string.toast_error_has_occurred, true,
                    false, false, R.string.message_error_has_occurred);
            Log.d(Const.ERROR, "saveImageToStorage: " + e.getMessage());
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, bitmapQuality, baos);
        } catch (NullPointerException e) {
            feedBackToUi(false, R.string.toast_error_has_occurred, true,
                    false, false, R.string.message_error_has_occurred);
            Log.d(Const.ERROR, "saveImageToStorage: " + e.getMessage());
        }
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = filepath.putBytes(data);
        uploadTask.addOnFailureListener(e -> {
            feedBackToUi(false, R.string.toast_error_has_occurred, true,
                    false, false, R.string.message_error_has_occurred);
            Log.d(Const.ERROR, "saveImageToStorage: " + e.getMessage());
        });
        uploadTask.addOnSuccessListener(taskSnapshot -> filepath.getDownloadUrl()
                .addOnSuccessListener(url -> {
                    if (updatedUserInfo != null) {
                        updatedUserInfo.setProfileImageUrl(url.toString());
                        saveUserInfoToRealTimeDb(updatedUserInfo);
                    } else if (updatedServiceItem != null) {
                        saveImageDataToRealtimeDb(updatedServiceItem, url.toString());
                    }
                }).addOnFailureListener(e -> {
                    feedBackToUi(false, R.string.toast_error_has_occurred,
                            false, false, false, R.string.message_error_has_occurred);
                    Log.d(Const.ERROR, "saveImageToStorage: " + e.getMessage());
                })
        );
    }

    private void saveUserInfoToRealTimeDb(UserInfo updatedUserInfo) {
        if (mCurrentUserId != null && mCurrentUserType != null)
            mDatabaseRef.child(Const.USERS)
                    .child(mCurrentUserType)
                    .child(mCurrentUserId)
                    .child(Const.INFO)
                    .setValue(updatedUserInfo)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            mUserInfoLiveData.setValue(updatedUserInfo);
                            feedBackToUi(false, R.string.toast_user_data_updated, true,
                                    true, false, null);
                        } else {
                            feedBackToUi(false, R.string.toast_error_has_occurred, true,
                                    false, false, R.string.message_error_has_occurred);
                            Log.d(Const.ERROR, "saveUserInfoToRealTimeDb: " + task.getException().getMessage());
                        }
                    });
    }

    public LiveData<Map<String, BrowsingImageItem>> getSpecialistGalleryImagesList() {
        /*до этого не дойдет, ведь этот медод может запуститься исключительно для специалистов, но мало ли...*/
        if (!mCurrentUserType.equals(Const.SPEC)) {
            mSpecialistGalleryImageItemsMapLiveData.setValue(mLocalImageItemsMap);
            return mSpecialistGalleryImageItemsMapLiveData;
        }

        if (mUserDataSnapShot.hasChild(Const.SERVICES) && mUserDataSnapShot.child(Const.SERVICES).hasChildren() && mLocalImageItemsMap.isEmpty()) {
            obtainSpecialistGalleryImagesData();
        }
        if (!mUserDataSnapShot.hasChild(Const.SERVICES) || !mUserDataSnapShot.child(Const.SERVICES).hasChildren()) {
            mSpecialistGalleryImageItemsMapLiveData.setValue(mLocalImageItemsMap);
        }
        return mSpecialistGalleryImageItemsMapLiveData;
    }

    private void obtainSpecialistGalleryImagesData() {
        mShowProgress.setValue(true);
        for (DataSnapshot data : mUserDataSnapShot.child(Const.SERVICES).getChildren()) {
            try {
                mLocalImageItemsMap.put(data.getKey(), data.getValue(BrowsingImageItem.class));
            } catch (NullPointerException e) {
                /*TODO такого, правда еще не случалось, но если вдруг то - отображать объект как
                    испорченый, чтобы пользователь его перезаписал. или может вообще его удалять*/
                feedBackToUi(false, R.string.toast_error_has_occurred,
                        false, false, false, null);
                Log.d(Const.ERROR, "obtainSpecialistGalleryImagesData: " + e.getMessage());
            }
        }
        mSpecialistGalleryImageItemsMapLiveData.setValue(mLocalImageItemsMap);
        mShowProgress.setValue(false);
    }

    /*сохранение измененного или добавленного изображения проходит в несколько шагов*/
    public void updateSpecialistGallery(BrowsingImageItem updatedServiceItem, @Nullable Uri resultUri) {
        mShowProgress.setValue(true);
        mProcessing.setValue(true);
        /*1. Если изображение изменялось и Uri != null,то сначала сохраняем новое изображение в Storage*/
        if (resultUri != null) {
            saveImageToStorage(FirebaseStorage.getInstance().getReference()
                            .child(Const.SERV)
                            .child(mCurrentUserId)
                            .child(updatedServiceItem.getKey()),
                    null,
                    updatedServiceItem,
                    resultUri,
                    Const.SERVICE_IMAGE_BITMAP_QUALITY);
        } else {
            saveImageDataToRealtimeDb(updatedServiceItem, null);
        }
    }

    private void saveImageDataToRealtimeDb(BrowsingImageItem updatedItem, @Nullable String url) {
        if (url != null) {
            updatedItem.setUrl(url);
        }
        if (updatedItem.getId() == null) {
            updatedItem.setId(mCurrentUserId);
        }
        /*2. Сохраняем данные в ветку юзера "images" */
        mDatabaseRef.child(Const.USERS)
                .child(Const.SPEC)
                .child(mCurrentUserId)
                .child(Const.SERVICES)
                .child(updatedItem.getKey())
                .setValue(updatedItem).addOnCompleteListener(imgTask -> {
            if (imgTask.isSuccessful()) {
                /*3. Сохраняем ссылку на данные в раздел "services".*/
                /*TODO после введения локализации - формировать этот запрос с учетом локации пользователя.*/
                mDatabaseRef.child(Const.SERVICES)
                        .child(LocalizationConstants.UKRAINE)
                        .child(LocalizationConstants.KYIV_REGION)
                        .child(LocalizationConstants.KYIV)
                        .child(updatedItem.getType())
                        .child(updatedItem.getSubtype())
                        .child(updatedItem.getKey())
                        .setValue(new BrowsingItemRef(mCurrentUserId
                                , updatedItem.getKey()
                                , updatedItem.getGender()
                                , updatedItem.getUrl()
                        )).addOnSuccessListener(aVoid -> updateLocalImageItemsMap(updatedItem))
                        .addOnFailureListener(e -> {
                            Log.d(Const.ERROR, "saveImageDataToRealtimeDb: " + e.getMessage());
                            feedBackToUi(false, R.string.toast_error_has_occurred,
                                    false, false, false, R.string.message_error_has_occurred);
                        });
            } else {
                feedBackToUi(false, R.string.toast_error_has_occurred,
                        false, false, false, R.string.message_error_has_occurred);
                Log.d(Const.ERROR, "saveImageDataToRealtimeDb: " + imgTask.getException().getMessage());
            }
        });
    }

    /*На случай, если специалист изменял тип услуги. тогда ссылка на услугу в ветке "services, будет
    перезаписана в новую ветку, а старую нужно удалить."*/
    public void deleteNotRelevantImageData(BrowsingImageItem updatedItem, String primordialItemType, String primordialItemSubtype) {
        try {
            /*TODO после введения локализации - формировать этот запрос с учетом локации пользователя.*/
            mDatabaseRef.child(Const.SERVICES)
                    .child(LocalizationConstants.UKRAINE)
                    .child(LocalizationConstants.KYIV_REGION)
                    .child(LocalizationConstants.KYIV)
                    .child(primordialItemType)
                    .child(primordialItemSubtype)
                    .child(updatedItem.getKey())
                    .removeValue().addOnCompleteListener(task -> updateLocalImageItemsMap(updatedItem));
        } catch (NullPointerException e) {
            Log.d(Const.ERROR, "deleteNotRelevantImageData: " + e.getMessage());
        }
    }

    private void updateLocalImageItemsMap(BrowsingImageItem updatedItem) {
        /*4. Добавляем или заменяем данные в списке, который подается локально на RecyclerView,
        потому что нет отслеживания в реальном времени. объекты сервисов специалиста находятся в
        snapshot которые взялся разово. соответственно - заменили в базе - нужно заменить и локально.
        в будущем - переделать.*/
        mLocalImageItemsMap.put(updatedItem.getKey(), updatedItem);
        mSpecialistGalleryImageItemsMapLiveData.setValue(mLocalImageItemsMap);
        feedBackToUi(false, R.string.toast_image_data_updated, true,
                true, false, null);
    }

    public LiveData<Map<String, ContactItem>> getContacts() {
        mShowProgress.setValue(true);
        /*Весь слепок информации данного пользователя находится в mUserDataSnapShot. В том числе и
         контакты. Мы, на даном этапе не вешаем слушатель на данные пользователя, а подгружаем их единым
         слепком при входе в систему. Если пользователь решит удалять контакты - то в базе данных
         они удалятся, но в слепке останутя. Чтобы нижестоящее условие не срабоатывало, при
          повторном входе на фрагмент контактов и не показывались контакты
         которых нет в бд, но еще есть в слепке - введена переменная mContactsObtained.*/
        if (mUserDataSnapShot.hasChild(Const.CONTACT) && mContactsMap.isEmpty() && !mContactsObtained.equals(mCurrentUserId)) {
            for (DataSnapshot data : mUserDataSnapShot.child(Const.CONTACT).getChildren()) {
                try {

                    mDatabaseRef.child(Const.USERS).child(Const.SPEC).child(data.getKey()).child(Const.INFO)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            /*Если при создании объекта ContactItem будет краш у тех специалистов, которые не добавили
                             изображение, и у них в описании нет profileImageUrl ключа - то обязать их
                             загружать изображение профайла, перед тем как разрешать подтверждать запросы*/
                                    if (dataSnapshot.exists()) {
                                        try {
                                            mContactsMap.put(dataSnapshot.getValue(ContactItem.class).getId()
                                                    , dataSnapshot.getValue(ContactItem.class));
                                        } catch (NullPointerException e) {
                                            Log.d(Const.ERROR, "getContacts.onDataChange: " + e.getMessage());
                                        }
                                        mContactsMapLiveData.setValue(mContactsMap);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    /*TODO подавать в mContactsMap объект ContactItem только с полем id и отображать.
                                     * в recycler view этот объект, так, как будто его нет - что то типа:
                                     * ("не можем получить данные специалиста. возможно они удалены или непрвильны")
                                     * это должно подталкивать юзера к удалению этого не валидного id*/

                                    feedBackToUi(false, R.string.toast_error_has_occurred,
                                            false, false, false, null);
                                    Log.d(Const.ERROR, "getContacts.onCancelled: " + databaseError.getMessage());
                                }
                            });
                } catch (NullPointerException e) {
                    feedBackToUi(false, R.string.toast_error_has_occurred,
                            false, false, false, R.string.message_error_has_occurred);
                    Log.d(Const.ERROR, "getContacts: " + e.getMessage());
                }
            }
            mContactsObtained = mCurrentUserId;
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
                .child(deletedContact.getId()).removeValue().addOnSuccessListener(aVoid -> {
            mContactsMap.remove(deletedContact.getId());
            mContactsMapLiveData.setValue(mContactsMap);
            mShowProgress.setValue(false);
            feedBackToUi(false, R.string.toast_contact_deleted, false, false, false, null);
        });
    }

    public LiveData<Map<String, ContactRequestItem>> getContactRequests() {
        mShowProgress.setValue(true);
        /*Весь слепок информации данного пользователя находится в mUserDataSnapShot. В том числе и запросы
         контактов(если текущий пользователь - специалст). Мы, на даном этапе не вешаем слушатель на данные
         пользователя, а подгружаем их единым слепком при входе в систему. Когда все запросы обработаны - то в базе данных
         их нет, но в слепке они остались. Чтобы нижестоящее условие не срабоатывало при повторном
         входе в раздел запросов и не показывались запросы
         которых нет в бд, но еще есть в слепке - введена переменная mRequestsObtained.*/
        if (mUserDataSnapShot.hasChild(Const.INREQUEST) && mContactRequestsMap.isEmpty() && !mRequestsObtained.equals(mCurrentUserId)) {
            for (DataSnapshot data : mUserDataSnapShot.child(Const.INREQUEST).getChildren()) {
                try {
                    mDatabaseRef.child(Const.USERS).child(data.getValue().toString()).child(data.getKey()).child(Const.INFO)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                /*TODO обратить внимание, будет ли крашится, если в инфо
                                   запрашиваемого пользователя нет ключа с url изображения*/
                                        mContactRequestsMap.put(dataSnapshot.getValue(ContactRequestItem.class).getId()
                                                , dataSnapshot.getValue(ContactRequestItem.class));
                                        mContactRequestsMapLiveData.setValue(mContactRequestsMap);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.d(Const.ERROR, "getContactRequests.onCancelled: " + databaseError.getMessage());
                                }
                            });
                } catch (NullPointerException e) {
                    Log.d(Const.ERROR, "getContactRequests: " + e.getMessage());
                    feedBackToUi(false, R.string.toast_error_has_occurred,
                            false, false, false, R.string.message_error_has_occurred);
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
                    child(handledItem.getType()).
                    child(handledItem.getId()).
                    child(Const.CONTACT).
                    child(currentUserInfo.getId()).
                    setValue(true).addOnSuccessListener(aVoid ->
                    deleteIncomingContactRequestData(handledItem, R.string.toast_contact_confirmed))
                    .addOnFailureListener(e -> {
                        feedBackToUi(false, R.string.toast_error_has_occurred,
                                false, false, false, null);
                        Log.d(Const.ERROR, "handleIncomingContactRequest: " + e.getMessage());
                    });
        } else {
            deleteIncomingContactRequestData(handledItem, R.string.toast_contact_denied);
        }
    }

    private void deleteIncomingContactRequestData(ContactRequestItem handledItem, int toastRes) {
        mDatabaseRef.child(Const.USERS)
                .child(Const.SPEC)
                .child(mCurrentUserId)
                .child(Const.INREQUEST)
                .child(handledItem.getId()).removeValue().addOnSuccessListener(aVoid -> {
                    mContactRequestsMap.remove(handledItem.getId());
                    mContactRequestsMapLiveData.setValue(mContactRequestsMap);
                    feedBackToUi(false, toastRes, false, false, false, null);
                }
        );
    }

    public void sendContactRequest(String specialistId) {
        mShowProgress.setValue(true);
        try {
            mDatabaseRef.child(Const.USERS)
                    .child(Const.SPEC)
                    .child(specialistId)
                    .child(Const.INREQUEST)
                    .child(mCurrentUserId)
                    .setValue(mCurrentUserType).addOnSuccessListener(aVoid -> {
                mOutgoingContactRequests.put(specialistId, true);
                /* TODO история исходящих запросов сохраняется только в течении текущего сеанса.
                    После перезахода в приложение она сбрасывается. Сделать историю отправленых запросов
                    не зависящей от перезаходы в приложение*/
                mOutgoingContactRequestsLiveData.setValue(mOutgoingContactRequests);
                mShowProgress.setValue(false);
            });
        } catch (NullPointerException e) {
            Log.d(Const.ERROR, "sendContactRequest: " + e.getMessage());
            mShowProgress.setValue(false);
        }
    }

    public LiveData<BrowsingImageItem> getEditableGalleryItem() {
        return mEditableGalleryItemLiveData;
    }

    public void setEditableGalleryItem(BrowsingImageItem editableItem) {
        mEditableGalleryItemLiveData.setValue(editableItem);
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
        mLocalImageItemsMap = new HashMap<>();
        mSpecialistGalleryImageItemsMapLiveData.setValue(mLocalImageItemsMap);
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

    public LiveData<Integer> getMessageResId() {
        return mMessageResId;
    }

    @SuppressWarnings("SameParameterValue")
    private void feedBackToUi(@Nullable Boolean showProgress,
                              @Nullable Integer toastResId,
                              @NonNull Boolean hideKeyboard,
                              @NonNull Boolean popBackStack,
                              @Nullable Boolean processing,
                              @Nullable Integer messageResId) {

        if (showProgress != null) {
            mShowProgress.setValue(showProgress);
        }
        if (toastResId != null) {
            mShowToast.setValue(toastResId);
            mShowToast.setValue(null);
        }
        if (hideKeyboard) {
            mHideKeyboard.setValue(true);
            mHideKeyboard.setValue(null);
        }
        if (popBackStack) {
            mPopBackStack.setValue(true);
            mPopBackStack.setValue(null);
        }
        if (processing != null) {
            mProcessing.setValue(false);
        }
        if (messageResId != null) {
            mMessageResId.setValue(messageResId);
            mMessageResId.setValue(null);
        }
    }
}