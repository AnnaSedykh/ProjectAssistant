package com.annasedykh.projectassistant;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.annasedykh.projectassistant.service.ProjectService;

public class ProjectActivity extends AppCompatActivity {
    public static final int COLUMN_NUMBER = 3;

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
}
