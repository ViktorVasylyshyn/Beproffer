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
import com.beproffer.beproffer.data.models.BrowsingImageItem;
import com.beproffer.beproffer.data.models.BrowsingItemRef;
import com.beproffer.beproffer.util.Const;
import com.beproffer.beproffer.util.LocalizationConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Репозиторий отвечает за получение, фильтрацию и поставку BrowsingItemRef из ветки сервисов.
 * Если же пользователю будет интересна подробная инфорамация - подгрузится BrowsingItem из ветки
 * специалиста.
 */

public class BrowsingItemsRepository {

    private final Application mApplication;

    private FirebaseUser mUser;

    private DatabaseReference mSearchRef;

    private BrowsingHistoryRepository mBrowsingHistoryRepository;
    private List<String> mActualBrowsingHistory;

    private final MutableLiveData<Boolean> mShowProgress = new MutableLiveData<>();
    private final MutableLiveData<Integer> mShowToast = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mShowSearchPanel = new MutableLiveData<>();
    private final MutableLiveData<Integer> mShowMessage = new MutableLiveData<>();

    private Map<String, String> mRequestParams;

    private MutableLiveData<BrowsingImageItem> mTargetBrowsingItemLiveData = new MutableLiveData<>();

    private List<BrowsingItemRef> mBrowsingItemsRefsList = new ArrayList<>();
    private final MutableLiveData<List<BrowsingItemRef>> mBrowsingItemsRefsLiveData = new MutableLiveData<>();

    public BrowsingItemsRepository(Application application) {
        mApplication = application;
        if (mRequestParams == null) {
            obtainRequestParams(mApplication);
        } else {
            obtainImageRefsFromDb();
        }
    }

    public LiveData<List<BrowsingItemRef>> getBrowsingItemRefListLiveData() {
        return mBrowsingItemsRefsLiveData;
    }

    public LiveData<BrowsingImageItem> getTargetBrowsingItemLiveData() {
        return mTargetBrowsingItemLiveData;
    }

    /*пытаемся получить параметры поика для пользователя, чтобы сделать запрос в Realtime Database.*/
    private void obtainRequestParams(Application application) {
        mShowProgress.setValue(true);
        mBrowsingItemsRefsList = new ArrayList<>();
        mBrowsingItemsRefsLiveData.setValue(mBrowsingItemsRefsList);
        SharedPreferences searchRequestData;
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser != null) {
            searchRequestData = application.getSharedPreferences(mUser.getUid(), MODE_PRIVATE);
        } else {
            searchRequestData = application.getSharedPreferences(Const.UNKNOWN_USER_REQUEST, MODE_PRIVATE);
        }
        if (searchRequestData == null) {
            feedBackToUi(false, null, true, R.string.toast_define_search_request);
            return;
        }
        try {
            mRequestParams = new HashMap<>();
            mRequestParams.put(Const.SERVSBTP, searchRequestData.getString(Const.SERVSBTP, null));
            mRequestParams.put(Const.SERVTYPE, searchRequestData.getString(Const.SERVTYPE, null));
            mRequestParams.put(Const.GENDER, searchRequestData.getString(Const.GENDER, null));
        } catch (NullPointerException e) {
            feedBackToUi(false, null, true, R.string.toast_error_search_request);
            return;
        }
        /*if search request params are not correct or equals null - we go to the search fragment to define params*/
        if (checkRequestParams()) {
            feedBackToUi(false, null, true, R.string.toast_define_search_request);
            return;
        }
        if (mUser != null && mActualBrowsingHistory == null) {
            obtainBrowsingHistory();
            return;
        }
        obtainImageRefsFromDb();
    }

    private void obtainBrowsingHistory() {
        Observer mBrowsingHistoryObserver = (Observer<List<String>>) browsingHistory -> {
            if (mActualBrowsingHistory == null) {
                mActualBrowsingHistory = browsingHistory;
                obtainImageRefsFromDb();
            } else {
                mActualBrowsingHistory = browsingHistory;
                /*Находится здесь, потому что последний просмотр должен успевать обновить
                 историю прежде чем начинать новую фильтрацию*/
                if (mBrowsingItemsRefsList.isEmpty()) {
                    filterBrowsingItemRefs();
                }
            }
        };

        try {
            if (mBrowsingHistoryRepository != null && mBrowsingHistoryRepository.getTargetBrowsingHistory().hasObservers()) {
                mBrowsingHistoryRepository.getTargetBrowsingHistory().removeObserver(mBrowsingHistoryObserver);
                mBrowsingHistoryRepository = null;
            }
            if (mBrowsingHistoryRepository == null) {
                mBrowsingHistoryRepository = new BrowsingHistoryRepository(mApplication);
                /*я не уверен с этим обзервером. ситуация такова, что obtainImageRefsFromDb() должен запускатьсятогда,
                 * как у нас уже есть mActualBrowsingHistory, как это зарешать, так чтобы было правильно, я незнаю.
                 * на угад, сделал этот обзервер. но не уверен что правильно.*/
                mBrowsingHistoryRepository.getTargetBrowsingHistory().observeForever(mBrowsingHistoryObserver);
            }
        } catch (NullPointerException e) {
            feedBackToUi(false, null, false, R.string.toast_error_has_occurred);
        }
    }

    public void clearBrowsingHistory() {
        if (mBrowsingHistoryRepository == null)
            mBrowsingHistoryRepository = new BrowsingHistoryRepository(mApplication);
        mBrowsingHistoryRepository.deleteWholeBrowsingHistory();
        mActualBrowsingHistory.clear();
        filterBrowsingItemRefs();
        feedBackToUi(false, R.string.toast_browsing_history_cleared, false, null);
    }

    private boolean checkRequestParams() {
        return mRequestParams == null || mRequestParams.containsValue(null) || mRequestParams.size() != Const.SEARCH_PARAMS_NUM;
    }

    private void obtainImageRefsFromDb() {
        /*выполнение этого метода должно убирать с фрагмента текст об отсутсвии результатов поиска*/
        feedBackToUi(true, null, false, R.string.string_res_without_text);
        mShowProgress.setValue(true);
        if (checkRequestParams()) {
            obtainRequestParams(mApplication);
            return;
        }
        try {
            /*TODO после введения локализации - формировать mSearchRef с учетом локации пользователя.*/
            mSearchRef = FirebaseDatabase.getInstance().getReference()
                    .child(Const.SERVICES)
                    .child(LocalizationConstants.UKRAINE)
                    .child(LocalizationConstants.KYIV_REGION)
                    .child(LocalizationConstants.KYIV)
                    .child(mRequestParams.get(Const.SERVTYPE))
                    .child(mRequestParams.get(Const.SERVSBTP));
            if (mRequestParams.get(Const.GENDER).equals(Const.BOTHGEND)) {
                mSearchRef.addListenerForSingleValueEvent(mValueEventListener);
            } else {
                mSearchRef.orderByChild(Const.GENDER).equalTo(mRequestParams.get(Const.GENDER))
                        .addListenerForSingleValueEvent(mValueEventListener);
            }
        } catch (NullPointerException exception) {
            feedBackToUi(false, null, false, R.string.toast_error_has_occurred);
        }
    }

    private ValueEventListener mValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            if (!dataSnapshot.exists()) {
                feedBackToUi(false, null, true, R.string.toast_no_any_service_items);
                return;
            }

            if (dataSnapshot.getChildrenCount() < 1) {
                feedBackToUi(false, null, true, R.string.toast_no_available_images);
                return;
            }

            mCurrentDataSnapshot = dataSnapshot;

            filterBrowsingItemRefs();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            feedBackToUi(false, null, false, R.string.toast_error_has_occurred);
        }
    };

    private DataSnapshot mCurrentDataSnapshot;

    private void filterBrowsingItemRefs() {

        if (mCurrentDataSnapshot == null) {
            obtainImageRefsFromDb();
            return;
        }

        /*Чтобы понять, когда все возможные ссылки обработаны*/
        long totalBrowsingImageRefsNum = mCurrentDataSnapshot.getChildrenCount();
        long handledBrowsingImageRefsNum = 0;

        for (DataSnapshot snapshot : mCurrentDataSnapshot.getChildren()) {
            BrowsingItemRef itemRef = snapshot.getValue(BrowsingItemRef.class);

            if (mUser != null && !mActualBrowsingHistory.contains(itemRef.getUrl())) {
                handleBrowsingItemRef(itemRef);
            } else if (mUser == null) {
                handleBrowsingItemRef(itemRef);
            }
            /*список mBrowsingItemRefsStorageList уже достаточно наполнен, но mCurrentDataSnapshot проитерован не полностью*/
            if (mBrowsingItemsRefsList.size() > Const.BROWSING_REFS_LIST_MAX_SIZE) {
                mShowProgress.setValue(false);
                return;
            }

            /*весь mCurrentDataSnapshot проитерован, показать юзеру нечего - по этому запросу*/
            handledBrowsingImageRefsNum++;
            if (handledBrowsingImageRefsNum == totalBrowsingImageRefsNum) {
                if (mBrowsingItemsRefsList.isEmpty()) {
                    feedBackToUi(false, null, true, R.string.toast_no_available_images);
                } else {
                    mShowProgress.setValue(false);
                }
            }
        }
    }

    private void handleBrowsingItemRef(BrowsingItemRef itemRef) {
        if (itemRef != null) {
            mBrowsingItemsRefsList.add(itemRef);
            mBrowsingItemsRefsLiveData.setValue(mBrowsingItemsRefsList);
        }
    }

    public void obtainBrowsingItemDetailInfo(BrowsingItemRef itemRef) {
        /*повторные входы на фрагмент инфо одного и того же объекта не должны наново его подгружать*/
        if (mTargetBrowsingItemLiveData.getValue() != null && mTargetBrowsingItemLiveData.getValue().getKey().equals(itemRef.getKey())) {
            return;
        }
        mShowProgress.setValue(true);
        mTargetBrowsingItemLiveData.setValue(null);
        FirebaseDatabase.getInstance().getReference()
                .child(Const.USERS)
                .child(Const.SPEC)
                .child(itemRef.getId())
                .child(Const.SERVICES)
                .child(itemRef.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            mTargetBrowsingItemLiveData.setValue(dataSnapshot.getValue(BrowsingImageItem.class));
                            mShowProgress.setValue(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        mShowProgress.setValue(false);
                        Log.d(Const.INFO, "obtainBrowsingItemDetailInfo onCancelled with db error: " + databaseError.getMessage());
                        /* TODO возможно отслеживать и фиксить случаи при которых, не вышло
                            получить объект, который по факту должен был быть.*/
                    }
                });
    }

    public void deleteObservedItem(BrowsingItemRef item) {
        if (mUser != null)
            mBrowsingHistoryRepository.insert(new BrowsingHistoryModel(item.getUrl()));
        mBrowsingItemsRefsList.remove(0);
        mBrowsingItemsRefsLiveData.setValue(mBrowsingItemsRefsList);
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

    public LiveData<Boolean> getShowSearchPanel() {
        return mShowSearchPanel;
    }

    public LiveData<Integer> getShowMessage() {
        return mShowMessage;
    }

    private void feedBackToUi(@Nullable Boolean showProgress,
                              @Nullable Integer toastResId,
                              @NonNull Boolean showSearchPanel,
                              @Nullable Integer showViewMessage) {

        if (showProgress != null) {
            mShowProgress.setValue(showProgress);
        }
        if (toastResId != null) {
            mShowToast.setValue(toastResId);
            mShowToast.setValue(null);
        }
        if (showSearchPanel) {
            mShowSearchPanel.setValue(true);
            mShowSearchPanel.setValue(null);
        }
        if (showViewMessage != null) {
            mShowMessage.setValue(showViewMessage);
            mShowMessage.setValue(null);
        }
    }
}