package com.annasedykh.projectassistant;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

public class ProjectFile implements Parcelable {
    public static final String PROJECT = "project";
    public static final String PHOTO_RU = "фото";
    public static final String PHOTO_EN = "photo";

    public static final String PHOTO_VIEW = "photo";
    public static final String LIST_VIEW = "list";

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(mimeType);
    }

    protected ProjectFile(Parcel in) {
        id = in.readString();
        name = in.readString();
        mimeType = in.readString();
    }

    public static final Creator<ProjectFile> CREATOR = new Creator<ProjectFile>() {
        @Override
        public ProjectFile createFromParcel(Parcel in) {
            return new ProjectFile(in);
        }

        @Override
        public ProjectFile[] newArray(int size) {
            return new ProjectFile[size];
        }
    };

    public static class SortedByFolderAndName implements Comparator<ProjectFile> {
        private static final String FOLDER = "folder";

        @Override
        public int compare(ProjectFile o1, ProjectFile o2) {
            if (!o1.mimeType.equals(o2.mimeType)) {
                if (o1.mimeType.contains(FOLDER)) {
                    return -1;
                } else if (o2.mimeType.contains(FOLDER)) {
                    return 1;
                }
            }
            return o1.name.compareTo(o2.name);
        }
    }
}
