package com.annasedykh.projectassistant;

public class ProjectFile {
    public static final String TYPE_CURRENT = "0";
    public static final String TYPE_FINISHED = "1";
    public static final String TYPE_UNKNOWN = "-1";

    private String id;
    private String name;
    private String mimeType;


    public ProjectFile(String id, String name, String mimeType) {
        this.id = id;
        this.name = name;
        this.mimeType = mimeType;
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

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
