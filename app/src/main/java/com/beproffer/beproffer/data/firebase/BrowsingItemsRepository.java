package com.beproffer.beproffer.data.firebase;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.browsing_history.BrowsingHistoryModel;
import com.beproffer.beproffer.data.browsing_history.BrowsingHistoryRepository;
import com.beproffer.beproffer.data.models.BrowsingImageItem;
import com.beproffer.beproffer.data.models.BrowsingItemRef;
import com.beproffer.beproffer.util.Const;
import com.beproffer.beproffer.util.LocalizationConstants;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
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
    @SuppressWarnings("FieldCanBeLocal")
    private DatabaseReference mSearchRef;

    private BrowsingHistoryRepository mBrowsingHistoryRepository;
    private List<String> mActualBrowsingHistory;

    private final MutableLiveData<Boolean> mShowProgress = new MutableLiveData<>();
    private final MutableLiveData<Integer> mShowToast = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mShowSearchPanel = new MutableLiveData<>();
    private final MutableLiveData<Integer> mShowMessage = new MutableLiveData<>();

    private Map<String, String> mRequestParams;

    private final MutableLiveData<BrowsingImageItem> mTargetBrowsingItemLiveData = new MutableLiveData<>();

    private final List<BrowsingItemRef> mBrowsingItemRefList = new ArrayList<>();
    private final List<BrowsingItemRef> mBrowsingItemRefStorageList = new ArrayList<>();
    private final MutableLiveData<List<BrowsingItemRef>> mBrowsingItemRefListLiveData = new MutableLiveData<>();

    private static final String CLEAR_ALL = "clear";
    private static final String REMOVE_FIRST = "remove";
    private static final String ADD_NEW = "add";

    private Observer mBrowsingHistoryObserver;

    private DataSnapshot mCurrentDataSnapshot;

    public BrowsingItemsRepository(Application application) {
        mApplication = application;
        if (mRequestParams == null) {
            obtainRequestParams(mApplication);
        } else {
            obtainImageRefsFromDb();
        }
    }

    public LiveData<List<BrowsingItemRef>> getBrowsingItemRefs() {
        return mBrowsingItemRefListLiveData;
    }

    public LiveData<BrowsingImageItem> getTargetBrowsingItemLiveData() {
        return mTargetBrowsingItemLiveData;
    }

    /*пытаемся получить параметры поика для пользователя, чтобы сделать запрос в Realtime Database.*/
    private void obtainRequestParams(Application application) {
        mShowProgress.setValue(true);
        SharedPreferences searchRequestData;
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser != null) {
            searchRequestData = application.getSharedPreferences(mUser.getUid(), MODE_PRIVATE);
        } else {
            searchRequestData = application.getSharedPreferences(Const.UNKNOWN_USER_REQUEST, MODE_PRIVATE);
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
            mBrowsingItemRefList.clear();
            mBrowsingItemRefListLiveData.setValue(mBrowsingItemRefList);
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
        if (mBrowsingHistoryObserver == null)
            mBrowsingHistoryObserver = (Observer<List<String>>) browsingHistory -> {
                if (mActualBrowsingHistory == null) {
                    mActualBrowsingHistory = browsingHistory;
                    obtainImageRefsFromDb();
                } else {
                    mActualBrowsingHistory = browsingHistory;
                /*Эта проверка и начало новой фильтрации валидны только для авторизированных пользователей,
                 когда история просмотров фиксируется. Фильтрация начинается после ответа, о том, что url последнего
                 просмотренного изображения записано в историю просмотров. Стьарт новой фильтрации для режима гостя,
                 когда история просмотров не фиксируется, находится в deleteObservedItem()*/
                    if (mBrowsingItemRefList.isEmpty()) {
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
        if (mActualBrowsingHistory != null)
            mActualBrowsingHistory.clear();
        syncLiveData(null, CLEAR_ALL);
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

    private final ValueEventListener mValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            if (!dataSnapshot.exists()) {
                syncLiveData(null, CLEAR_ALL);
                feedBackToUi(false, null, true, R.string.toast_no_any_service_items);
                return;
            }

            if (dataSnapshot.getChildrenCount() < 1) {
                syncLiveData(null, CLEAR_ALL);
                feedBackToUi(false, null, true, R.string.toast_no_available_images);
                return;
            }

            mCurrentDataSnapshot = dataSnapshot;

            filterBrowsingItemRefs();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            syncLiveData(null, CLEAR_ALL);
            feedBackToUi(false, null, false, R.string.toast_error_has_occurred);
        }
    };

    /*разница между отправленными и принятыми запросами в storage для загрузки изображений*/
    private int mLoadings = 0;

    private void filterBrowsingItemRefs() {
        Log.d(Const.ERROR, "filterBrowsingItemRefs");
        if (mCurrentDataSnapshot == null) {
            Log.d(Const.ERROR, "filterBrowsingItemRefs mCurrentDataSnapshot == null");
            obtainImageRefsFromDb();
            return;
        }

        if (!mBrowsingItemRefStorageList.isEmpty()) {
            Log.d(Const.ERROR, "filterBrowsingItemRefs !mBrowsingItemRefStorageList.isEmpty()");
            for (int i = 0; i < Const.BROWSING_REFS_LIST_SIZE; i++) {
                if (!mBrowsingItemRefStorageList.isEmpty()) {
                    syncLiveData(mBrowsingItemRefStorageList.get(0), ADD_NEW);
                    mBrowsingItemRefStorageList.remove(0);
                    Log.d(Const.ERROR, "filterBrowsingItemRefs mBrowsingItemRefStorageList.size is: " + mBrowsingItemRefStorageList.size());
                } else {
                    Log.d(Const.ERROR, "filterBrowsingItemRefs mBrowsingItemRefStorageList.isEmpty()");
                    return;
                }
            }
            return;
        }

        /*Чтобы понять, когда все возможные ссылки обработаны*/
        long totalBrowsingImageRefsNum = mCurrentDataSnapshot.getChildrenCount();
        long handledBrowsingImageRefsNum = 0;

        for (DataSnapshot snapshot : mCurrentDataSnapshot.getChildren()) {
            BrowsingItemRef itemRef = snapshot.getValue(BrowsingItemRef.class);
            Log.d(Const.ERROR, "filterBrowsingItemRefs DataSnapshot filtration");
            if (itemRef == null)
                return;

            if (mUser != null && !mActualBrowsingHistory.contains(itemRef.getUrl())) {
                handleBrowsingItemRef(itemRef);
            } else if (mUser == null) {
                handleBrowsingItemRef(itemRef);
            }
            /*список mBrowsingItemRefsStorageList уже достаточно наполнен, но mCurrentDataSnapshot проитерован не полностью*/
            if (mBrowsingItemRefStorageList.size() > Const.BROWSING_REFS_LIST_MAX_SIZE) {
                mShowProgress.setValue(false);
                return;
            }
            /*весь mCurrentDataSnapshot проитерован, показать юзеру нечего - по этому запросу*/
            handledBrowsingImageRefsNum++;
            if (handledBrowsingImageRefsNum == totalBrowsingImageRefsNum) {
                if (mBrowsingItemRefList.isEmpty() && mLoadings == 0) {
                    feedBackToUi(false, null, true, R.string.toast_no_available_images);
                } else {
                    mShowProgress.setValue(false);
                }
            }
        }
    }

    private void handleBrowsingItemRef(@NonNull BrowsingItemRef itemRef) {
        if (mBrowsingItemRefList.size() > Const.BROWSING_REFS_LIST_SIZE) {
            mBrowsingItemRefStorageList.add(itemRef);
            Log.d(Const.ERROR, "mBrowsingItemRefStorageList size is: " + mBrowsingItemRefStorageList.size());
            return;
        }
        syncLiveData(itemRef, ADD_NEW);
    }

    private void syncLiveData(@Nullable BrowsingItemRef browsingItemRef, String action) {
        switch (action) {
            case REMOVE_FIRST:
                mBrowsingItemRefList.remove(0);
                mBrowsingItemRefListLiveData.setValue(mBrowsingItemRefList);
                break;
            case ADD_NEW:
                if (browsingItemRef != null)
                    preloadImageFromDb(browsingItemRef);
                break;
            case CLEAR_ALL:
                mBrowsingItemRefList.clear();
                mBrowsingItemRefStorageList.clear();
                Glide.get(mApplication.getApplicationContext()).clearMemory();
                mBrowsingItemRefListLiveData.setValue(mBrowsingItemRefList);
        }
    }

    private void preloadImageFromDb(BrowsingItemRef browsingItemRef) {
        /*добавляем в mBrowsingItemRefList объекты browsingItemRef только в том случае, если произошел
         * preload изображения в кеш. при загрузке сразу в imageView часто изображение не успевает
         * загрузиться а пользователь уже свайпает. это дает сбои.*/
        mLoadings++;
        Glide.with(mApplication.getApplicationContext())
                .load(browsingItemRef.getUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        mBrowsingItemRefList.add(browsingItemRef);
                        mBrowsingItemRefListLiveData.setValue(mBrowsingItemRefList);
                        Log.d(Const.ERROR, "mBrowsingItemRefList size is: " + mBrowsingItemRefList.size());
                        mLoadings--;
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        mLoadings--;
                    }
                });
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
        syncLiveData(null, REMOVE_FIRST);
        /*Эта проверка и начало новой фильтрации валидны только для пользователей в режиме гостя,
        когда история просмотров не фиксируется. Старт новой фильтрации для авторизированных пользователей,
         которые имеют историю просмотров находится в obtainBrowsingHistory()*/
        if (mBrowsingItemRefList.isEmpty() && mUser == null) {
            /*Если показ изображений пошел повторно, то пользователю должно показаться сообщение, о
            том, что изображения могут повторяться, если же, mBrowsingItemRefStorageList еще содержит
             не показанные гостю - то сообщение не должно показаться*/
            if (mBrowsingItemRefStorageList.isEmpty()) {
                feedBackToUi(false, R.string.toast_images_for_guest_mode, false, null);
                refreshAdapter();
            } else {
                feedBackToUi(false, null, false, null);
                filterBrowsingItemRefs();
            }
        }
    }

    public void refreshAdapter() {
        mActualBrowsingHistory = null;
        syncLiveData(null, CLEAR_ALL);
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

    public void resetValues(@NonNull Boolean resetShowSearchPanelValue,
                            @NonNull Boolean resetViewMessageValue) {
        if (resetShowSearchPanelValue)
            mShowSearchPanel.setValue(false);
        if (resetViewMessageValue)
            mShowMessage.setValue(null);
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
        }
        if (showViewMessage != null) {
            mShowMessage.setValue(showViewMessage);
        }
    }
}