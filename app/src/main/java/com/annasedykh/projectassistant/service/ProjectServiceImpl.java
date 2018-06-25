package com.annasedykh.projectassistant.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import com.annasedykh.projectassistant.BuildConfig;
import com.annasedykh.projectassistant.ProjectFile;
import com.annasedykh.projectassistant.ProjectsAdapter;
import com.annasedykh.projectassistant.R;
import com.google.android.gms.common.Scopes;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProjectServiceImpl implements ProjectService {

    private Drive driveService;
    private GoogleAccountCredential credential;
    private Context context;

    public ProjectServiceImpl(Context context) {
        this.context = context;
    }

    private void initServices() {
        credential = GoogleAccountCredential.usingOAuth2(context, Collections.singleton(Scopes.DRIVE_FULL));
        credential.setSelectedAccountName(BuildConfig.APP_ACCOUNT_NAME);

        driveService = new Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
        ).setApplicationName(context.getString(R.string.app_name))
                .build();
    }


    @Override
    public void showFolderContent(String folderId, ProjectsAdapter adapter, ProgressBar progressBar) {
        if(driveService == null){
            initServices();
        }
        new GetFoldersContentTask(adapter, progressBar).execute(folderId);
    }

    @SuppressLint("StaticFieldLeak")
    private class GetFoldersContentTask extends AsyncTask<String, Void, List<ProjectFile>> {
        private ProjectsAdapter adapter;
        private ProgressBar progressBar;

        GetFoldersContentTask(ProjectsAdapter adapter, ProgressBar progressBar) {
            this.adapter = adapter;
            this.progressBar = progressBar;
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
            adapter.setData(projects);
            progressBar.setVisibility(View.GONE);
        }
    }
}
