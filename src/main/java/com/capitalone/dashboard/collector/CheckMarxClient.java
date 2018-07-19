package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.CheckMarx;
import com.capitalone.dashboard.model.CheckMarxProject;

public interface CheckMarxClient {
    CheckMarxProject getProject();
    CheckMarx getCurrentMetrics(CheckMarxProject project);
    void parseDocument(String instanceUrl);
}
