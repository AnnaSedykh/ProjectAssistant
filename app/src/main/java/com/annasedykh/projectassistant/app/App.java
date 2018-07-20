package com.annasedykh.projectassistant.app;

import android.app.Application;

import com.annasedykh.projectassistant.service.ProjectService;
import com.annasedykh.projectassistant.service.ProjectServiceImpl;

public class App extends Application {
    private ProjectService projectService;

    @Override
    public void onCreate() {
        super.onCreate();
        projectService = new ProjectServiceImpl(this);
    }

    public ProjectService getProjectService() {
        return projectService;
    }
}
