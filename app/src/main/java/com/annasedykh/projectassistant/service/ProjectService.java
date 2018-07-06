package com.annasedykh.projectassistant.service;

import com.annasedykh.projectassistant.ProjectFile;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.model.File;

import java.util.List;

public interface ProjectService {

    List<ProjectFile> getFilesFromFolder(String folderId);
    byte[] getFileAsByteArray(String fileId);
    File createFile(File fileMetadata, FileContent mediaContent);
}
