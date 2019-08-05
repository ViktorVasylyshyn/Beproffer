package com.beproffer.beproffer.data.browsing_history;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.beproffer.beproffer.util.Const;

import java.util.List;

public class BrowsingHistoryRepository {
    private final BrowsingHistoryDao browsingHistoryDao;
    private LiveData<List<String>> targetBrowsingHistory;

    public BrowsingHistoryRepository(Application application, String targetType){
        BrowsingHistoryDatabase database = BrowsingHistoryDatabase.getInstance(application);
        browsingHistoryDao = database.browsingHistoryDao();
        switch (targetType){
            case Const.HAI:
                targetBrowsingHistory = browsingHistoryDao.getBrowsingHistoryHair();
                break;
            case Const.NAI:
                targetBrowsingHistory = browsingHistoryDao.getBrowsingHistoryNails();
                break;
            case Const.MAK:
                targetBrowsingHistory = browsingHistoryDao.getBrowsingHistoryMakeup();
                break;
            case Const.TAT:
                targetBrowsingHistory = browsingHistoryDao.getBrowsingHistoryTattoo();
                break;
            case Const.BAR:
                targetBrowsingHistory = browsingHistoryDao.getBrowsingHistoryBarber();
                break;
            case Const.FIT:
                targetBrowsingHistory = browsingHistoryDao.getBrowsingHistoryFitness();
                break;
        }
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
