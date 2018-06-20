package com.annasedykh.projectassistant.service;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import com.annasedykh.projectassistant.Project;
import com.annasedykh.projectassistant.ProjectsAdapter;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProjectServiceImpl implements ProjectService {
    private static final String CURRENT_FOLDER_ID = "1G8ozUR7jyP3DOiU-it2sa1_4j-EYqxUB";
    private static final String FINISHED_FOLDER_ID = "1g1xmm-jbrVxiQZJv6myo0CMhsqILiKom";
    private Drive driveService;
    private ProjectsAdapter adapter;

    public ProjectServiceImpl(Drive driveService) {
        this.driveService = driveService;
    }

    @Override
    public void showFinishedProjects(ProjectsAdapter adapter) {
        this.adapter = adapter;
        new GetFoldersContentTask().execute(FINISHED_FOLDER_ID);
    }

    @SuppressLint("StaticFieldLeak")
    private class GetFoldersContentTask extends AsyncTask<String, Void, List<Project>> {

        @Override
        protected List<Project> doInBackground(String... folderIds) {
            List<Project> projects = new ArrayList<>();
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
                        projects.add(new Project(file.getId(), file.getName()));
                    }
                }
            }
            return projects;
        }

        @Override
        protected void onPostExecute(List<Project> projects) {
            adapter.setData(projects);
            adapter.notifyDataSetChanged();
        }
    }
}
