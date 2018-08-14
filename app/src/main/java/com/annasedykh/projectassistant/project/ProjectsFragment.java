package com.annasedykh.projectassistant.project;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.annasedykh.projectassistant.R;
import com.annasedykh.projectassistant.app.App;
import com.annasedykh.projectassistant.service.ProjectService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ProjectsFragment extends Fragment {
    public static final String CURRENT_FOLDER_ID = "1G8ozUR7jyP3DOiU-it2sa1_4j-EYqxUB";
    public static final String FINISHED_FOLDER_ID = "1g1xmm-jbrVxiQZJv6myo0CMhsqILiKom";
    public static final String TYPE_KEY = "type";

    private ProjectService projectService;
    private ProjectsAdapter projectAdapter;
    private String type;
    private Unbinder unbinder;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.project_list)
    RecyclerView recycler;

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

        projectService = ((App) getActivity().getApplication()).getProjectService();
        projectAdapter = new ProjectsAdapter(projectService);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_projects, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(projectAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        loadProjectsData();
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    private void loadProjectsData() {
        switch (type) {
            case ProjectFile.TYPE_CURRENT:
                projectAdapter.showFilesInRootFolder(CURRENT_FOLDER_ID, ProjectFile.LIST_VIEW, progressBar, this.getActivity());
                break;
            case ProjectFile.TYPE_FINISHED:
                projectAdapter.showFilesInRootFolder(FINISHED_FOLDER_ID, ProjectFile.LIST_VIEW, progressBar, this.getActivity());
        }
    }
}
