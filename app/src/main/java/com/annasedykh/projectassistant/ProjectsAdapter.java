package com.annasedykh.projectassistant;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.annasedykh.projectassistant.service.ProjectService;

import java.util.ArrayList;
import java.util.List;

public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder> {

    private List<ProjectFile> data = new ArrayList<>();
    private ProjectsFragment projectsFragment;

    public ProjectsAdapter(ProjectsFragment projectsFragment) {
        this.projectsFragment = projectsFragment;
    }

    @Override
    public ProjectsAdapter.ProjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_file, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProjectViewHolder holder, int position) {
        ProjectFile project = data.get(position);
        holder.bind(project, position, projectsFragment);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<ProjectFile> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public List<ProjectFile> getData() {
        return data;
    }


    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        private static final String MIME_TYPE_FOLDER = "application/vnd.google-apps.folder";
        private static final String MIME_TYPE_JPG = "image/jpeg";
        private static final String MIME_TYPE_DWG = "image/vnd.dwg";
        private static final String MIME_TYPE_PDF = "application/pdf";

        private final TextView text;
        private final ImageView image;

        public ProjectViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.file_title);
            image = itemView.findViewById(R.id.file_image);
        }

        public void bind(final ProjectFile project, final int position, final ProjectsFragment projectsFragment) {
            text.setText(project.getName());

            switch (project.getMimeType()) {
                case MIME_TYPE_FOLDER:
                    image.setImageResource(R.drawable.ic_mime_folder);
                    break;
                case MIME_TYPE_JPG:
                    image.setImageResource(R.drawable.ic_mime_image);
                    break;
                case MIME_TYPE_DWG:
                    image.setImageResource(R.drawable.ic_mime_dwg);
                    break;
                case MIME_TYPE_PDF:
                    image.setImageResource(R.drawable.ic_mime_pdf);
                    break;
                default:
                    image.setImageResource(R.drawable.ic_mime_other);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (project.getMimeType().equals(MIME_TYPE_FOLDER)) {
                        ProjectService projectService = projectsFragment.getProjectService();
                        projectService.showFolderContent(project.getId(), projectsFragment);
                    }
                }
            });
        }
    }
}
