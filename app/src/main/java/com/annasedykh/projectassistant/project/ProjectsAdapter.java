package com.annasedykh.projectassistant.project;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.annasedykh.projectassistant.R;
import com.annasedykh.projectassistant.main.CommonDialog;
import com.annasedykh.projectassistant.main.MainActivity;
import com.annasedykh.projectassistant.service.ProjectService;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * {@link ProjectsAdapter} displays a scrolling list of {@link ProjectFile} objects using RecyclerView.
 */
public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder> {

    private static final String OWNER_ACCOUNT = "anna.emulator@gmail.com";
    static final String READ_PERMISSION = "request read permission";
    static final String WRITE_PERMISSION = "request write permission";

    private ProjectService service;
    private Set<String> changedFilesIds;
    private List<ProjectFile> data = new ArrayList<>();
    private String dataViewType;

    public ProjectsAdapter(ProjectService service) {
        this.service = service;
    }

    /**
     * Create new views (invoked by the layout manager).
     */
    @NonNull
    @Override
    public ProjectsAdapter.ProjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        switch (dataViewType) {
            case ProjectFile.PHOTO_VIEW:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo, parent, false);
                ProgressBar progressBar = view.findViewById(R.id.progressBar);
                return new ProjectViewHolder(view, service, progressBar);
            case ProjectFile.LIST_VIEW:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_file, parent, false);
        }
        return new ProjectViewHolder(view, service);
    }

    /**
     * Replace the contents of a view (invoked by the layout manager).
     */
    @Override
    public void onBindViewHolder(ProjectViewHolder holder, int position) {
        ProjectFile project = data.get(position);
        boolean hasChanges = false;
        if (changedFilesIds != null) {
            hasChanges = changedFilesIds.contains(project.getId());
        }
        holder.bind(project, hasChanges);
    }

    /**
     * Return the size of project file's dataset (invoked by the layout manager)
     */
    @Override
    public int getItemCount() {
        return data.size();
    }

    /**
     * Set data with last changes
     */
    public void setData(List<ProjectFile> data) {
        this.data = data;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            data.sort(new ProjectFile.SortedByFolderAndName());
        }
        changedFilesIds = service.getChangedFilesIds();
        notifyDataSetChanged();
    }

    public void addProjectFile(ProjectFile projectFile) {
        data.add(projectFile);
        notifyItemInserted(data.size());
    }

    /**
     * Starts async task to show files in selected folder
     */
    public void showFilesInFolder(String folderId, String dataViewType, ProgressBar progressBar) {
        this.dataViewType = dataViewType;
        new ShowFilesInFolderTask(this, progressBar, folderId).execute();
    }

    /**
     * Task requests for project files in selected folder and display them to user.
     */
    @SuppressLint("StaticFieldLeak")
    private class ShowFilesInFolderTask extends AsyncTask<String, Void, List<ProjectFile>> {
        private ProjectsAdapter adapter;
        private ProgressBar progressBar;
        private String folderId;

        ShowFilesInFolderTask(ProjectsAdapter adapter, ProgressBar progressBar, String folderId) {
            this.adapter = adapter;
            this.progressBar = progressBar;
            this.folderId = folderId;
        }

        @Override
        protected List<ProjectFile> doInBackground(String... strings) {
            return service.getFilesFromFolder(folderId);
        }

        @Override
        protected void onPostExecute(List<ProjectFile> projects) {
            adapter.setData(projects);
            progressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Starts async task to show files in root folder
     */
    public void showFilesInRootFolder(String folderId, String dataViewType,
                                      ProgressBar progressBar, FragmentActivity currentActivity) {
        this.dataViewType = dataViewType;
        new ShowFilesInRootFolderTask(this, progressBar, folderId, currentActivity).execute();
    }

    /**
     * Task requests for project files in root folder.
     * If success - update UI to display them to user.
     * If failed - request for read permission via email.
     */
    @SuppressLint("StaticFieldLeak")
    private class ShowFilesInRootFolderTask extends AsyncTask<String, Void, List<ProjectFile>> {
        private ProjectsAdapter adapter;
        private ProgressBar progressBar;
        private String folderId;
        private MainActivity mainActivity;

        ShowFilesInRootFolderTask(ProjectsAdapter adapter, ProgressBar progressBar,
                                  String folderId, FragmentActivity currentActivity) {
            this.adapter = adapter;
            this.progressBar = progressBar;
            this.folderId = folderId;
            this.mainActivity = (MainActivity) currentActivity;
        }

        @Override
        protected List<ProjectFile> doInBackground(String... strings) {
            return service.getFilesFromFolder(folderId);
        }

        @Override
        protected void onPostExecute(List<ProjectFile> projects) {
            adapter.setData(projects);
            progressBar.setVisibility(View.GONE);

            if (projects.isEmpty()) {
                if (!mainActivity.isAccessRequestSent()) {
                    requestPermission(READ_PERMISSION, mainActivity);
                    mainActivity.setAccessRequestSent(true);
                } else {
                    Toast.makeText(mainActivity, mainActivity.getString(R.string.still_no_permission_msg), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * Shows no permission dialog.
     */
    void requestPermission(String permissionType, AppCompatActivity activity) {
        switch (permissionType) {
            case READ_PERMISSION:
                CommonDialog.show(
                        activity.getString(R.string.no_read_permission_dialog),
                        activity.getString(R.string.request_permission_msg),
                        new NoAccessDialogListener(activity, activity.getString(R.string.email_text_read_access), permissionType),
                        activity.getSupportFragmentManager());
                break;
            case WRITE_PERMISSION:
                CommonDialog.show(
                        activity.getString(R.string.no_write_permission_dialog),
                        activity.getString(R.string.request_permission_msg),
                        new NoAccessDialogListener(activity, activity.getString(R.string.email_text_write_access), permissionType),
                        activity.getSupportFragmentManager());
        }
    }

    /**
     * Starts activity to send email asking permission to view project files or load photos on Google Drive.
     */
    private void sendAccessRequest(AppCompatActivity currentActivity, String emailText, String permissionType) {

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(currentActivity);
        String email = account.getEmail();
        String name = account.getDisplayName();
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setType("message/rfc822");
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{OWNER_ACCOUNT});
        intent.putExtra(Intent.EXTRA_SUBJECT, currentActivity.getString(R.string.request_permission));
        intent.putExtra(Intent.EXTRA_TEXT, currentActivity.getString(R.string.email_template, email, name, emailText));
        try {
            currentActivity.startActivity(Intent.createChooser(intent, currentActivity.getString(R.string.email_chooser)));

            if (READ_PERMISSION.equals(permissionType)) {
                CommonDialog.show(currentActivity.getString(R.string.request_sent),
                        currentActivity.getString(R.string.exit_msg),
                        new CloseAppDialogListener(currentActivity),
                        currentActivity.getSupportFragmentManager());
            }
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Send access request on OK button click
     */
    private class NoAccessDialogListener implements DialogInterface.OnClickListener {
        private AppCompatActivity currentActivity;
        private String emailText;
        private String permissionType;

        public NoAccessDialogListener(AppCompatActivity currentActivity, String emailText, String permissionType) {
            this.currentActivity = currentActivity;
            this.emailText = emailText;
            this.permissionType = permissionType;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    sendAccessRequest(currentActivity, emailText, permissionType);
            }
        }
    }

    /**
     * Finish activity on OK button click
     */
    private class CloseAppDialogListener implements DialogInterface.OnClickListener {
        private Activity currentActivity;

        CloseAppDialogListener(Activity currentActivity) {
            this.currentActivity = currentActivity;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    currentActivity.finish();
            }
        }
    }

    /**
     * {@link ProjectViewHolder} provide a reference to the views for each project file
     */
    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        private static final String OPEN_URL = "https://drive.google.com/open?id=";
        private static final String MIME_TYPE_FOLDER = "application/vnd.google-apps.folder";
        private static final String MIME_TYPE_JPG = "image/jpeg";
        private static final String MIME_TYPE_PDF = "application/pdf";

        private final TextView text;
        private final ImageView image;
        private final ImageView changes;
        private final Context context;
        private final ProjectService service;
        private ProgressBar progressBar;


        ProjectViewHolder(View itemView, ProjectService service, ProgressBar progressBar) {
            this(itemView, service);
            this.progressBar = progressBar;
        }

        ProjectViewHolder(View itemView, ProjectService service) {
            super(itemView);
            text = itemView.findViewById(R.id.file_title);
            image = itemView.findViewById(R.id.image);
            changes = itemView.findViewById(R.id.changes);
            context = itemView.getContext();
            this.service = service;
        }

        /**
         * Binds view with project file
         */
        void bind(final ProjectFile projectFile, final boolean hasChanges) {
            if (text != null) {
                text.setText(projectFile.getName());
            }
            if (changes != null) {
                if (hasChanges) {
                    changes.setVisibility(View.VISIBLE);
                } else {
                    changes.setVisibility(View.INVISIBLE);
                }
            }

            //Set image icon and click listener according to file mime type
            switch (projectFile.getMimeType()) {
                case MIME_TYPE_FOLDER:
                    String nameLowCase = projectFile.getName().toLowerCase();
                    if (nameLowCase.contains(ProjectFile.PHOTO_RU) || nameLowCase.contains(ProjectFile.PHOTO_EN)) {
                        image.setImageResource(R.drawable.ic_photo);
                        startProjectActivityOnClick(projectFile, ProjectFile.PHOTO_VIEW);
                    } else {
                        image.setImageResource(R.drawable.ic_mime_folder);
                        startProjectActivityOnClick(projectFile, ProjectFile.LIST_VIEW);
                    }
                    break;
                case MIME_TYPE_JPG:
                    startViewInBrowserOnClick(projectFile);
                    new ShowPhotoTask(projectFile.getId(), image).execute();
                    break;
                case MIME_TYPE_PDF:
                    image.setImageResource(R.drawable.ic_mime_pdf);
                    startViewInBrowserOnClick(projectFile);
                    break;
                default:
                    image.setImageResource(R.drawable.ic_mime_other);
                    startViewInBrowserOnClick(projectFile);
            }
        }

        /**
         * Task loads photo from Google Drive by id and displays it as ImageView.
         */
        @SuppressLint("StaticFieldLeak")
        private class ShowPhotoTask extends AsyncTask<String, Void, byte[]> {
            private String photoId;
            private ImageView image;

            public ShowPhotoTask(String photoId, ImageView image) {
                this.photoId = photoId;
                this.image = image;
            }

            @Override
            protected byte[] doInBackground(String... strings) {
                return service.getFileAsByteArray(photoId);
            }

            @Override
            protected void onPostExecute(byte[] imageByteArray) {
                if (imageByteArray != null) {
                    int width = context.getResources().getDisplayMetrics().widthPixels;
                    int columns = ProjectActivity.COLUMN_NUMBER;

                    try {
                        Glide.with(context)
                                .load(imageByteArray)
                                .apply(new RequestOptions()
                                        .centerCrop()
                                        .override(width / columns, width / columns))
                                .into(image);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        }

        /**
         * Start ProjectActivity on item click.
         */
        private void startProjectActivityOnClick(final ProjectFile projectFile, final String dataViewType) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent projectIntent = new Intent(context, ProjectActivity.class);
                    projectIntent.putExtra(ProjectFile.PROJECT, projectFile);
                    projectIntent.putExtra("dataViewType", dataViewType);
                    context.startActivity(projectIntent);
                    if (changes != null) {
                        changes.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }

        /**
         * Start view in browser activity on item click.
         */
        private void startViewInBrowserOnClick(final ProjectFile projectFile) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = OPEN_URL + projectFile.getId();
                    Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                    viewIntent.setData(Uri.parse(url));
                    context.startActivity(viewIntent);
                    if (changes != null) {
                        changes.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }
    }
}
