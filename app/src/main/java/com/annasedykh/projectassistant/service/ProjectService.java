package com.annasedykh.projectassistant.service;

import com.annasedykh.projectassistant.project.ProjectFile;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.model.File;

import java.util.List;
import java.util.Set;

/**
 * {@link ProjectService} interface for working with Google Drive REST API.
 */
public interface ProjectService {

    /**
     * Requests for project files in selected folder.
     * @param folderId - selected folder id
     * @return list of ProjectFile objects
     */
    List<ProjectFile> getFilesFromFolder(String folderId);

    /**
     * Requests for a single file by id.
     * @param fileId - selected file id
     * @return file as byte array
     */
    byte[] getFileAsByteArray(String fileId);

    /**
     * Creates file on Google Drive.
     * @param fileMetadata - file metadata
     * @param mediaContent - file media content
     * @return File object of created file.
     */
    File createFile(File fileMetadata, FileContent mediaContent);

    /**
     * Receive last changes of files from Google Drive.
     */
    void receiveLastChanges();

    /**
     * Get last changes as set of changed files id's
     * @return set of changed files id's
     */
    Set<String> getChangedFilesIds();
}
