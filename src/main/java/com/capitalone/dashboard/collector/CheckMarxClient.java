package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.CheckMarxProject;

import com.capitalone.dashboard.model.CodeSecurity;

import java.util.List;

public interface CheckMarxClient {

    List<CheckMarxProject> getProjects(String instanceUrl);
    CodeSecurity currentCodeSecurity(CheckMarxProject project);

}
