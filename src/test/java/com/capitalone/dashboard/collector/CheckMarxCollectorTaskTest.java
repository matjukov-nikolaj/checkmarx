package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.*;
import com.capitalone.dashboard.repository.*;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.scheduling.TaskScheduler;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CheckMarxCollectorTaskTest extends CheckMarxTestUtils {

    private CheckMarxCollectorTask task;

    private TaskScheduler mockScheduler;
    private CheckMarxCollectorRepository mockCollectorRepository;
    private CheckMarxProjectRepository mockProjectRepository;
    private CheckMarxRepository mockRepository;
    private ComponentRepository mockComponentRepository;
    private DefaultCheckMarxClient client;

    private static final String SERVER = "checkmarx-tests/test.xml";
    private static final String CRON = "0 0/1 * * * *";

    @Before
    public void setup() {
        mockScheduler = mock(TaskScheduler.class);
        mockCollectorRepository = mock(CheckMarxCollectorRepository.class);
        mockProjectRepository = mock(CheckMarxProjectRepository.class);
        mockRepository = mock(CheckMarxRepository.class);
        mockComponentRepository = mock(ComponentRepository.class);

        CheckMarxSettings settings = new CheckMarxSettings();
        settings.setCron(CRON);
        settings.setServer(getUrlToTestFile(SERVER));
        settings.setUsername("");
        settings.setPassword("");

        client = new DefaultCheckMarxClient(settings);
        this.task = new CheckMarxCollectorTask(mockScheduler, mockCollectorRepository, mockProjectRepository,
                mockRepository, client, settings, mockComponentRepository);
    }

    @Test
    public void getCollectorReturnsCheckMarxCollector() {
        final CheckMarxCollector collector = task.getCollector();

        assertThat(collector).isNotNull().isInstanceOf(CheckMarxCollector.class);
        assertThat(collector.isEnabled()).isTrue();
        assertThat(collector.isOnline()).isTrue();
        assertThat(collector.getCheckMarxServer()).contains(getUrlToTestFile(SERVER));
        assertThat(collector.getCollectorType()).isEqualTo(CollectorType.CheckMarx);
        assertThat(collector.getName()).isEqualTo("CheckMarx");
        assertThat(collector.getAllFields().get("instanceUrl")).isEqualTo("");
        assertThat(collector.getAllFields().get("projectName")).isEqualTo("");
        assertThat(collector.getAllFields().get("projectId")).isEqualTo("");
        assertThat(collector.getUniqueFields().get("instanceUrl")).isEqualTo("");
        assertThat(collector.getUniqueFields().get("projectName")).isEqualTo("");
    }

    @Test
    public void getCollectorRepositoryReturnsTheRepository() {
        assertThat(task.getCollectorRepository()).isNotNull().isSameAs(mockCollectorRepository);
    }

    @Test
    public void getCron() {
        assertThat(task.getCron()).isNotNull().isSameAs(CRON);
    }


    @Test
    public void collectEmpty() {
        when(mockComponentRepository.findAll()).thenReturn(components());
        task.collect(new CheckMarxCollector());
        verifyZeroInteractions(mockRepository);
    }

    @Test
    public void collectWithServer() {
        when(mockComponentRepository.findAll()).thenReturn(components());
        CheckMarxCollector collector = collectorWithServer();
        task.collect(collector);
        CheckMarxProject project = client.getProject();
        CheckMarxProject expectedProject = getExpectedCheckMarxProject();
        assertEquals(project, expectedProject);
        assertTrue(project.equals(expectedProject));
        verify(mockProjectRepository).save(project);
        verify(mockProjectRepository).findCheckMarxProject(collector.getId(), project.getProjectId(), project.getProjectName());
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

    protected String getUrl(String server) {
        return getUrlToTestFile(server);
    }

    protected String getServer() {
        return SERVER;
    }

    private CheckMarxCollector collectorWithServer() {
        return CheckMarxCollector.prototype(getUrlToTestFile(SERVER));
    }
}