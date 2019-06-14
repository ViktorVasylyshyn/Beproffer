package com.beproffer.beproffer.data.browsing_history;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

@Database(entities = {BrowsingHistoryModel.class}, version = 1)
public abstract class BrowsingHistoryDatabase extends RoomDatabase {

    private static BrowsingHistoryDatabase instance;

    public abstract BrowsingHistoryDao browsingHistoryDao();

    public static synchronized BrowsingHistoryDatabase getInstance(Context context){

        if(instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    BrowsingHistoryDatabase.class, "browsing_history")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build();
        }
        return instance;
    }

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback(){
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            new PopulateDbAsyncTask(instance).execute();
            super.onCreate(db);
        }
    };

    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void> {
        private BrowsingHistoryDao browsingHistoryDao;

        private PopulateDbAsyncTask(BrowsingHistoryDatabase browsingHistoryDatabase){
            browsingHistoryDao = browsingHistoryDatabase.browsingHistoryDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            browsingHistoryDao.insert(new BrowsingHistoryModel("firstdbhairurl", "HAI"));
            browsingHistoryDao.insert(new BrowsingHistoryModel("firstdbnailsurl", "NAI"));
            browsingHistoryDao.insert(new BrowsingHistoryModel("firstdbmakeupurl", "MAK"));
            browsingHistoryDao.insert(new BrowsingHistoryModel("firstdbtattoourl", "TAT"));
            browsingHistoryDao.insert(new BrowsingHistoryModel("firstdbbarbershopurl", "BAR"));
            browsingHistoryDao.insert(new BrowsingHistoryModel("firstdbfitnessurl", "FIT"));
            return null;
        }
    }
}