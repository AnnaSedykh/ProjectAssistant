package com.annasedykh.projectassistant;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.annasedykh.projectassistant.service.ProjectService;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProjectActivity extends AppCompatActivity {
    public static final int COLUMN_NUMBER = 3;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final String AUTHORITY = "com.annasedykh.projectassistant.fileprovider";
    private java.io.File photoFile;

    private ProjectService projectService;
    private ProjectFile project;
    private String dataViewType;
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private RecyclerView recycler;
    private ProjectsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        projectService = ((App) getApplication()).getProjectService();
        project = getIntent().getParcelableExtra(ProjectFile.PROJECT);
        dataViewType = getIntent().getStringExtra("dataViewType");

        switch (dataViewType) {
            case ProjectFile.PHOTO_VIEW:
                setContentView(R.layout.activity_photo);
                break;
            case ProjectFile.LIST_VIEW:
                setContentView(R.layout.activity_list);
        }

        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);
        recycler = findViewById(R.id.recycler);

        adapter = new ProjectsAdapter(projectService);
        recycler.setAdapter(adapter);
        switch (dataViewType) {
            case ProjectFile.PHOTO_VIEW:
                recycler.setLayoutManager(new GridLayoutManager(this, COLUMN_NUMBER));
                break;
            case ProjectFile.LIST_VIEW:
                recycler.setLayoutManager(new LinearLayoutManager(this));
        }

        if (project != null) {
            toolbar.setTitle(project.getName());
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            adapter.showFilesInFolder(project.getId(), dataViewType, progressBar);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (dataViewType.equals(ProjectFile.PHOTO_VIEW)) {
            getMenuInflater().inflate(R.menu.photo_menu, menu);
            MenuItem takePhoto = menu.findItem(R.id.take_photo);
            takePhoto.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    takePhotoIntent();
                    return true;
                }
            });
            MenuItem loadFromGallery = menu.findItem(R.id.load_from_gallery);
            loadFromGallery.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
//                    CommonDialog.show(getString(R.string.logout), getString(R.string.dialog_msg), new MainActivity.LogoutDialogListener(), getSupportFragmentManager());
                    return true;
                }
            });
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            File fileMetadata = new File();
            fileMetadata.setName(photoFile.getName());
            List<String> parents = new ArrayList<>();
            parents.add(project.getId());
            fileMetadata.setParents(parents);
            FileContent mediaContent = new FileContent(getString(R.string.mime_type_jpeg), photoFile);
            createFileOnDrive(fileMetadata, mediaContent);
        }
    }

    private void takePhotoIntent() {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)
                && takePhotoIntent.resolveActivity(getPackageManager()) != null) {
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        AUTHORITY,
                        photoFile);
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePhotoIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private java.io.File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = DateFormat.getDateTimeInstance().format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        java.io.File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        java.io.File photoFile = java.io.File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return photoFile;
    }

    public void createFileOnDrive(File fileMetadata, FileContent mediaContent) {
        new CreateFileTask(fileMetadata, mediaContent).execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class CreateFileTask extends AsyncTask<Void, Void, File> {
        private File fileMetadata;
        private FileContent mediaContent;

        public CreateFileTask(File fileMetadata, FileContent mediaContent) {
            this.fileMetadata = fileMetadata;
            this.mediaContent = mediaContent;
        }

        @Override
        protected File doInBackground(Void... voids) {
            return projectService.createFile(fileMetadata, mediaContent);
        }

        @Override
        protected void onPostExecute(File file) {
            if(file != null){
                ProjectFile projectFile = new ProjectFile(file.getId(), photoFile.getName(), getString(R.string.mime_type_jpeg));
                adapter.addProjectFile(projectFile);
            }
        }
    }
}
