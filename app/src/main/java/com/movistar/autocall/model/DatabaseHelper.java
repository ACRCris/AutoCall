package com.movistar.autocall.model;

import android.content.Context;

import androidx.room.Room;

public class DatabaseHelper {
    public static AppDatabase getDB(Context context) {
        return Room.databaseBuilder(context,
                AppDatabase.class, "ussdata.db").build();
    }
}
