package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.CheckMarx;
import com.capitalone.dashboard.model.CheckMarxProject;

import java.util.List;

public interface CheckMarxClient {
    CheckMarxProject getProject(String instanceUrl);
    CheckMarx currentCheckMarxMetrics(CheckMarxProject project);
}
