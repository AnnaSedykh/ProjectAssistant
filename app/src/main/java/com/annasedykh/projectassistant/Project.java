package com.annasedykh.projectassistant;

public class Project {
    public static final String TYPE_CURRENT = "0";
    public static final String TYPE_FINISHED = "1";
    public static final String TYPE_UNKNOWN = "-1";

    public int id;
    public String title;
    public String address;
    public String date;

    public Project(String title) {
        this.title = title;
    }
}
