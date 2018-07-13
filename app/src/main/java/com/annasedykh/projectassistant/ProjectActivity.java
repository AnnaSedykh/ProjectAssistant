package com.annasedykh.projectassistant;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.annasedykh.projectassistant.service.ProjectService;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProjectActivity extends AppCompatActivity {
    private static final String TAG = "ProjectActivity";
    public static final int COLUMN_NUMBER = 3;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_IMAGE_FROM_GALLERY = 2;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 11;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 22;
    private static final String AUTHORITY = "com.annasedykh.projectassistant.fileprovider";
    private java.io.File photoFromCamera;

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
            MenuItem takePhotoItem = menu.findItem(R.id.take_photo);
            takePhotoItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    takePhoto();
                    return true;
                }
            });
            final MenuItem loadFromGalleryItem = menu.findItem(R.id.load_from_gallery);
            loadFromGalleryItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    loadFromGallery();
                    return true;
                }
            });
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadFromGallery();
                } else {
                    showMessage(getString(R.string.permission_storage_denied));
                }
                break;
            case MY_PERMISSIONS_REQUEST_CAMERA:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto();
                } else {
                    showMessage(getString(R.string.permission_camera_denied));
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    createFileOnDrive(photoFromCamera, true);
                }
                break;
            case REQUEST_IMAGE_FROM_GALLERY:
                if (resultCode == RESULT_OK) {
                    if (data != null && data.getClipData() != null) {
                        for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                            Uri photoUri = data.getClipData().getItemAt(i).getUri();
                            String realPath = getRealPathFromURI(this, photoUri);
                            java.io.File photoFile = new java.io.File(realPath);
                            createFileOnDrive(photoFile, false);
                        }
                    }
                }
        }
    }

    private void loadFromGallery() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(intent, REQUEST_IMAGE_FROM_GALLERY);
        }
    }

    private static String getRealPathFromURI(Context context, Uri uri) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();
        return filePath;
    }

    private void takePhoto() {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)
                && takePhotoIntent.resolveActivity(getPackageManager()) != null) {
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            try {
                photoFromCamera = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photoFromCamera != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        AUTHORITY,
                        photoFromCamera);
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePhotoIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private java.io.File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = DateFormat.getDateTimeInstance().format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        java.io.File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        java.io.File imageFile = java.io.File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return imageFile;
    }

    private void createFileOnDrive(java.io.File photoFile, boolean deleteAfterLoad) {
        File fileMetadata = new File();
        fileMetadata.setName(photoFile.getName());
        List<String> parents = new ArrayList<>();
        parents.add(project.getId());
        fileMetadata.setParents(parents);
        FileContent mediaContent = new FileContent(getString(R.string.mime_type_jpeg), photoFile);
        new CreateFileTask(fileMetadata, mediaContent, photoFile, deleteAfterLoad).execute();
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @SuppressLint("StaticFieldLeak")
    private class CreateFileTask extends AsyncTask<Void, Void, File> {
        private File fileMetadata;
        private FileContent mediaContent;
        private java.io.File photoFile;
        private boolean deleteAfterLoad;

        public CreateFileTask(File fileMetadata, FileContent mediaContent, java.io.File photoFile, boolean deleteAfterLoad) {
            this.fileMetadata = fileMetadata;
            this.mediaContent = mediaContent;
            this.photoFile = photoFile;
            this.deleteAfterLoad = deleteAfterLoad;
        }

        @Override
        protected File doInBackground(Void... voids) {
            return projectService.createFile(fileMetadata, mediaContent);
        }

        @Override
        protected void onPostExecute(File file) {
            if (file != null) {
                ProjectFile projectFile = new ProjectFile(file.getId(), photoFile.getName(), getString(R.string.mime_type_jpeg));
                adapter.addProjectFile(projectFile);
            }
            if (deleteAfterLoad) {
                deleteLocalImageFile(photoFile);
            }
        }
    }

    private void deleteLocalImageFile(java.io.File photoFile) {
        if (photoFile.exists()) {
            String filePath = photoFile.getPath();
            if (photoFile.delete()) {
                Log.i(TAG, "deleteLocalImageFile: File was deleted: " + filePath);
            } else {
                Log.e(TAG, "deleteLocalImageFile: File was not deleted: " + filePath);
            }
        }

    }
}
