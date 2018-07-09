package com.capitalone.dashboard.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckMarxCollector extends Collector {
    private List<String> checkMarxServers = new ArrayList<>();

    public List<String> getCheckMarxServers() {
        return checkMarxServers;
    }

    public static CheckMarxCollector prototype(List<String> servers) {
        CheckMarxCollector protoType = new CheckMarxCollector();
        protoType.setName("CheckMarx");
        protoType.setCollectorType(CollectorType.CodeSecurity);
        protoType.setOnline(true);
        protoType.setEnabled(true);

        if(servers!=null) {
            protoType.getCheckMarxServers().addAll(servers);
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
