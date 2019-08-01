package com.beproffer.beproffer.data.firebase;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class SwipeImagesRepository {

    private Application mApplication;

    private FirebaseUser mUser;

    private BrowsingHistoryRepository mBrowsingHistoryRepository;
    private List<String> mActualBrowsingHistory;

    private MutableLiveData<Boolean> mShowProgress = new MutableLiveData<>();
    private MutableLiveData<Integer> mShowToast = new MutableLiveData<>();
    private MutableLiveData<Boolean> mPerformSearch = new MutableLiveData<>();

    private Map<String, String> mRequestParams;

    private DataSnapshot mActualDataSnapshot;

    private List<SwipeImageItem> mSwipeImageItemsList = new ArrayList<>();
    private MutableLiveData<List<SwipeImageItem>> mSwipeImageItemsLiveData = new MutableLiveData<>();

    private Observer mBrowsingHistoryObserver;

    public SwipeImagesRepository(Application application) {
        mApplication = application;
        if (mRequestParams == null) {
            obtainRequestParams(mApplication);
        } else {
            obtainImagesFromDb();
        }
    }

    public LiveData<List<SwipeImageItem>> getSwipeImageItemsListLiveData() {
//        if (mSwipeImageItemsList != null && mSwipeImageItemsList.size() == 0)
//            obtainImagesFromDb();
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
        Log.d(Const.INFO, "obtainRequestParams");
        mShowProgress.setValue(true);
        mSwipeImageItemsList = new ArrayList<>();
        mSwipeImageItemsLiveData.setValue(mSwipeImageItemsList);
        SharedPreferences searchRequestData;
        mUser = FirebaseAuth.getInstance().getCurrentUser();
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
            Log.d(Const.INFO, "obtainRequestParams b hist");
            obtainBrowsingHistory();
            return;
        }
        obtainImagesFromDb();
    }

    private void obtainBrowsingHistory() {
        Log.d(Const.INFO, "obtainBrowsingHistory");

        mBrowsingHistoryObserver = new Observer<List<String>>() {
            @Override
            public void onChanged(@Nullable List<String> browsingHistory) {
                Log.d(Const.INFO, "Browsing History changed");
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
            }
        };

        try {
            if (mBrowsingHistoryRepository != null && mBrowsingHistoryRepository.getTargetBrowsingHistory().hasObservers()) {
                Log.d(Const.INFO, "obtainBrowsingHistory has observers");
                mBrowsingHistoryRepository.getTargetBrowsingHistory().removeObserver(mBrowsingHistoryObserver);
                mBrowsingHistoryRepository = null;
            }
            if(mBrowsingHistoryRepository == null){
            mBrowsingHistoryRepository = new BrowsingHistoryRepository(mApplication, mRequestParams.get(Const.SERVTYPE));
            /*я не уверен с этим обзервером. ситуация такова, что obtainImagesFromDb() должен запускатьсятогда,
             * как у нас уже есть mActualBrowsingHistory, как это зарешать, так чтобы было правильно, я незнаю.
             * на угад, сделал этот обзервер. но не уверен что правильно.*/
            mBrowsingHistoryRepository.getTargetBrowsingHistory().observeForever(mBrowsingHistoryObserver);}
        } catch (NullPointerException e) {
            feedBackToUi(false, R.string.toast_error_has_occurred, null);
        }

    }

    public void clearBrowsingHistory() {
        if (mBrowsingHistoryRepository == null)
            mBrowsingHistoryRepository = new BrowsingHistoryRepository(mApplication, mRequestParams.get(Const.SERVTYPE));
        mBrowsingHistoryRepository.deleteWholeBrowsingHistory();
        mActualBrowsingHistory = null;
        obtainRequestParams(mApplication);
        feedBackToUi(false, R.string.toast_browsing_history_cleared, null);
    }

    private boolean checkRequestParams() {
        return mRequestParams != null && !mRequestParams.containsValue(null) && mRequestParams.size() == Const.SEARCH_PARAMS_NUM;
    }

    public void obtainImagesFromDb() {
        mShowProgress.setValue(true);
        if (!checkRequestParams()) {
            Log.d(Const.INFO, "!checkRequestParams()");
            obtainRequestParams(mApplication);
            return;
        }
        try {
            FirebaseDatabase.getInstance().getReference()
                    .child(Const.SERVICES)
                    .child(mRequestParams.get(Const.SERVTYPE))
                    .child(mRequestParams.get(Const.SERVSBTP))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (!dataSnapshot.exists()) {
                                feedBackToUi(false, R.string.toast_no_any_service_items, true);
                                return;
                            }

                            if (dataSnapshot.getChildrenCount() < 1) {
                                feedBackToUi(false, R.string.toast_no_available_images, true);
                                return;
                            }

                            mActualDataSnapshot = dataSnapshot;

                            filterOutItemsForAdapter();

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
        for (DataSnapshot userSnapshotItem : mActualDataSnapshot.getChildren()) {
            if (mSwipeImageItemsList.size() > Const.MAX_NUM_OF_IMAGES_IN_ADAPTER) {
                Log.d(Const.INFO, "list size is  > MAX_NUM_OF_IMAGES_IN_ADAPTER = " + mSwipeImageItemsList.size());
                mShowProgress.setValue(false);
                return;
            }
            for (DataSnapshot serviceSnapshotItem : userSnapshotItem.getChildren()) {
                if (mUser != null) {
                    Log.d(Const.INFO, "filterOutItemsForAdapter children 1 ");
                    filterOutItemsForSignedUp(serviceSnapshotItem.getValue(SwipeImageItem.class));
                } else {
                    Log.d(Const.INFO, "filterOutItemsForAdapter children 2");
                    filterOutItemsForGuest(serviceSnapshotItem.getValue(SwipeImageItem.class));
                }
            }
        }
        if (mSwipeImageItemsList.isEmpty()) {
            Log.d(Const.INFO, "filterOutItemsForAdapter no_available_images");
            feedBackToUi(false, R.string.toast_no_available_images, true);
        }
        mShowProgress.setValue(false);
    }

    private void filterOutItemsForSignedUp(SwipeImageItem imageItem) {
        if (mRequestParams.get(Const.GENDER).equals(Const.BOTHGEND) &&
                !mActualBrowsingHistory.contains(imageItem.getUrl())) {
            addImageAdapterList(imageItem);
        } else if (!mActualBrowsingHistory.contains(imageItem.getUrl())
                && (imageItem.getGender().equals(mRequestParams.get(Const.GENDER)) || imageItem.getGender().equals(Const.BOTHGEND))) {
            addImageAdapterList(imageItem);
        }
    }

    private void filterOutItemsForGuest(SwipeImageItem imageItem) {
        if (mRequestParams.get(Const.GENDER).equals(Const.BOTHGEND)) {
            addImageAdapterList(imageItem);
            return;
        }
        if (imageItem.getGender().equals(mRequestParams.get(Const.GENDER))) {
            addImageAdapterList(imageItem);
        }
    }

    private void addImageAdapterList(SwipeImageItem imageItem) {
        if (!mSwipeImageItemsList.contains(imageItem)) {
            Log.d(Const.INFO, "addImageAdapterList item added");
            mSwipeImageItemsList.add(imageItem);
            mSwipeImageItemsLiveData.setValue(mSwipeImageItemsList);
        }
    }

    public void refreshAdapter() {
        mActualBrowsingHistory = null;
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
            Log.d(Const.INFO, "feedBackToUi");
            mPerformSearch.setValue(performSearch);
        }
    }
}
