package com.capitalone.dashboard.model;

import com.capitalone.dashboard.model.Collector;

public class CheckMarxCollector extends Collector {
    private String apiToken;

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }
}
