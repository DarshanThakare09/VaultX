package com.example.vaultx;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FileDao {

    @Insert
    void insert(FileEntity file);

    @Update
    void update(FileEntity file);

    @Delete
    void delete(FileEntity file);

    @Query("SELECT * FROM files_table ORDER BY timestamp DESC")
    List<FileEntity> getAllFiles();

    @Query("SELECT * FROM files_table WHERE type = :fileType ORDER BY timestamp DESC")
    List<FileEntity> getFilesByType(String fileType);

    @Query("SELECT * FROM files_table WHERE isFavorite = 1 ORDER BY timestamp DESC")
    List<FileEntity> getFavorites();
    
    @Query("SELECT * FROM files_table WHERE isEncrypted = 1 ORDER BY timestamp DESC")
    List<FileEntity> getLockedFiles();
}
