package com.annasedykh.projectassistant;

public class Project {
    public static final String TYPE_CURRENT = "0";
    public static final String TYPE_FINISHED = "1";
    public static final String TYPE_UNKNOWN = "-1";

    private String id;
    private String name;


    public Project(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
