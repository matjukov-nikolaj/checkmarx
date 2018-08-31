package com.capitalone.dashboard.collector;

import codesecurity.collectors.collector.DefaultCodeSecurityClient;
import codesecurity.config.Constants;
import com.capitalone.dashboard.model.CheckMarxProject;
import com.capitalone.dashboard.model.CheckMarx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;
import java.util.*;

@Component("DefaultCheckMarxClient")
public class DefaultCheckMarxClient extends DefaultCodeSecurityClient<CheckMarx, CheckMarxProject> {
    private static final String PROJECT_ID = "ProjectId";
    private static final String PROJECT_NAME = "ProjectName";
    private static final String SCAN_START = "ScanStart";
    private static final String CHECK_MARX_XML_RESULTS_TAG = "CxXMLResults";
    private static final String RESULT_TAG = "Result";
    private static final String ATTRIBUTE_NAME = "Severity";
    private static final String DATE_FORMAT = "EEEE, MMMM d, yyyy h:mm:ss a";

    private CheckMarx checkMarx;
    private CheckMarxProject project;
    private Map<String, Integer> metrics = new HashMap<>();
    private CheckMarxSettings settings;

    @Autowired
    public DefaultCheckMarxClient(CheckMarxSettings settings) {
        this.settings = settings;
    }

    @Override
    public CheckMarxProject getProject() {
        return this.project;
    }

    @Override
    public CheckMarx getCurrentMetrics(CheckMarxProject project) {
        return this.checkMarx;
    }

    protected void setInstanceUrlInProject(String instanceUrl) {
        this.project.setInstanceUrl(instanceUrl);
    }

    protected void parseCodeSecurityDocument(Document document) throws Exception {
        parseProject(document);
        parseMetrics(document);
        setCheckMarxMetrics(document);
    }

    protected String getDateFormat() {
        return DATE_FORMAT;
    }

    protected String getUsernameFromSettings() {
        return this.settings.getUsername();
    }

    protected String getPasswordFromSettings() {
        return  this.settings.getPassword();
    }

    private void setCheckMarxMetrics(Document document) {
        this.metrics.put(Constants.TOTAL, getTotalIssues());
        this.checkMarx.setMetrics(metrics);
        this.checkMarx.setName(project.getProjectName());
        this.checkMarx.setUrl(project.getInstanceUrl());
        this.checkMarx.setTimestamp(getTimeStamp(getScanStart(document)));
    }

    private Integer getTotalIssues() {
        return this.metrics.get(Constants.CheckMarx.LOW)
                + this.metrics.get(Constants.CheckMarx.MEDIUM)
                + this.metrics.get(Constants.CheckMarx.HIGH);
    }

    private void parseProject(Document document) {
        NodeList cxXMLResultsTag = document.getElementsByTagName(CHECK_MARX_XML_RESULTS_TAG);
        this.project.setProjectId(getProjectId(cxXMLResultsTag));
        String name = getNodeAttributeValue(cxXMLResultsTag, PROJECT_NAME);
        this.project.setProjectName(name);
        this.project.setProjectDate(getProjectDate(getScanStart(document)));
        this.project.setProjectTimestamp(getTimeStamp(getScanStart(document)));
    }

    public void setSettings(CheckMarxSettings settings) {
        this.settings = settings;
    }

    private void parseMetrics(Document document) {
        NodeList nodesWithResults = document.getElementsByTagName(RESULT_TAG);
        findScanRiskLevels(nodesWithResults);
    }

    private void findScanRiskLevels(NodeList nodes) {
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);
            NamedNodeMap nodeMap = node.getAttributes();
            Node nodeInMap = nodeMap.getNamedItem(ATTRIBUTE_NAME);
            String nodeName = nodeInMap.getNodeValue();
            updateScanRiskLevel(nodeName);
        }
    }

    private void updateScanRiskLevel(String name) {
        switch (name) {
            case Constants.CheckMarx.HIGH:
                incrementMetric(Constants.CheckMarx.HIGH);
                break;
            case Constants.CheckMarx.MEDIUM:
                incrementMetric(Constants.CheckMarx.MEDIUM);
                break;
            case Constants.CheckMarx.LOW:
                incrementMetric(Constants.CheckMarx.LOW);
                break;
            default:
                break;
        }
    }

    private void incrementMetric(String name) {
        this.metrics.put(name, this.metrics.get(name) + 1);
    }

    protected void initializationFields() {
        this.checkMarx = new CheckMarx();
        this.project = new CheckMarxProject();
        this.metrics.put(Constants.CheckMarx.LOW, 0);
        this.metrics.put(Constants.CheckMarx.MEDIUM, 0);
        this.metrics.put(Constants.CheckMarx.HIGH, 0);
        this.metrics.put(Constants.TOTAL, 0);
    }

    private String getProjectId(NodeList cxXMLResultsTag) {
        return getNodeAttributeValue(cxXMLResultsTag, PROJECT_ID);
    }

    private String getScanStart(Document document) {
        NodeList cxXMLResultsTag = document.getElementsByTagName(CHECK_MARX_XML_RESULTS_TAG);
        return getNodeAttributeValue(cxXMLResultsTag, SCAN_START);
    }

    private String getNodeAttributeValue(NodeList cxXMLResultsTag, String itemName) {
        Node node = cxXMLResultsTag.item(0);
        return node.getAttributes().getNamedItem(itemName).getNodeValue();
    }
}
