package com.annasedykh.projectassistant.service;

import android.widget.ProgressBar;

import com.annasedykh.projectassistant.ProjectsAdapter;

public interface ProjectService {

    void showFolderContent(String folderId, ProjectsAdapter adapter, ProgressBar progressBar);
}
