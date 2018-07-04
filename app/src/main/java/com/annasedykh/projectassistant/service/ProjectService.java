package com.annasedykh.projectassistant.service;

import com.annasedykh.projectassistant.ProjectFile;

import java.util.List;

public interface ProjectService {

    List<ProjectFile> getFilesFromFolder(String folderId);
    byte[] getFileAsByteArray(String fileId);
}
