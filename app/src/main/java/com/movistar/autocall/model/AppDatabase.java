package com.movistar.autocall.model;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Code.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CodeDao codeDao();
}
