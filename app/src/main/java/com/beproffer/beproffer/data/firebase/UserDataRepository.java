package com.beproffer.beproffer.data.firebase;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.BrowsingImageItem;
import com.beproffer.beproffer.data.models.BrowsingItemRef;
import com.beproffer.beproffer.data.models.ContactItem;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private String mContactsObtained = "user";

    private final MutableLiveData<Boolean> mShowProgress = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mProcessing = new MutableLiveData<>();
    private final MutableLiveData<Integer> mShowToast = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mHideKeyboard = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mPopBackStack = new MutableLiveData<>();
    private final MutableLiveData<Integer> mMessageResId = new MutableLiveData<>();

    private final MutableLiveData<UserInfo> mUserInfoLiveData = new MutableLiveData<>();

    /*список личных сервисов специалиста*/
    private final Map<String, BrowsingImageItem> mServiceItemsMap = new HashMap<>();
    private final MutableLiveData<Map<String, BrowsingImageItem>> mServiceItemsMapLiveData = new MutableLiveData<>();

    private final MutableLiveData<BrowsingImageItem> mEditableGalleryItemLiveData = new MutableLiveData<>();

    /*список сервисов специалиста который находится в конактах пользователя*/
    private final List<BrowsingImageItem> mServiceItemsList = new ArrayList<>();
    private final MutableLiveData<List<BrowsingImageItem>> mServiceItemsListLiveData = new MutableLiveData<>();

    private final Map<String, ContactItem> mContactsMap = new HashMap<>();
    private final MutableLiveData<Map<String, ContactItem>> mContactsMapLiveData = new MutableLiveData<>();

    private final MutableLiveData<String> mSpecialistPhone = new MutableLiveData<>();
    private final MutableLiveData<String> mPopularity = new MutableLiveData<>();

    public UserDataRepository(Application application) {
        mApplication = application;
    }

    public LiveData<UserInfo> getUserInfoLiveData() {

        if (mUserInfoLiveData.getValue() == null && FirebaseAuth.getInstance().getCurrentUser() != null) {
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
                if (!dataSnapshot.hasChild(Const.INFO))
                    return;
                mUserDataSnapShot = dataSnapshot;
                mUserInfoLiveData.setValue(mUserDataSnapShot.child(Const.INFO).getValue(UserInfo.class));
                mShowProgress.setValue(false);
                if (mCurrentUserType.equals(Const.SPEC) && dataSnapshot.hasChild(Const.POPULARITY)) {
                    mPopularity.postValue(String.valueOf(dataSnapshot.child(Const.POPULARITY).getChildrenCount()));
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
            if (bitmap == null)
                return;
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
                            if (task.getException() != null)
                                Log.d(Const.ERROR, "saveUserInfoToRealTimeDb: " + task.getException().getMessage());
                        }
                    });
    }

    /*специалист получает данные о своих сервисах*/
    public LiveData<Map<String, BrowsingImageItem>> getServiceItemsList() {
        if (!mCurrentUserType.equals(Const.SPEC)) {
            mServiceItemsMapLiveData.setValue(mServiceItemsMap);
            return mServiceItemsMapLiveData;
        }

        if (mUserDataSnapShot.hasChild(Const.SERVICES) && mUserDataSnapShot.child(Const.SERVICES).hasChildren() && mServiceItemsMap.isEmpty()) {
            obtainServiceItems();
        }
        if (!mUserDataSnapShot.hasChild(Const.SERVICES) || !mUserDataSnapShot.child(Const.SERVICES).hasChildren()) {
            mServiceItemsMapLiveData.setValue(mServiceItemsMap);
        }
        return mServiceItemsMapLiveData;
    }

    /*пользователь получает данные о сервисах специалиста, которые находится в его контактах*/
    public LiveData<List<BrowsingImageItem>> getServiceItemsList(String specialistId) {
        /*если пользователь хочет, повторно, посмотреть данные специалиста, которые хранятся в mServiceItemsList*/
        if (!mServiceItemsList.isEmpty() && mServiceItemsList.get(0).getId().equals(specialistId))
            return mServiceItemsListLiveData;
        /*пользователь хочет просмотреть данные, которые он еще не видел*/
        mShowProgress.setValue(true);
        /*очистить список, если раньше просматривались данные другого специалиста*/
        if (!mServiceItemsList.isEmpty()) {
            mServiceItemsList.clear();
            mServiceItemsListLiveData.setValue(mServiceItemsList);
        }
        mDatabaseRef.child(Const.USERS)
                .child(Const.SPEC)
                .child(specialistId)
                .child(Const.SERVICES)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            return;
                        }
                        mShowProgress.postValue(false);
                        for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                            try {
                                mServiceItemsList.add(itemSnapshot.getValue(BrowsingImageItem.class));
                                mServiceItemsListLiveData.postValue(mServiceItemsList);
                            } catch (NullPointerException e) {
                                Log.d(Const.ERROR, "getServiceItemsList: " + e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(Const.ERROR, "getServiceItemsList.onCancelled: " + databaseError.getMessage());
                    }
                });
        return mServiceItemsListLiveData;
    }

    private void obtainServiceItems() {
        mShowProgress.setValue(true);
        for (DataSnapshot data : mUserDataSnapShot.child(Const.SERVICES).getChildren()) {
            try {
                mServiceItemsMap.put(data.getKey(), data.getValue(BrowsingImageItem.class));
            } catch (NullPointerException e) {
                /*TODO такого, правда еще не случалось, но если вдруг то - отображать объект как
                    испорченый, чтобы пользователь его перезаписал. или может вообще его удалять*/
                feedBackToUi(false, R.string.toast_error_has_occurred,
                        false, false, false, null);
                Log.d(Const.ERROR, "obtainServiceItems: " + e.getMessage());
            }
        }
        mServiceItemsMapLiveData.setValue(mServiceItemsMap);
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
                if (imgTask.getException() != null)
                    Log.d(Const.ERROR, "saveImageDataToRealtimeDb: " + imgTask.getException().getMessage());
            }
        });
    }

    /*На случай, если специалист изменял тип услуги. тогда ссылка на услугу в ветке "services, будет
    перезаписана в новую ветку, а старую нужно удалить."*/
    public void deleteNotRelevantImageData(BrowsingImageItem updatedItem, String primordialItemType, String primordialItemSubtype) {
        try {
            /*TODO после введения локализации - формировать этот запрос с учетом локации пользователя.*/
            /*если удаление неактуального объекта сервиса, по каким то причинам не удается - сохраняем
             * в раздел "not deleted" этот объект, чтобы удалить мануально*/
            mDatabaseRef.child(Const.SERVICES)
                    .child(LocalizationConstants.UKRAINE)
                    .child(LocalizationConstants.KYIV_REGION)
                    .child(LocalizationConstants.KYIV)
                    .child(primordialItemType)
                    .child(primordialItemSubtype)
                    .child(updatedItem.getKey())
                    .removeValue().addOnFailureListener(e -> mDatabaseRef.child(Const.SERVICES)
                    .child(LocalizationConstants.UKRAINE)
                    .child(LocalizationConstants.KYIV_REGION)
                    .child(LocalizationConstants.KYIV)
                    .child(Const.NOT_DELETED)
                    .child(updatedItem.getKey())
                    .setValue(updatedItem));
        } catch (NullPointerException e) {
            Log.d(Const.ERROR, "deleteNotRelevantImageData: " + e.getMessage());
        }
    }

    private void updateLocalImageItemsMap(BrowsingImageItem updatedItem) {
        /*4. Добавляем или заменяем данные в списке, который подается локально на RecyclerView,
        потому что нет отслеживания в реальном времени. объекты сервисов специалиста находятся в
        snapshot которые взялся разово. соответственно - заменили в базе - нужно заменить и локально.
        в будущем - переделать.*/
        mServiceItemsMap.put(updatedItem.getKey(), updatedItem);
        mServiceItemsMapLiveData.setValue(mServiceItemsMap);
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
                obtainContact(data.getKey());
            }
            mContactsObtained = mCurrentUserId;
        } else {
            mShowProgress.setValue(false);
            mContactsMapLiveData.setValue(mContactsMap);
        }
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
            editPopularity(deletedContact.getId(), false);
            feedBackToUi(false, R.string.toast_contact_deleted, false, false, false, null);
        });
    }

    private void obtainContact(String specialistId) {
        try {
            mDatabaseRef.child(Const.USERS).child(Const.SPEC).child(specialistId).child(Const.INFO)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            /*Если при создании объекта ContactItem будет краш у тех специалистов, которые не добавили
                             изображение, и у них в описании нет profileImageUrl ключа - то обязать их
                             загружать изображение профайла, перед тем как разрешать подтверждать запросы*/
                            if (dataSnapshot.exists()) {
                                mShowProgress.postValue(false);
                                mProcessing.postValue(false);
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

    public void addContact(String specialistId) {
        mShowProgress.setValue(true);
        mProcessing.setValue(true);
        mDatabaseRef.child(Const.USERS)
                .child(mCurrentUserType)
                .child(mCurrentUserId)
                .child(Const.CONTACT)
                .child(specialistId)
                .setValue(true)
                .addOnSuccessListener(aVoid -> {
                    obtainContact(specialistId);
                    editPopularity(specialistId, true);
                })
                .addOnFailureListener(e -> {
                    mShowProgress.setValue(false);
                    mProcessing.setValue(false);
                    mMessageResId.postValue(R.string.message_error_has_occurred);
                    Log.d(Const.ERROR, "addContact addOnFailureListener: " + e.getMessage());
                });
    }

    private String mLastSpecialistId;

    public LiveData<String> getSpecialistPhone(String specialistId) {
        if (!specialistId.equals(mLastSpecialistId)) {
            mLastSpecialistId = specialistId;
            mSpecialistPhone.setValue(null);
            obtainSpecialistPhone(specialistId);
        }
        return mSpecialistPhone;
    }

    private void obtainSpecialistPhone(String specialistId) {
        mShowProgress.setValue(true);
        mProcessing.setValue(true);
        mDatabaseRef.child(Const.USERS).child(Const.SPEC).child(specialistId).child(Const.INFO).child("phone").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mProcessing.postValue(false);
                mShowProgress.setValue(false);

                if (dataSnapshot.exists()) {
                    try {
                        mSpecialistPhone.postValue(dataSnapshot.getValue().toString());
                    } catch (NullPointerException e) {
                        Log.d(Const.ERROR, "obtainSpecialistPhone: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mProcessing.postValue(false);
                mShowProgress.setValue(false);
                Log.d(Const.ERROR, "obtainSpecialistPhone - onCancelled: " + databaseError.getMessage());
            }
        });
    }

    /*Популярность - значение валидно только для специалистов и равняется количеству пользователей,
    которые добавили специалиста в контакты. пока что, настройками базы данных, доступно только для
    авторизированных пользователей*/
    private void editPopularity(String specialistId, boolean increaseValue) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(Const.USERS)
                .child(Const.SPEC).child(specialistId).child(Const.POPULARITY);
        if (increaseValue) {
            ref.child(mCurrentUserId).setValue(true);
        } else {
            ref.child(mCurrentUserId).removeValue();
        }
    }

    public LiveData<BrowsingImageItem> getEditableGalleryItem() {
        return mEditableGalleryItemLiveData;
    }

    public void setEditableGalleryItem(BrowsingImageItem editableItem) {
        mEditableGalleryItemLiveData.setValue(editableItem);
    }

    public LiveData<String> getPopularity() {
        return mPopularity;
    }

    public void resetLocalUserData() {
        mShowProgress.setValue(true);
        mCurrentUserId = null;
        mCurrentUserType = null;
        mUserInfoLiveData.setValue(null);
        mUserDataSnapShot = null;
        mContactsMap.clear();
        mContactsMapLiveData.setValue(mContactsMap);
        mServiceItemsMap.clear();
        mServiceItemsMapLiveData.setValue(mServiceItemsMap);
        mShowProgress.setValue(false);
        mPopularity.setValue(null);
        mServiceItemsListLiveData.setValue(null);
        mServiceItemsList.clear();
        mEditableGalleryItemLiveData.setValue(null);
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

    public void resetValues(@NonNull Boolean resetToastResId,
                            @NonNull Boolean resetHideKeyboard,
                            @NonNull Boolean resetPopBackStack,
                            @NonNull Boolean resetMessageResId) {
        if (resetToastResId)
            mShowToast.setValue(null);
        if (resetHideKeyboard)
            mHideKeyboard.setValue(false);
        if (resetPopBackStack)
            mPopBackStack.setValue(false);
        if (resetMessageResId)
            mMessageResId.setValue(null);
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
        }
        if (hideKeyboard) {
            mHideKeyboard.setValue(true);
        }
        if (popBackStack) {
            mPopBackStack.setValue(true);
        }
        if (processing != null) {
            mProcessing.setValue(false);
        }
        if (messageResId != null) {
            mMessageResId.setValue(messageResId);
        }
    }
}