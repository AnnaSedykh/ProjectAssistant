package com.annasedykh.projectassistant;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class ProjectsFragment extends Fragment {
    private static final String TAG = "ProjectsFragment";
    public static final String TYPE_KEY = "type";

    private DriveClient driveClient;
    private DriveResourceClient driveResourceClient;

    private String type;
    private RecyclerView recycler;
    private ProjectsAdapter adapter;
    private SwipeRefreshLayout refresh;

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

        adapter = new ProjectsAdapter();
        Bundle args = getArguments();
        type = args.getString(TYPE_KEY, Project.TYPE_UNKNOWN);

        if (type.equals(Project.TYPE_UNKNOWN)) {
            throw new IllegalArgumentException("Unknown type");
        }

        MainActivity mainActivity = (MainActivity) getActivity();
        driveClient = mainActivity.getDriveClient();
        driveResourceClient = mainActivity.getDriveResourceClient();
        Log.i(TAG, "driveClient " + driveClient);
        Log.i(TAG, "driveResourceClient " + driveResourceClient);
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

        recycler = view.findViewById(R.id.project_list);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(adapter);

        refresh = view.findViewById(R.id.swipe_refresh);
        refresh.setColorSchemeColors(Color.CYAN, Color.BLUE, Color.GREEN);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(1000);
        itemAnimator.setRemoveDuration(1000);
        recycler.setItemAnimator(itemAnimator);

        loadData();
    }

    private void loadData() {

        List<Project> projects = new ArrayList<>();
        projects.add(new Project("Москва"));
        projects.add(new Project("Сочи"));
        projects.add(new Project("Красноярск"));
        projects.add(new Project("Обнинск"));
        projects.add(new Project("Калуга"));
        projects.add(new Project("Бангкок"));
        adapter.setData(projects);
//        createFolder();

    }

    private void createFolder() {
        driveResourceClient
                .getRootFolder()
                .continueWithTask(new Continuation<DriveFolder, Task<DriveFolder>>() {
                    @Override
                    public Task<DriveFolder> then(@NonNull Task<DriveFolder> task)
                            throws Exception {
                        DriveFolder parentFolder = task.getResult();
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle("New folder")
                                .setMimeType(DriveFolder.MIME_TYPE)
                                .setStarred(true)
                                .build();
                        return driveResourceClient.createFolder(parentFolder, changeSet);
                    }
                })
                .addOnSuccessListener(this.getActivity(),
                        new OnSuccessListener<DriveFolder>() {
                            @SuppressLint("StringFormatInvalid")
                            @Override
                            public void onSuccess(DriveFolder driveFolder) {
                                String id = driveFolder.getDriveId().encodeToString();
                                showMessage(getString(R.string.file_created, id));
                            }
                        })
                .addOnFailureListener(this.getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Unable to create file", e);
                        showMessage(getString(R.string.file_create_error));
                    }
                });
    }

    protected void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }
}
