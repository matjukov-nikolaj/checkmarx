package com.capitalone.dashboard.collector;

import codesecurity.collectors.collector.CodeSecurityCollectorController;
import com.capitalone.dashboard.model.CheckMarx;
import com.capitalone.dashboard.model.CheckMarxProject;
import com.capitalone.dashboard.model.CheckMarxCollector;
import com.capitalone.dashboard.repository.CheckMarxProjectRepository;
import com.capitalone.dashboard.repository.CheckMarxRepository;

public class CheckMarxCollectorController extends CodeSecurityCollectorController<CheckMarxCollector, CheckMarxProject>{

    private CheckMarxProjectRepository projectRepository;
    private CheckMarxRepository dataRepository;
    private DefaultCheckMarxClient client;

    public CheckMarxCollectorController(CheckMarxProjectRepository projectRepository,
                                        CheckMarxRepository dataRepository,
                                        DefaultCheckMarxClient client) {
        this.projectRepository = projectRepository;
        this.dataRepository = dataRepository;
        this.client = client;
    }

    @Override
    protected void saveProjectToProjectRepository(CheckMarxProject project) {
        projectRepository.save(project);
    }

    @Override
    protected CheckMarxProject getAMovedProject(CheckMarxProject lhs, CheckMarxProject rhs) {
        lhs.setProjectDate(rhs.getProjectDate());
        lhs.setProjectName(rhs.getProjectName());
        lhs.setInstanceUrl(rhs.getInstanceUrl());
        lhs.setProjectTimestamp(rhs.getProjectTimestamp());
        lhs.setDescription(rhs.getProjectName());
        return lhs;
    }

    @Override
    protected CheckMarxProject enabledProject(CheckMarxCollector collector, CheckMarxProject project) {
        return projectRepository.findCheckMarxProject(collector.getId(), project.getProjectId(), project.getProjectName(), project.getProjectTimestamp());
    }

    @Override
    protected void refreshCollectorItemId(CheckMarxProject currentProject, CheckMarxProject project) {
        CheckMarx checkMarx = dataRepository.findByCollectorItemIdAndTimestamp(currentProject.getId(), project.getProjectTimestamp());
        saveToDataRepository(checkMarx, project);
    }

    @Override
    protected CheckMarxProject getNewProject() {
        return new CheckMarxProject();
    }

    @Override
    protected void refreshCollectorData(CheckMarxProject project) {
        CheckMarx checkMarx = client.getCurrentMetrics(project);
        if (checkMarx != null && isNewData(project, checkMarx)) {
            saveToDataRepository(checkMarx, project);
        }
    }

    @Override
    protected CheckMarxProject getCurrentProjectFromProjectRepository(CheckMarxCollector collector) {
        return projectRepository.findCurrentProject(collector.getId(), true);
    }

    private void saveToDataRepository(CheckMarx checkMarx, CheckMarxProject project) {
        checkMarx.setCollectorItemId(project.getId());
        dataRepository.save(checkMarx);
    }

    private boolean isNewData(CheckMarxProject project, CheckMarx checkMarx) {
        return dataRepository.findByCollectorItemIdAndTimestamp(
                project.getId(), checkMarx.getTimestamp()) == null;
    }

}
