package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.Component;
import com.capitalone.dashboard.model.ConfigHistOperationType;
import com.capitalone.dashboard.model.CheckMarxCollector;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CheckMarxCollectorTaskTest {
    @InjectMocks private CheckMarxCollectorTask task;
    @Mock private CheckMarxCollectorRepository checkMarxCollectorRepository;
    @Mock private CheckMarxProjectRepository checkMarxProjectRepository;
    @Mock private CodeSecurityRepository codeSecurityRepository;
    @Mock private CheckMarxProfileRepostory checkMarxProfileRepostory;


    @Mock private CheckMarxSettings checkMarxSettings;
    @Mock private ComponentRepository dbComponentRepository;
    @Mock private DefaultCheckMarxClient defaultCheckMarxClient;

    @Test
    public void collectEmpty() throws Exception {
        when(dbComponentRepository.findAll()).thenReturn(components());
        task.collect(new CheckMarxCollector());
        verifyZeroInteractions(defaultCheckMarxClient, codeSecurityRepository);
    }

    private ArrayList<com.capitalone.dashboard.model.Component> components() {
        ArrayList<com.capitalone.dashboard.model.Component> cArray = new ArrayList<>();
        com.capitalone.dashboard.model.Component c = new Component();
        c.setId(new ObjectId());
        c.setName("COMPONENT1");
        c.setOwner("JOHN");
        cArray.add(c);
        return cArray;
    }





}
