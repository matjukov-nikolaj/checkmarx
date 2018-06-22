package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.CheckMarxCollector;

import com.capitalone.dashboard.model.CodeQuality;
import com.capitalone.dashboard.model.CollectorItemConfigHistory;
import com.capitalone.dashboard.model.CollectorItem;
import com.capitalone.dashboard.model.CollectorType;
import com.capitalone.dashboard.model.ConfigHistOperationType;
import com.capitalone.dashboard.model.CheckMarxCollector;
import com.capitalone.dashboard.model.CheckMarxProject;
import com.capitalone.dashboard.repository.BaseCollectorRepository;
import com.capitalone.dashboard.repository.CodeQualityRepository;
import com.capitalone.dashboard.repository.ComponentRepository;
import com.capitalone.dashboard.repository.CheckMarxCollectorRepository;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CheckMarxCollectorTask extends CollectorTask<CheckMarxCollector> {

    private final CheckMarxCollectorRepository checkMarxCollectorRepository;

    @Value("${cron}") // Injected from application.properties
    private String cron;

    @Value("${apiToken}") // Injected from application.properties
    private String apiToken;

    @Autowired
    public CheckMarxCollectorTask(TaskScheduler taskScheduler,
                                  CheckMarxCollectorRepository checkMarxCollectorRepository) {
        super(taskScheduler, "CheckMarx");
        this.checkMarxCollectorRepository = checkMarxCollectorRepository;
    }

    @Override
    public CheckMarxCollector getCollector() {

        CheckMarxCollector collector = new CheckMarxCollector();

        collector.setName("CheckMarx"); // Must be unique to all collectors for a given Dashboard Application instance
  //      collector.setCollectorType(CollectorType.Feature);
        collector.setEnabled(true);
        collector.setApiToken(apiToken);

        return collector;
    }

    @Override
    public BaseCollectorRepository<CheckMarxCollector> getCollectorRepository() {
        return checkMarxCollectorRepository;
    }

    @Override
    public String getCron() {
        return cron;
    }

    @Override
    public void collect(CheckMarxCollector collector) {

        // Collector logic
//        PivotalTrackerApi api = new PivotalTrackerApi(collector.getApiToken());
//
//        for (Project project : api.getProjects()) {
//
//            PivotalTrackerCollectorItem collectorItem = getOrCreateCollectorItems(project.getProjectId());
//
//            // Naive implementation
//            deleteFeaturesFor(collectorItem);
//
//            addFeaturesFor(collectorItem, project.getStories());
//        }
    }
}