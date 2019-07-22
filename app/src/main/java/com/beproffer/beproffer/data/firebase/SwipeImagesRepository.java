package com.beproffer.beproffer.data.firebase;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.browsing_history.BrowsingHistoryModel;
import com.beproffer.beproffer.data.browsing_history.BrowsingHistoryRepository;
import com.beproffer.beproffer.data.models.SwipeImageItem;
import com.beproffer.beproffer.util.Const;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class SwipeImagesRepository {

    private Application mApplication;

    private FirebaseUser mUser;

    private DataSnapshot mActualDataSnapshot;

    private BrowsingHistoryRepository mBrowsingHistoryRepository;
    private List<String> mActualBrowsingHistory;

    private MutableLiveData<Boolean> mShowProgress = new MutableLiveData<>();
    private MutableLiveData<Integer> mShowToast = new MutableLiveData<>();
    private MutableLiveData<Boolean> mPerformSearch = new MutableLiveData<>();

    private Map<String, String> mRequestParams;

    private List<SwipeImageItem> mSwipeImageItemsList = new ArrayList<>();
    private MutableLiveData<List<SwipeImageItem>> mSwipeImageItemsLiveData = new MutableLiveData<>();

    public SwipeImagesRepository(Application application) {
        mApplication = application;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            mUser = FirebaseAuth.getInstance().getCurrentUser();
        }
        if (mRequestParams == null) {
            obtainRequestParams(mApplication);
        } else {
            obtainImagesFromDb();
        }
    }

    public LiveData<List<SwipeImageItem>> getSwipeImageItemsListLiveData() {
       if (mSwipeImageItemsList == null )
            obtainRequestParams(mApplication);
        if(mSwipeImageItemsList != null && mSwipeImageItemsList.size() == 0)
            obtainImagesFromDb();
        return mSwipeImageItemsLiveData;
    }

    public void deleteObservedImageItem(SwipeImageItem item) {
        if (mUser != null)
            mBrowsingHistoryRepository.insert(new BrowsingHistoryModel(item.getUrl(), item.getType()));
        mSwipeImageItemsList.remove(0);
        mSwipeImageItemsLiveData.setValue(mSwipeImageItemsList);
    }

    /*пытаемся получить параметры для пользователя или гостя. согласно этим параметрам, будет сделан запрос в Firebase.*/
    private void obtainRequestParams(Application application) {
        mShowProgress.setValue(true);
        mSwipeImageItemsList = new ArrayList<>();
        SharedPreferences searchRequestData;
        if (mUser != null) {
            searchRequestData = application.getSharedPreferences(mUser.getUid(), MODE_PRIVATE);
        } else {
            searchRequestData = application.getSharedPreferences(Const.UNKNOWN_USER_REQUEST, MODE_PRIVATE);
        }
        if (searchRequestData == null) {
            feedBackToUi(false, R.string.toast_define_search_request, true);
            return;
        }
        try {
            mRequestParams = new HashMap<>();
            mRequestParams.put(Const.SERVSBTP, searchRequestData.getString(Const.SERVSBTP, null));
            mRequestParams.put(Const.SERVTYPE, searchRequestData.getString(Const.SERVTYPE, null));
            mRequestParams.put(Const.GENDER, searchRequestData.getString(Const.GENDER, null));
        } catch (NullPointerException e) {
            feedBackToUi(false, R.string.toast_error_search_request, true);
            return;
        }
        /*if search request params are not correct or equals null - we go to the search fragment to define params*/
        if (!checkRequestParams()) {
            feedBackToUi(false, R.string.toast_define_search_request, true);
            return;
        }
        if (mUser != null && mActualBrowsingHistory == null) {
            obtainBrowsingHistory();
            return;
        }
        obtainImagesFromDb();
    }

    private void obtainBrowsingHistory() {
        if (mBrowsingHistoryRepository == null)
            mBrowsingHistoryRepository = new BrowsingHistoryRepository(mApplication, mRequestParams.get(Const.SERVTYPE));
        /*я не уверен с этим обзервером. ситуация такова, что obtainImagesFromDb() должен запускатьсятогда,
         * как у нас уже есть mActualBrowsingHistory, как это зарешать, так чтобы было правильно, я незнаю.
         * на угад, сделал этот обзервер. но не уверен что правильно.*/
        if (!mBrowsingHistoryRepository.getTargetBrowsingHistory().hasObservers())
            mBrowsingHistoryRepository.getTargetBrowsingHistory().observeForever(browsingHistory -> {
                if (mActualBrowsingHistory == null) {
                    mActualBrowsingHistory = browsingHistory;
                    obtainImagesFromDb();
                } else {
                    mActualBrowsingHistory = browsingHistory;
                    /*проверяем на то, пустой ли список. если да - то дальше фильтруем -
                     * наполняем список новыми изображениями(ведь количество объектор в списке ограничено).
                     * Так, чтобы со снимка фб в mSwipeImageItemsList не добавлялись объекты, которые там уже есть
                     * здесь и нужна эта проверка. пыталя сделать проверку нв equals(в том числе и пере
                     * определял equals & hashcode), непосредственно
                     * перед помещением в список, но чето нихуя не получилось. так что, пока что так оставлю. */
                    if (mSwipeImageItemsList.isEmpty()) {
                        filterOutItemsForAdapter();
                    }
                }
            });
    }

    public void clearBrowsingHistory() {
        mBrowsingHistoryRepository.deleteWholeBrowsingHistory();
        mActualBrowsingHistory = null;
        feedBackToUi(false, R.string.toast_browsing_history_cleared, null);
    }

    private boolean checkRequestParams() {
        return mRequestParams != null && !mRequestParams.containsValue(null) && mRequestParams.size() == Const.SEARCH_PARAMS_NUM;
    }

    public void obtainImagesFromDb() {
        mShowProgress.setValue(true);
        if (!checkRequestParams()) {
            obtainRequestParams(mApplication);
            return;
        }
        try {
            FirebaseDatabase.getInstance().getReference()
                    .child(Const.SERVICES)
                    .child(mRequestParams.get(Const.SERVTYPE))
                    .child(mRequestParams.get(Const.SERVSBTP))
                    .addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            if (!dataSnapshot.exists()) {
                                feedBackToUi(false, R.string.toast_error_has_occurred, null);
                                return;
                            }
                            if (dataSnapshot.getChildrenCount() <1) {
                                feedBackToUi(false, R.string.toast_no_available_images, true);
                                return;
                            }
                            mActualDataSnapshot = dataSnapshot;

                            filterOutItemsForAdapter();
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            feedBackToUi(false, R.string.toast_error_has_occurred, null);
                        }
                    });
        } catch (NullPointerException exception) {
            feedBackToUi(false, R.string.toast_error_has_occurred, null);
        }
    }

    private void filterOutItemsForAdapter() {
        if (mActualDataSnapshot == null) {
            obtainImagesFromDb();
            return;
        }
        mShowProgress.setValue(true);
        for (DataSnapshot snapshotItem : mActualDataSnapshot.getChildren()) {
            if (mUser != null) {
                filterOutItemsForSignedUp(snapshotItem);
            } else {
                filterOutItemsForGuest(snapshotItem);
            }
            if (mSwipeImageItemsList.size() > Const.MAX_NUM_OF_IMAGES_IN_ADAPTER) {
                mShowProgress.setValue(false);
                return;
            }
        }
        if (!mSwipeImageItemsList.isEmpty()) {
            mShowProgress.setValue(false);
            return;
        }
        feedBackToUi(false, R.string.toast_no_available_images, true);
    }

    private void filterOutItemsForSignedUp(DataSnapshot snapshotItem) {
        if (mRequestParams.get(Const.GENDER).equals(Const.ALLGEND) && !mActualBrowsingHistory.contains(snapshotItem.child(Const.SERIMGURL).getValue().toString())) {
            addImageAdapterList(snapshotItem);
            return;
        }
        if (snapshotItem.child(Const.GENDER).getValue().toString().equals(mRequestParams.get(Const.GENDER)) &&
                !mActualBrowsingHistory.contains(snapshotItem.child(Const.SERIMGURL).getValue().toString())) {
            addImageAdapterList(snapshotItem);
        }
    }

    private void filterOutItemsForGuest(DataSnapshot snapshotItem) {
        if (mRequestParams.get(Const.GENDER).equals(Const.ALLGEND)) {
            addImageAdapterList(snapshotItem);
            return;
        }
        if (snapshotItem.child(Const.GENDER).getValue().toString().equals(mRequestParams.get(Const.GENDER))) {
            addImageAdapterList(snapshotItem);
        }
    }

    private void addImageAdapterList(DataSnapshot snapshotItem) {
        SwipeImageItem imageItem = snapshotItem.getValue(SwipeImageItem.class);
        if (mSwipeImageItemsList.contains(imageItem))
            return;
        mSwipeImageItemsList.add(imageItem);
        mSwipeImageItemsLiveData.setValue(mSwipeImageItemsList);
    }

    public void refreshItems(){
        obtainRequestParams(mApplication);
    }

    public LiveData<Boolean> getShowProgress() {
        return mShowProgress;
    }

    public LiveData<Integer> getShowToast() {
        return mShowToast;
    }

    public LiveData<Boolean> getPerformSearch() {
        return mPerformSearch;
    }

    public void resetTriggers(@Nullable Boolean resetToastValue, @Nullable Boolean resetPerformSearch) {
        if (resetToastValue != null && resetToastValue) {
            mShowToast.setValue(null);
        }
        if (resetPerformSearch != null && resetPerformSearch) {
            mPerformSearch.setValue(null);
        }
    }

    private void feedBackToUi(@Nullable Boolean showProgress,
                              @Nullable Integer toastResId,
                              @Nullable Boolean performSearch) {

        if (showProgress != null) {
            mShowProgress.setValue(showProgress);
        }
        if (toastResId != null) {
            mShowToast.setValue(toastResId);
        }
        if (performSearch != null) {
            mPerformSearch.setValue(performSearch);
        }
    }
}
