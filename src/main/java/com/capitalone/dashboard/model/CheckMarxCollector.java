package com.capitalone.dashboard.model;

import java.util.HashMap;
import java.util.Map;

public class CheckMarxCollector extends Collector {
    private String checkMarxServer = "";
    public String getCheckMarxServer() {
        return checkMarxServer;
    }

    public static CheckMarxCollector prototype(String server) {
        CheckMarxCollector protoType = new CheckMarxCollector();
        protoType.setName("CheckMarx");
        protoType.setCollectorType(CollectorType.CheckMarx);
        protoType.setOnline(true);
        protoType.setEnabled(true);

        if(server!=null) {
            protoType.checkMarxServer = server;
        }

        Map<String, Object> allOptions = new HashMap<>();
        allOptions.put(CheckMarxProject.INSTANCE_URL,"");
        allOptions.put(CheckMarxProject.PROJECT_NAME,"");
        allOptions.put(CheckMarxProject.PROJECT_ID, "");
        protoType.setAllFields(allOptions);
        Map<String, Object> uniqueOptions = new HashMap<>();
        uniqueOptions.put(CheckMarxProject.INSTANCE_URL,"");
        uniqueOptions.put(CheckMarxProject.PROJECT_NAME,"");
        protoType.setUniqueFields(uniqueOptions);
        return protoType;
    }
}
