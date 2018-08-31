package com.annasedykh.projectassistant.service;

import android.accounts.Account;
import android.content.Context;
import android.text.TextUtils;

import com.annasedykh.projectassistant.BuildConfig;
import com.annasedykh.projectassistant.R;
import com.annasedykh.projectassistant.project.ProjectFile;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.Scopes;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.ChangeList;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.StartPageToken;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

/**
 * {@link ProjectServiceImpl} implementation of {@link ProjectService} for working with Google Drive REST API.
 */
public class ProjectServiceImpl implements ProjectService {

    private static final String PREFS_NAME = "shared_prefs";
    private static final String PAGE_TOKEN = "start_page_token";
    private Set<String> changedFilesIds = new HashSet<>();

    private Drive driveService;
    private Context context;

    public ProjectServiceImpl(Context context) {
        this.context = context;
    }

    /**
     * Get last changes as set of changed files id's
     * @return set of changed files id's
     */
    @Override
    public Set<String> getChangedFilesIds() {
        return changedFilesIds;
    }

    /**
     * Requests for project files in selected folder.
     * @param folderId - selected folder id
     * @return list of ProjectFile objects
     */
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

    /**
     * Requests for a single file by id.
     * @param fileId - selected file id
     * @return file as byte array
     */
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

    /**
     * Creates file on Google Drive.
     * @param fileMetadata - file metadata
     * @param mediaContent - file media content
     * @return File object of created file.
     */
    @Override
    public File createFile(File fileMetadata, FileContent mediaContent) {
        if (driveService == null) {
            initDriveService();
        }
        File file = null;
        try {
            file = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * Receive last changes of files from Google Drive.
     */
    @Override
    public void receiveLastChanges() {
        if (driveService == null) {
            initDriveService();
        }
        changedFilesIds.clear();
        // save changed file id with it's changes time
        Map<String, DateTime> changesMap = new HashMap<>();
        if (!hasStartPageToken()) {
            try {
                StartPageToken response = driveService.changes()
                        .getStartPageToken().execute();
                saveStartPageToken(response.getStartPageToken());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String pageToken = getStartPageToken();
        while (pageToken != null) {
            try {
                ChangeList changes = driveService.changes()
                        .list(pageToken)
                        .setIncludeRemoved(false)
                        .execute();

                for (Change change : changes.getChanges()) {
                    if (!context.getString(R.string.mime_type_folder).equals(change.getFile().getMimeType())) {
                        changesMap.put(change.getFileId(), change.getTime());
                    }
                }

                if (changes.getNewStartPageToken() != null) {
                    // Last page, save this token for the next polling interval
                    saveStartPageToken(changes.getNewStartPageToken());
                }
                pageToken = changes.getNextPageToken();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        resolveChanges(changesMap);
    }

    /**
     * If file's changes time equals modified time - this file was modified, not only viewed.
     * Add this file id to list of changed files
     * @param changesMap - map with pairs of changed file id - file changes time
     */
    private void resolveChanges(Map<String, DateTime> changesMap) {
        for (Map.Entry<String, DateTime> change : changesMap.entrySet()) {
            String fileId = change.getKey();
            long timeMillis = change.getValue().getValue();
            String changeTimeString = getTimeStringWithoutSeconds(timeMillis);
            try {
                File file = getFileById(fileId);
                DateTime modifiedTime = file.getModifiedTime();
                String modifiedTimeString = getTimeStringWithoutSeconds(modifiedTime.getValue());
                if (changeTimeString.equals(modifiedTimeString)) {
                    changedFilesIds.add(file.getId());
                    addParentsIds(file);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Recursively add all file parents ids to list of changed files
     * @param file - current file
     * @throws IOException - If a file could not be found
     */
    private void addParentsIds(File file) throws IOException {
        List<String> parents = file.getParents();
        if (parents != null) {
            boolean isCurrentProjectsFolder = parents.contains(context.getString(R.string.current_folder_id));
            boolean isFinishedProjectsFolder = parents.contains(context.getString(R.string.finished_folder_id));
            if (!isCurrentProjectsFolder && !isFinishedProjectsFolder) {
                changedFilesIds.addAll(parents);
                for (String parentId : parents) {
                    File parentFile = getFileById(parentId);
                    addParentsIds(parentFile);
                }
            }
        }
    }

    /**
     * Requests for a single file by id.
     * @param fileId - selected file id
     * @return file as File object
     */
    private File getFileById(String fileId) throws IOException {
        return driveService.files()
                .get(fileId)
                .setFields("id, parents, modifiedTime")
                .execute();
    }

    /**
     * Round date time to minutes
     * @param dateMillis - date as millis
     * @return formatted date string
     */
    private String getTimeStringWithoutSeconds(long dateMillis) {
        Date date = new Date(dateMillis);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US);
        return simpleDateFormat.format(date);
    }

    /**
     * Init Google Drive service
     */
    private void initDriveService() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        if (account != null) {
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(context, Collections.singleton(Scopes.DRIVE_FULL));
            credential.setSelectedAccount(new Account(account.getEmail(), BuildConfig.APPLICATION_ID));

            driveService = new Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential
            ).setApplicationName(context.getString(R.string.app_name))
                    .build();
        }
    }

    /**
     * Save start page token in shared prefs for the next polling interval
     */
    private void saveStartPageToken(String token) {
        context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(PAGE_TOKEN, token).apply();
    }

    private String getStartPageToken() {
        return context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(PAGE_TOKEN, null);
    }

    private boolean hasStartPageToken() {
        return !TextUtils.isEmpty(getStartPageToken());
    }
}
