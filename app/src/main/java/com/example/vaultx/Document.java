package com.example.vaultx;

import java.io.Serializable;

public class Document implements Serializable {
    private String title;
    private String content;   // preview text — may be empty for binary files
    private String fileName;
    private String filePath;  // absolute path on disk
    private String mimeType;  // e.g. application/pdf

    public Document(String title, String content, String fileName, String filePath, String mimeType) {
        this.title = title;
        this.content = content;
        this.fileName = fileName;
        this.filePath = filePath;
        this.mimeType = mimeType;
    }

    // Backward-compat constructor (text documents)
    public Document(String title, String content, String fileName) {
        this(title, content, fileName, "", "text/plain");
    }

    public String getTitle()    { return title; }
    public String getContent()  { return content; }
    public String getFileName() { return fileName; }
    public String getFilePath() { return filePath; }
    public String getMimeType() { return mimeType; }

    public void setTitle(String title)       { this.title = title; }
    public void setContent(String content)   { this.content = content; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
}
