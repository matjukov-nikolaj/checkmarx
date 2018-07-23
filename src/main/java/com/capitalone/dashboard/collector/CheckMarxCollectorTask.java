package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.*;
import com.capitalone.dashboard.model.CheckMarx;
import com.capitalone.dashboard.repository.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class CheckMarxCollectorTask extends CollectorTask<CheckMarxCollector> {
    @SuppressWarnings({"PMD.UnusedPrivateField", "unused"})
    private static final Log LOG = LogFactory.getLog(CheckMarxCollectorTask.class);

    private final CheckMarxCollectorRepository checkMarxCollectorRepository;
    private final CheckMarxProjectRepository checkMarxProjectRepository;
    private final CheckMarxRepository checkMarxRepository;
    private final DefaultCheckMarxClient checkMarxClient;
    private final CheckMarxSettings checkMarxSettings;
    private final ComponentRepository dbComponentRepository;

    @Autowired
    public CheckMarxCollectorTask(TaskScheduler taskScheduler,
                                  CheckMarxCollectorRepository checkMarxCollectorRepository,
                                  CheckMarxProjectRepository checkMarxProjectRepository,
                                  CheckMarxRepository checkMarxRepository,
                                  DefaultCheckMarxClient checkMarxClient,
                                  CheckMarxSettings checkMarxSettings,
                                  ComponentRepository dbComponentRepository) {
        super(taskScheduler, "CheckMarx");
        this.checkMarxCollectorRepository = checkMarxCollectorRepository;
        this.checkMarxProjectRepository = checkMarxProjectRepository;
        this.checkMarxRepository = checkMarxRepository;
        this.checkMarxClient = checkMarxClient;
        this.checkMarxSettings = checkMarxSettings;
        this.dbComponentRepository = dbComponentRepository;
    }

    @Override
    public CheckMarxCollector getCollector() {
        return CheckMarxCollector.prototype(checkMarxSettings.getServer());
    }

    @Override
    public BaseCollectorRepository<CheckMarxCollector> getCollectorRepository() {
        return checkMarxCollectorRepository;
    }

    @Override
    public String getCron() {
        return checkMarxSettings.getCron();
    }

    @Override
    public void collect(CheckMarxCollector collector) {
        if (collector.getCheckMarxServer().isEmpty()) {
            return;
        }
        Set<ObjectId> udId = new HashSet<>();
        udId.add(collector.getId());
        List<CheckMarxProject> existingProjects = checkMarxProjectRepository.findByCollectorIdIn(udId);
        clean(collector, existingProjects);

        String instanceUrl = collector.getCheckMarxServer();
        checkMarxClient.parseDocument(instanceUrl);
        CheckMarxProject project = checkMarxClient.getProject();
        logBanner("Fetched project: " + project.getProjectName());
        if (isNewProject(project, existingProjects)) {
            addNewProject(project, collector);
        }
        refreshData(enabledProject(collector, project));
    }

    private boolean isNewProject(CheckMarxProject project, List<CheckMarxProject> existingProjects) {
        return (!existingProjects.contains(project));
    }

    private void addNewProject(CheckMarxProject project, CheckMarxCollector collector) {
        project.setCollectorId(collector.getId());
        project.setEnabled(false);
        project.setDescription(project.getProjectName());
        checkMarxProjectRepository.save(project);
    }

    private void refreshData(CheckMarxProject project) {
        CheckMarx checkMarx = checkMarxClient.getCurrentMetrics(project);
        if (checkMarx != null && isNewData(project, checkMarx)) {
            checkMarx.setCollectorItemId(project.getId());
            checkMarxRepository.save(checkMarx);
        }
    }

    private CheckMarxProject enabledProject(CheckMarxCollector collector, CheckMarxProject project) {
        return checkMarxProjectRepository.findCheckMarxProject(collector.getId(), project.getProjectId(), project.getProjectName());
    }

    private boolean isNewData(CheckMarxProject project, CheckMarx checkMarx) {
        return checkMarxRepository.findByCollectorItemIdAndTimestamp(
                project.getId(), checkMarx.getTimestamp()) == null;
    }

    /**
     * Clean up unused checkmarx collector items
     *
     * @param collector the {@link CheckMarxCollector}
     */

    @SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts")
    private void clean(CheckMarxCollector collector, List<CheckMarxProject> existingProjects) {
        Set<ObjectId> uniqueIDs = getUniqueIds(collector);
        List<CheckMarxProject> stateChangeJobList = new ArrayList<>();
        for (CheckMarxProject job : existingProjects) {
            if ((job.isEnabled() && !uniqueIDs.contains(job.getId())) ||  // if it was enabled but not on a dashboard
                    (!job.isEnabled() && uniqueIDs.contains(job.getId()))) { // OR it was disabled and now on a dashboard
                job.setEnabled(uniqueIDs.contains(job.getId()));
                stateChangeJobList.add(job);
            }
        }
        if (!CollectionUtils.isEmpty(stateChangeJobList)) {
            checkMarxProjectRepository.save(stateChangeJobList);
        }
    }

    private Set<ObjectId> getUniqueIds(CheckMarxCollector collector) {
        Set<ObjectId> uniqueIDs = new HashSet<>();
        for (com.capitalone.dashboard.model.Component comp : dbComponentRepository
                .findAll()) {
            if (comp.getCollectorItems().isEmpty()) continue;
            List<CollectorItem> itemList = comp.getCollectorItems().get(CollectorType.CheckMarx);
            if (CollectionUtils.isEmpty(itemList)) continue;

            for (CollectorItem ci : itemList) {
                if (collector.getId().equals(ci.getCollectorId())) {
                    uniqueIDs.add(ci.getId());
                }
            }
        }
        return uniqueIDs;
    }
}
