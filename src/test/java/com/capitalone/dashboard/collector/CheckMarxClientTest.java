package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.CheckMarxProject;
import com.capitalone.dashboard.model.CheckMarx;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class CheckMarxClientTest {
    @Mock
    private CheckMarxSettings settings;
    private DefaultCheckMarxClient checkMarxClient;

    private static final String SERVER = "checkmarx-tests/test.xml";
    private static final String EXPECTED_NAME = "test:2018-7-14";
    private static final String EXPECTED_ID = "1";
    private static final String CRON = "0 0/1 * * * *";

    private static final String LOW = "Low";
    private static final String MEDIUM = "Medium";
    private static final String HIGH = "High";
    private static final String TOTAL = "Total";

    @Before
    public void init() {
        settings = new CheckMarxSettings();
        settings.setCron(CRON);
        settings.setServer(getUrlToTestFile(SERVER));
        settings.setPassword("");
        settings.setUsername("");

        checkMarxClient = new DefaultCheckMarxClient(settings);
    }

    @Test
    public void getProjects() throws Exception {
        checkMarxClient.parseDocument(settings.getServer());
        CheckMarxProject project = checkMarxClient.getProject();
        CheckMarxProject expectedProject = getExpectedCheckMarxProject();
        assertEquals(project, expectedProject);
        assertTrue(project.equals(expectedProject));
    }

    @Test
    public void currentCheckMarxMetrics() throws Exception {
        checkMarxClient.parseDocument(settings.getServer());
        CheckMarxProject project = checkMarxClient.getProject();
        CheckMarx checkMarx = checkMarxClient.getCurrentMetrics(project);
        Map<String, Integer> metrics = checkMarx.getMetrics();
        assertEquals(547, metrics.get(LOW).intValue());
        assertEquals(6, metrics.get(MEDIUM).intValue());
        assertEquals(6, metrics.get(HIGH).intValue());
        assertEquals(559, metrics.get(TOTAL).intValue());
        assertEquals(1531530177000L, checkMarx.getTimestamp());
    }

    private String getUrlToTestFile(String server) {
        ClassLoader classLoader = getClass().getClassLoader();
        return classLoader.getResource(server).toString();
    }

    private CheckMarxProject getExpectedCheckMarxProject() {
        CheckMarxProject project = new CheckMarxProject();
        project.setProjectName(EXPECTED_NAME);
        project.setProjectId(EXPECTED_ID);
        project.setInstanceUrl(getUrlToTestFile(SERVER));
        return project;
    }
}
