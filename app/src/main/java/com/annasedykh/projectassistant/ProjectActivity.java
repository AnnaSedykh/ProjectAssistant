package com.annasedykh.projectassistant;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.annasedykh.projectassistant.service.ProjectService;

public class ProjectActivity extends AppCompatActivity {

    private ProjectService projectService;

    private ProjectFile project;
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private RecyclerView recycler;
    private ProjectsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);
        recycler = findViewById(R.id.project_files);

        adapter = new ProjectsAdapter();
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        project = getIntent().getParcelableExtra(ProjectFile.PROJECT);
        projectService = ((App) getApplication()).getProjectService();

        setRecyclerAnimation();

        if (project != null) {
            toolbar.setTitle(project.getName());
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            projectService.showFolderContent(project.getId(), adapter, progressBar);
        }
    }

    private void setRecyclerAnimation() {
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(1000);
        itemAnimator.setRemoveDuration(1000);
        recycler.setItemAnimator(itemAnimator);
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
