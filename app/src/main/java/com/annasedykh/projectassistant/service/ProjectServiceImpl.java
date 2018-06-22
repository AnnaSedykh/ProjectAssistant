package com.annasedykh.projectassistant.service;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import com.annasedykh.projectassistant.ProjectFile;
import com.annasedykh.projectassistant.ProjectsAdapter;
import com.annasedykh.projectassistant.ProjectsFragment;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProjectServiceImpl implements ProjectService {

    private Drive driveService;

    public ProjectServiceImpl(Drive driveService) {
        this.driveService = driveService;
    }


    @Override
    public void showFolderContent(String folderId, ProjectsFragment fragment) {
        new GetFoldersContentTask(fragment).execute(folderId);
    }

    @SuppressLint("StaticFieldLeak")
    private class GetFoldersContentTask extends AsyncTask<String, Void, List<ProjectFile>> {
        private ProjectsFragment fragment;

        public GetFoldersContentTask(ProjectsFragment fragment) {
            this.fragment = fragment;
        }

        @Override
        protected List<ProjectFile> doInBackground(String... folderIds) {
            List<ProjectFile> projects = new ArrayList<>();
            for (String folderId : folderIds) {
                FileList result = null;
                try {
                    result = driveService.files().list()
                            .setQ("'" + folderId + "' in parents and trashed = false")
                            .execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (result != null) {
                    for (File file : result.getFiles()) {
                        projects.add(new ProjectFile(file.getId(), file.getName(), file.getMimeType()));
                    }
                }
            }
            return projects;
        }

        @Override
        protected void onPostExecute(List<ProjectFile> projects) {
            ProjectsAdapter adapter = fragment.getProjectAdapter();
            adapter.setData(projects);
            ProgressBar progressBar = fragment.getProgressBar();
            progressBar.setVisibility(View.GONE);
        }
    }
}
