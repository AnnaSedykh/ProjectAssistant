package com.annasedykh.projectassistant;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.annasedykh.projectassistant.service.ProjectService;

public class ProjectsFragment extends Fragment {
    private static final String CURRENT_FOLDER_ID = "1G8ozUR7jyP3DOiU-it2sa1_4j-EYqxUB";
    private static final String FINISHED_FOLDER_ID = "1g1xmm-jbrVxiQZJv6myo0CMhsqILiKom";
    private static final String TAG = "ProjectsFragment";
    public static final String TYPE_KEY = "type";

    private ProjectService projectService;

    private String type;
    private ProgressBar progressBar;
    private RecyclerView recycler;
    private ProjectsAdapter projectAdapter;

    public static ProjectsFragment createProjectFragment(String type) {
        ProjectsFragment fragment = new ProjectsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ProjectsFragment.TYPE_KEY, type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        type = args.getString(TYPE_KEY, ProjectFile.TYPE_UNKNOWN);

        if (type.equals(ProjectFile.TYPE_UNKNOWN)) {
            throw new IllegalArgumentException("Unknown type");
        }

        projectService = ((MainActivity) getActivity()).getProjectService();
        projectAdapter = new ProjectsAdapter(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_projects, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);
        recycler = view.findViewById(R.id.project_list);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(projectAdapter);
        setRecyclerAnimation();

        loadProjectsData();
    }

    private void loadProjectsData() {
        switch (type){
            case ProjectFile.TYPE_CURRENT:
                projectService.showFolderContent(CURRENT_FOLDER_ID, this); break;
            case ProjectFile.TYPE_FINISHED:
                projectService.showFolderContent(FINISHED_FOLDER_ID, this);break;
        }
    }

    private void setRecyclerAnimation() {
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(1000);
        itemAnimator.setRemoveDuration(1000);
        recycler.setItemAnimator(itemAnimator);
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public ProjectsAdapter getProjectAdapter() {
        return projectAdapter;
    }

    public ProjectService getProjectService() {
        return projectService;
    }

}
