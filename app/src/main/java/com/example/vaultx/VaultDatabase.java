package com.example.vaultx;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {FileEntity.class}, version = 1, exportSchema = false)
public abstract class VaultDatabase extends RoomDatabase {

    private static VaultDatabase instance;

    public abstract FileDao fileDao();

    public static synchronized VaultDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    VaultDatabase.class, "vault_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
