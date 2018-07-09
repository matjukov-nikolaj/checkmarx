package com.capitalone.dashboard.collector;


import com.capitalone.dashboard.model.CheckMarxProject;
import com.capitalone.dashboard.model.CheckMarxCollector;
import com.capitalone.dashboard.model.CodeSecurity;
import com.capitalone.dashboard.collector.DefaultCheckMarxClient;
import com.capitalone.dashboard.model.CollectorItem;
import com.capitalone.dashboard.model.CollectorType;
import com.capitalone.dashboard.repository.*;
import com.capitalone.dashboard.repository.ComponentRepository;

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
    private final CheckMarxProfileRepostory checkMarxProfileRepostory;
    private final CodeSecurityRepository codeSecurityRepository;
    private final DefaultCheckMarxClient checkMarxClient;
    private final CheckMarxSettings checkMarxSettings;
    private final ComponentRepository dbComponentRepository;

    @Autowired
    public CheckMarxCollectorTask(TaskScheduler taskScheduler,
                                  CheckMarxCollectorRepository checkMarxCollectorRepository,
                                  CheckMarxProjectRepository checkMarxProjectRepository,
                                  CheckMarxProfileRepostory checkMarxProfileRepostory,
                                  CodeSecurityRepository codeSecurityRepository,
                                  DefaultCheckMarxClient checkMarxClient,
                                  CheckMarxSettings checkMarxSettings,
                                  ComponentRepository dbComponentRepository) {
        super(taskScheduler, "CheckMarx");
        this.checkMarxCollectorRepository = checkMarxCollectorRepository;
        this.checkMarxProjectRepository = checkMarxProjectRepository;
        this.checkMarxProfileRepostory = checkMarxProfileRepostory;
        this.codeSecurityRepository = codeSecurityRepository;
        this.checkMarxClient = checkMarxClient;
        this.checkMarxSettings = checkMarxSettings;
        this.dbComponentRepository = dbComponentRepository;
    }

    @Override
    public CheckMarxCollector getCollector() {
        return CheckMarxCollector.prototype(checkMarxSettings.getServers());
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
        Set<ObjectId> udId = new HashSet<>();
        udId.add(collector.getId());
        List<CheckMarxProject> existingProjects = checkMarxProjectRepository.findByCollectorIdIn(udId);
        List<CheckMarxProject> latestProjects = new ArrayList<>();
        clean(collector, existingProjects);
        if (!CollectionUtils.isEmpty(collector.getCheckMarxServers())) {
            for (int i = 0; i < collector.getCheckMarxServers().size(); ++i) {
                String instanceUrl = collector.getCheckMarxServers().get(i);
                logBanner(instanceUrl);

                List<CheckMarxProject> projects = checkMarxClient.getProjects(instanceUrl);
                latestProjects.addAll(projects);
                log("Fetched projects   " + projects.size());
                addNewProjects(projects, existingProjects, collector);

                refreshData(enabledProjects(collector, projects));
                log("Finished");
            }
        }
    }

    private void addNewProjects(List<CheckMarxProject> projects, List<CheckMarxProject> existingProjects, CheckMarxCollector collector) {
        int count = 0;
        List<CheckMarxProject> newProjects = new ArrayList<>();
        for (CheckMarxProject project : projects) {
            if (!existingProjects.contains(project)) {
                project.setCollectorId(collector.getId());
                project.setEnabled(false);
                project.setDescription(project.getProjectName());
                newProjects.add(project);
                count++;
            }
        }
        if (!CollectionUtils.isEmpty(newProjects)) {
            checkMarxProjectRepository.save(newProjects);
        }
        log("New projects       " + count);
    }

    private void refreshData(List<CheckMarxProject> checkMarxProjects) {
        int count = 0;
        for (CheckMarxProject project : checkMarxProjects) {
            CodeSecurity codeSecurity = checkMarxClient.currentCodeSecurity(project);
            if (codeSecurity != null && isNewSecurityData(project, codeSecurity)) {
                codeSecurity.setCollectorItemId(project.getId());
                codeSecurityRepository.save(codeSecurity);
                count++;
            }
        }
        log("Updated            " + count);
    }

    private List<CheckMarxProject> enabledProjects(CheckMarxCollector collector, List<CheckMarxProject> projects) {
        List<CheckMarxProject> enabledProjects = new ArrayList<>();
        CheckMarxProject checkMarxProject;
        for (CheckMarxProject project : projects) {
            checkMarxProject = checkMarxProjectRepository.findCheckMarxProject(collector.getId(), project.getInstanceUrl(), project.getProjectId());
            if (!checkMarxProject.equals(null)) {
                enabledProjects.add(checkMarxProject);
            }
        }
        return enabledProjects.equals(null) ? projects : enabledProjects;
    }

    private boolean isNewSecurityData(CheckMarxProject project, CodeSecurity codeSecurity) {
        CodeSecurity kek = codeSecurityRepository.findByCollectorItemIdAndTimestamp(
                project.getId(), codeSecurity.getTimestamp());
        return kek == null;
    }

    /**
     * Clean up unused sonar collector items
     *
     * @param collector the {@link CheckMarxCollector}
     */

    @SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts") // agreed PMD, fixme
    private void clean(CheckMarxCollector collector, List<CheckMarxProject> existingProjects) {
        Set<ObjectId> uniqueIDs = new HashSet<>();
        for (com.capitalone.dashboard.model.Component comp : dbComponentRepository.findAll()) {
            if (comp.getCollectorItems() != null && !comp.getCollectorItems().isEmpty()) {
                List<CollectorItem> itemList = comp.getCollectorItems().get(CollectorType.CodeSecurity);
                if (itemList != null) {
                    for (CollectorItem ci : itemList) {
                        if (ci != null && ci.getCollectorId().equals(collector.getId())) {
                            uniqueIDs.add(ci.getId());
                        }
                    }
                }
            }
        }
        List<CheckMarxProject> stateChangeJobList = new ArrayList<>();
        Set<ObjectId> udId = new HashSet<>();
        udId.add(collector.getId());
        for (CheckMarxProject job : existingProjects) {
            // collect the jobs that need to change state : enabled vs disabled.
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
}
