package com.beproffer.beproffer.data.browsing_history;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.NonNull;

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

    private static final RoomDatabase.Callback roomCallback = new RoomDatabase.Callback(){
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            new PopulateDbAsyncTask(instance).execute();
            super.onCreate(db);
        }
    };

    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void> {
        private final BrowsingHistoryDao browsingHistoryDao;

        private PopulateDbAsyncTask(BrowsingHistoryDatabase browsingHistoryDatabase){
            browsingHistoryDao = browsingHistoryDatabase.browsingHistoryDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            browsingHistoryDao.insert(new BrowsingHistoryModel("firstdburl"));
            return null;
        }
    }
}