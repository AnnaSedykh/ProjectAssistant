package com.annasedykh.projectassistant.service;

import com.annasedykh.projectassistant.ProjectsAdapter;

public interface ProjectService {

    void showFolderContent(String folderId, ProjectsAdapter projectAdapter);
}
