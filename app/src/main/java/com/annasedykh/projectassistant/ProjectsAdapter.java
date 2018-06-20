package com.annasedykh.projectassistant;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder>{

    private List<Project> data = new ArrayList<>();

    @Override
    public ProjectsAdapter.ProjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProjectViewHolder holder, int position) {
        Project project = data.get(position);
        holder.bind(project, position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<Project> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public List<Project> getData() {
        return data;
    }


    static class ProjectViewHolder extends RecyclerView.ViewHolder{

        private final TextView textView;

        public ProjectViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.project_title);

        }

        public void bind(final Project project, final int position) {
            textView.setText(project.getName());
        }
    }
}
