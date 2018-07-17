package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.CheckMarxCollector;
import com.capitalone.dashboard.model.Component;
import com.capitalone.dashboard.model.ConfigHistOperationType;
import com.capitalone.dashboard.repository.*;
import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.DefaultManagedTaskScheduler;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CheckMarxCollectorTaskTest {

    @InjectMocks private CheckMarxCollectorTask task;
    @Mock private CheckMarxCollectorRepository checkMarxCollectorRepository;
    @Mock private CheckMarxProjectRepository checkMarxProjectRepository;
    @Mock private CheckMarxRepository checkMarxRepository;

    @Mock private ComponentRepository dbComponentRepository;

    private static final String SERVER = "checkmarx-tests/test1.xml";
    private static final String CRON = "0 0/1 * * * *";

    @Test
    public void collectEmpty() throws Exception {
        when(dbComponentRepository.findAll()).thenReturn(components());
        task.collect(new CheckMarxCollector());
        verifyZeroInteractions(checkMarxRepository);
    }

//    @Test
//    public void collectWithServer() throws Exception {
//        when(dbComponentRepository.findAll()).thenReturn(components());
//        TaskScheduler taskScheduler = new DefaultManagedTaskScheduler();
//        CheckMarxSettings checkMarxSettings = new CheckMarxSettings();
//        checkMarxSettings.setCron(CRON);
//        checkMarxSettings.setServer(getUrlToTestFile());
//        DefaultCheckMarxClient checkMarxClient = new DefaultCheckMarxClient(checkMarxSettings);
//        task = new CheckMarxCollectorTask(taskScheduler, checkMarxCollectorRepository,
//                checkMarxProjectRepository, checkMarxRepository,
//                checkMarxClient, checkMarxSettings, dbComponentRepository);
//        task.collect(collectorWithServer());
        //assertThat(task.getCron(), is(CRON));
//    }

    private ArrayList<com.capitalone.dashboard.model.Component> components() {
        ArrayList<com.capitalone.dashboard.model.Component> cArray = new ArrayList<>();
        com.capitalone.dashboard.model.Component c = new Component();
        c.setId(new ObjectId());
        c.setName("COMPONENT1");
        c.setOwner("JOHN");
        cArray.add(c);
        return cArray;
    }

    private String getUrlToTestFile() {
        ClassLoader classLoader = getClass().getClassLoader();
        return classLoader.getResource(SERVER).toString();
    }

    private CheckMarxCollector collectorWithServer() {
        return CheckMarxCollector.prototype(getUrlToTestFile());
    }
}