package com.annasedykh.projectassistant.service;

import com.annasedykh.projectassistant.project.ProjectFile;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.model.File;

import java.util.List;
import java.util.Set;

public interface ProjectService {

    List<ProjectFile> getFilesFromFolder(String folderId);
    byte[] getFileAsByteArray(String fileId);
    File createFile(File fileMetadata, FileContent mediaContent);
    void receiveLastChanges();
    Set<String> getChangedFilesIds();
}
