package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.CheckMarx;
import com.capitalone.dashboard.model.CheckMarxProject;
import com.capitalone.dashboard.model.CheckMarxCollector;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;
import static com.sun.org.apache.xerces.internal.util.PropertyState.is;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class CheckMarxClientTest {
    @Mock
    private CheckMarxSettings settings;
    private DefaultCheckMarxClient checkMarxClient;

    private static final String SERVER = "http://192.168.103.97:8081/checkmarx-tests/test.xml";
    private static final String EXPECTED_URL = "http://admin:admin@192.168.103.97:8081/checkmarx-tests/test.xml";
    private static final String EXPECTED_NAME = "test:2018-7-14";
    private static final String EXPECTED_ID = "1";
    private static final String CRON = "0 0/1 * * * *";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    private static final String LOW = "Low";
    private static final String MEDIUM = "Medium";
    private static final String HIGH = "High";
    private static final String TOTAL = "Total";




    @Before
    public void init() {
        settings = new CheckMarxSettings();
        settings.setCron(CRON);
        settings.setServer(SERVER);
        settings.setUsername(USERNAME);
        settings.setPassword(PASSWORD);

        checkMarxClient = new DefaultCheckMarxClient(settings);
    }

    @Test
    public void getProjects() throws Exception {
        CheckMarxProject project = checkMarxClient.getProject(settings.getServer());
        assertEquals(project, getExpectedCheckMarxProject());
    }

    @Test
    public void currentCheckMarxMetrics() throws Exception {
        CheckMarxProject project = checkMarxClient.getProject(settings.getServer());
        CheckMarx checkMarx = checkMarxClient.currentCheckMarxMetrics(project);
        Map<String, Integer> metrics = checkMarx.getMetrics();
        assertEquals(547, metrics.get(LOW).intValue());
        assertEquals(6, metrics.get(MEDIUM).intValue());
        assertEquals(6, metrics.get(HIGH).intValue());
        assertEquals(559, metrics.get(TOTAL).intValue());
        assertEquals(1531530177000L, checkMarx.getTimestamp());
    }

    private CheckMarxProject getExpectedCheckMarxProject() {
        CheckMarxProject project = new CheckMarxProject();
        project.setProjectName(EXPECTED_NAME);
        project.setProjectId(EXPECTED_ID);
        project.setInstanceUrl(EXPECTED_URL);
        return project;
    }
}
