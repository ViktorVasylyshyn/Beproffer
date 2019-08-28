package com.beproffer.beproffer.data.browsing_history;

import android.app.Application;
import androidx.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class BrowsingHistoryRepository {
    private final BrowsingHistoryDao browsingHistoryDao;
    private LiveData<List<String>> targetBrowsingHistory;

    public BrowsingHistoryRepository(Application application){
        BrowsingHistoryDatabase database = BrowsingHistoryDatabase.getInstance(application);
        browsingHistoryDao = database.browsingHistoryDao();
                targetBrowsingHistory = browsingHistoryDao.getBrowsingHistory();

    }

    public void insert(BrowsingHistoryModel browsingHistoryModel){
        new InsertBrowsingHistoryAsyncTask(browsingHistoryDao).execute(browsingHistoryModel);
    }

    public void deleteWholeBrowsingHistory(){
        new DeleteWholeBrowsingHistoryAsyncTask(browsingHistoryDao).execute();
    }

    public LiveData<List<String>> getTargetBrowsingHistory(){
        return targetBrowsingHistory;
    }

    private static class InsertBrowsingHistoryAsyncTask extends AsyncTask<BrowsingHistoryModel, Void, Void> {
        private final BrowsingHistoryDao browsingHistoryDao;

        private InsertBrowsingHistoryAsyncTask(BrowsingHistoryDao browsingHistoryDao){
            this.browsingHistoryDao = browsingHistoryDao;
        }

        @Override
        protected Void doInBackground(BrowsingHistoryModel... models){
            browsingHistoryDao.insert(models[0]);
            return null;
        }
    }

    private static class DeleteWholeBrowsingHistoryAsyncTask extends AsyncTask<Void, Void, Void>{
        private final BrowsingHistoryDao browsingHistoryDao;

        private DeleteWholeBrowsingHistoryAsyncTask(BrowsingHistoryDao browsingHistoryDao){
            this.browsingHistoryDao = browsingHistoryDao;
        }

        @Override
        protected Void doInBackground(Void... voids){
            browsingHistoryDao.deleteWholeBrowsingHistory();
            return null;
        }
    }

}
