package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.CheckMarxProject;

public abstract  class CheckMarxTestUtils {

    private static final String EXPECTED_NAME = "test";
    private static final String EXPECTED_ID = "1";
    private static final String EXPECTED_DATE = "2018-7-14";
    private static final Long EXPECTED_TIMESTAMP = 1531530177000L;


    protected String getUrlToTestFile(String server) {
        ClassLoader classLoader = getClass().getClassLoader();
        return classLoader.getResource(server).toString();
    }

    protected CheckMarxProject getExpectedCheckMarxProject() {
        CheckMarxProject project = new CheckMarxProject();
        project.setProjectName(EXPECTED_NAME);
        project.setProjectId(EXPECTED_ID);
        project.setProjectDate(EXPECTED_DATE);
        project.setProjectTimestamp(EXPECTED_TIMESTAMP);
        project.setInstanceUrl(getUrl(getServer()));
        return project;
    }

    protected abstract String getUrl(String server);

    protected abstract String getServer();

}
