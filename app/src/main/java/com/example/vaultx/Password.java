package com.example.vaultx;

import java.io.Serializable;

public class Password implements Serializable {
    private String title;
    private String username;
    private String password;
    private String website;
    private String fileName;

    public Password(String title, String username, String password, String website, String fileName) {
        this.title = title;
        this.username = username;
        this.password = password;
        this.website = website;
        this.fileName = fileName;
    }

    public String getTitle() { return title; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getWebsite() { return website; }
    public String getFileName() { return fileName; }
}
