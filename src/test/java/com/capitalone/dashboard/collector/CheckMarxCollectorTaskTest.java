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
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CheckMarxCollectorTaskTest {

    @InjectMocks private CheckMarxCollectorTask task;
    @Mock private CheckMarxCollectorRepository checkMarxCollectorRepository;
    @Mock private CheckMarxProjectRepository checkMarxProjectRepository;
    @Mock private CheckMarxRepository checkMarxRepository;

    @Mock private CheckMarxSettings settings;
    @Mock private ComponentRepository dbComponentRepository;
    @Mock private DefaultCheckMarxClient checkMarxClient;

    private static final String SERVER = "http://192.168.103.97:8081/checkmarx-tests/test1.xml";
    private static final String CRON = "0 0/1 * * * *";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";


    @Before
    public void init(){
        settings = new CheckMarxSettings();
        settings.setCron(CRON);
        settings.setServer(SERVER);
        settings.setUsername(USERNAME);
        settings.setPassword(PASSWORD);
        checkMarxClient = new DefaultCheckMarxClient(settings);
//        task = new CheckMarxCollectorTask(TaskScheduler, checkMarxCollectorRepository,
//                checkMarxProjectRepository, checkMarxRepository,
//                checkMarxClient, settings, dbComponentRepository);
    }

    @Test
    public void collectEmpty() throws Exception {
        when(dbComponentRepository.findAll()).thenReturn(components());
        task.collect(new CheckMarxCollector());
        verifyZeroInteractions(checkMarxRepository);
    }

    @Test
    public void collectWithServer() throws Exception {
        when(dbComponentRepository.findAll()).thenReturn(components());
        //task.collect(collectorWithServer());
        //assertThat(task.getCron(), is(CRON));
    }

//    @Test
//    public void collectOneServer54() throws Exception {
//        when(dbComponentRepository.findAll()).thenReturn(components());
//        when(sonarClientSelector.getSonarClient(VERSION54)).thenReturn(defaultSonar6Client);
//        task.collect(collectorWithOneServer(VERSION54));
//        verify(sonarClientSelector).getSonarClient(VERSION54);
//        verify(defaultSonar6Client).getQualityProfiles(SERVER1);
//        verify(defaultSonar6Client).retrieveProfileAndProjectAssociation(SERVER1, QUALITYPROFILE);
//        verify(defaultSonar6Client).getQualityProfileConfigurationChanges(SERVER1, QUALITYPROFILE);
//    }
//
//
//    @Test
//    public void collectOneServer63() throws Exception {
//        when(dbComponentRepository.findAll()).thenReturn(components());
//        when(sonarClientSelector.getSonarClient(VERSION63)).thenReturn(defaultSonar6Client);
//        task.collect(collectorWithOneServer(VERSION63));
//        verify(sonarClientSelector).getSonarClient(VERSION63);
//        verify(defaultSonar6Client).getQualityProfiles(SERVER1);
//        verify(defaultSonar6Client).retrieveProfileAndProjectAssociation(SERVER1, QUALITYPROFILE);
//        verify(defaultSonar6Client).getQualityProfileConfigurationChanges(SERVER1, QUALITYPROFILE);
//    }
//
//
//    @Test
//    public void collectTwoServer43And54() throws Exception {
//        when(dbComponentRepository.findAll()).thenReturn(components());
//        when(sonarClientSelector.getSonarClient(VERSION54)).thenReturn(defaultSonar6Client);
//        when(sonarClientSelector.getSonarClient(VERSION43)).thenReturn(defaultSonarClient);
//        task.collect(collectorWithOnTwoServers(VERSION43, VERSION54));
//        verify(sonarClientSelector).getSonarClient(VERSION43);
//        verify(sonarClientSelector).getSonarClient(VERSION54);
//
//        verify(defaultSonar6Client).getQualityProfiles(SERVER2);
//        verify(defaultSonar6Client).retrieveProfileAndProjectAssociation(SERVER2, QUALITYPROFILE);
//        verify(defaultSonar6Client).getQualityProfileConfigurationChanges(SERVER2, QUALITYPROFILE);
//
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

    private String getCron()
    {
        return CRON;
    }

    private CheckMarxCollector collectorWithServer() {
        return CheckMarxCollector.prototype(SERVER);
    }
}