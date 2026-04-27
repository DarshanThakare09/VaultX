package com.example.vaultx;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "files_table")
public class FileEntity {
    
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String path;
    public String type; // e.g., "IMAGE", "PDF", "DOCUMENT"
    public boolean isEncrypted;
    public boolean isFavorite;
    public long timestamp;

    public FileEntity(String name, String path, String type, boolean isEncrypted, boolean isFavorite, long timestamp) {
        this.name = name;
        this.path = path;
        this.type = type;
        this.isEncrypted = isEncrypted;
        this.isFavorite = isFavorite;
        this.timestamp = timestamp;
    }
}
