package com.example.vaultx;

import java.io.Serializable;

public class Note implements Serializable {
    private String title;
    private String content;
    private String colorHex;
    private String fileName;

    public Note(String title, String content, String colorHex, String fileName) {
        this.title = title;
        this.content = content;
        this.colorHex = colorHex;
        this.fileName = fileName;
    }

    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getColorHex() { return colorHex; }
    public String getFileName() { return fileName; }
}
