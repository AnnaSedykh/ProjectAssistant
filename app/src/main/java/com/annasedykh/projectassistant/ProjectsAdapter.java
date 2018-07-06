package com.annasedykh.projectassistant;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.annasedykh.projectassistant.service.ProjectService;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder> {

    private ProjectService service;
    private List<ProjectFile> data = new ArrayList<>();
    private String dataViewType;

    public ProjectsAdapter(ProjectService service) {
        this.service = service;
    }

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

    @Override
    public void onBindViewHolder(ProjectViewHolder holder, int position) {
        ProjectFile project = data.get(position);
        holder.bind(project, position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<ProjectFile> data) {
        this.data = data;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            data.sort(new ProjectFile.SortedByFolderAndName());
        }
        notifyDataSetChanged();
    }

    public void addProjectFile(ProjectFile projectFile){
        data.add(projectFile);
        notifyItemInserted(data.size());
    }

    public void showFilesInFolder(String folderId, String dataViewType, ProgressBar progressBar) {
        this.dataViewType = dataViewType;
        new ShowFilesInFolderTask(this, progressBar, folderId).execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class ShowFilesInFolderTask extends AsyncTask<String, Void, List<ProjectFile>> {
        private ProjectsAdapter adapter;
        private ProgressBar progressBar;
        private String folderId;

        public ShowFilesInFolderTask(ProjectsAdapter adapter, ProgressBar progressBar, String folderId) {
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

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        private static final String OPEN_URL = "https://drive.google.com/open?id=";
        private static final String MIME_TYPE_FOLDER = "application/vnd.google-apps.folder";
        private static final String MIME_TYPE_JPG = "image/jpeg";
        private static final String MIME_TYPE_PDF = "application/pdf";

        private final TextView text;
        private final ImageView image;
        private final Context context;
        private final ProjectService service;
        private ProgressBar progressBar;

        public ProjectViewHolder(View itemView, ProjectService service, ProgressBar progressBar) {
            this(itemView, service);
            this.progressBar = progressBar;
        }

        public ProjectViewHolder(View itemView, ProjectService service) {
            super(itemView);
            text = itemView.findViewById(R.id.file_title);
            image = itemView.findViewById(R.id.image);
            context = itemView.getContext();
            this.service = service;
        }

        public void bind(final ProjectFile projectFile, final int position) {
            if (text != null) {
                text.setText(projectFile.getName());
            }

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

                    Glide.with(context)
                            .load(imageByteArray)
                            .apply(new RequestOptions()
                                    .centerCrop()
                                    .override(width / columns, width / columns))
                            .into(image);
                }
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        }

        private void startProjectActivityOnClick(final ProjectFile projectFile, final String dataViewType) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent projectIntent = new Intent(context, ProjectActivity.class);
                    projectIntent.putExtra(ProjectFile.PROJECT, projectFile);
                    projectIntent.putExtra("dataViewType", dataViewType);
                    context.startActivity(projectIntent);
                }
            });
        }

        private void startViewInBrowserOnClick(final ProjectFile projectFile) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = OPEN_URL + projectFile.getId();
                    Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                    viewIntent.setData(Uri.parse(url));
                    context.startActivity(viewIntent);
                }
            });
        }
    }
}
