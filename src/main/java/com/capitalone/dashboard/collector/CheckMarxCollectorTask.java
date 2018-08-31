package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.*;
import com.capitalone.dashboard.repository.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class CheckMarxCollectorTask extends CollectorTask<CheckMarxCollector> {
    @SuppressWarnings({"PMD.UnusedPrivateField", "unused"})
    private static final Log LOG = LogFactory.getLog(CheckMarxCollectorTask.class);

    private final CheckMarxCollectorRepository checkMarxCollectorRepository;
    private final CheckMarxProjectRepository checkMarxProjectRepository;
    private final DefaultCheckMarxClient checkMarxClient;
    private final CheckMarxSettings checkMarxSettings;

    private final CheckMarxCollectorController collectorController;

    @Autowired
    public CheckMarxCollectorTask(TaskScheduler taskScheduler,
                                  CheckMarxCollectorRepository checkMarxCollectorRepository,
                                  CheckMarxProjectRepository checkMarxProjectRepository,
                                  CheckMarxRepository checkMarxRepository,
                                  DefaultCheckMarxClient checkMarxClient,
                                  CheckMarxSettings checkMarxSettings) {
        super(taskScheduler, "CheckMarx");
        this.checkMarxCollectorRepository = checkMarxCollectorRepository;
        this.checkMarxProjectRepository = checkMarxProjectRepository;
        this.checkMarxClient = checkMarxClient;
        this.checkMarxSettings = checkMarxSettings;

        this.collectorController = new CheckMarxCollectorController(checkMarxProjectRepository, checkMarxRepository, checkMarxClient);
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
        String instanceUrl = collector.getCheckMarxServer();

        checkMarxClient.parseDocument(instanceUrl);
        CheckMarxProject project = checkMarxClient.getProject();
        logBanner("Fetched project: " + project.getProjectName() + ":" + project.getProjectDate());
        if (this.collectorController.isNewProject(project, existingProjects)) {
            this.collectorController.addNewProject(project, collector, existingProjects);
        }
    }
}
