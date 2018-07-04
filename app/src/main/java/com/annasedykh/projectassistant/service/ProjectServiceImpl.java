package com.annasedykh.projectassistant.service;

import android.accounts.Account;
import android.content.Context;

import com.annasedykh.projectassistant.BuildConfig;
import com.annasedykh.projectassistant.ProjectFile;
import com.annasedykh.projectassistant.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.Scopes;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProjectServiceImpl implements ProjectService {

    private Drive driveService;
    private Context context;

    public ProjectServiceImpl(Context context) {
        this.context = context;
    }

    @Override
    public List<ProjectFile> getFilesFromFolder(String folderId) {
        if (driveService == null) {
            initDriveService();
        }
        List<ProjectFile> projects = new ArrayList<>();
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
        return projects;
    }

    @Override
    public byte[] getFileAsByteArray(String fileId) {
        if (driveService == null) {
            initDriveService();
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try {
                driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


        private void initDriveService() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        if (account != null) {
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(context, Collections.singleton(Scopes.DRIVE_FULL));
            credential.setSelectedAccount(new Account(BuildConfig.APP_ACCOUNT_NAME, BuildConfig.APPLICATION_ID));

            driveService = new Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential
            ).setApplicationName(context.getString(R.string.app_name))
                    .build();
        }
    }
}
