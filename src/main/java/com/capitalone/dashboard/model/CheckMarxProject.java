package com.capitalone.dashboard.model;

import codesecurity.collectors.model.CodeSecurityProject;

public class CheckMarxProject extends CodeSecurityProject {

    public static final String PROJECT_ID = "projectId";
    public static final String PROJECT_TIMESTAMP = "projectTimestamp";

    public String getProjectId() { return (String) getOptions().get(PROJECT_ID); }

    public void setProjectId(String id) { getOptions().put(PROJECT_ID, id); }

    public Long getProjectTimestamp() { return (Long) getOptions().get(PROJECT_TIMESTAMP); }

    public void setProjectTimestamp(Long timestamp) { getOptions().put(PROJECT_TIMESTAMP, timestamp); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CheckMarxProject that = (CheckMarxProject) o;
        return getProjectId().equals(that.getProjectId())
                && getInstanceUrl().equals(that.getInstanceUrl())
                && getProjectName().equals(that.getProjectName())
                && getProjectDate().equals(that.getProjectDate())
                && getProjectTimestamp().equals(that.getProjectTimestamp());
    }

    @Override
    public int hashCode() {
        int result = getInstanceUrl().hashCode();
        result = 31 * result + getProjectId().hashCode();
        return result;
    }

}
