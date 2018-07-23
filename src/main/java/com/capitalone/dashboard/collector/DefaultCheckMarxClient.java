package com.capitalone.dashboard.collector;

import codesecurity.collector.DefaultCodeSecurityClient;
import com.capitalone.dashboard.model.CheckMarxProject;
import com.capitalone.dashboard.model.CheckMarx;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;
import java.util.*;

@Component("DefaultCheckMarxClient")
public class DefaultCheckMarxClient extends DefaultCodeSecurityClient<CheckMarx, CheckMarxProject> {
    private static final Log LOG = LogFactory.getLog(DefaultCheckMarxClient.class);

    private static final String PROJECT_ID = "ProjectId";
    private static final String PROJECT_NAME = "ProjectName";
    private static final String SCAN_START = "ScanStart";
    private static final String CHECK_MARX_XML_RESULTS_TAG = "CxXMLResults";
    private static final String RESULT_TAG = "Result";
    private static final String ATTRIBUTE_NAME = "Severity";
    private static final String DATE_FORMAT = "EEEE, MMMM d, yyyy h:mm:ss a";
    private static final String HIGH = "High";
    private static final String MEDIUM = "Medium";
    private static final String LOW = "Low";
    private static final String TOTAL = "Total";

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

    protected void parseCodeSecurityDocument(Document document) {
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
        this.metrics.put(TOTAL, getTotalIssues());
        this.checkMarx.setMetrics(metrics);
        this.checkMarx.setName(project.getProjectName());
        this.checkMarx.setUrl(project.getInstanceUrl());
        this.checkMarx.setTimestamp(getTimeStamp(getScanStart(document)));
    }

    private Integer getTotalIssues() {
        return this.metrics.get(LOW) + this.metrics.get(MEDIUM) + this.metrics.get(HIGH);
    }

    private void parseProject(Document document) {
        NodeList cxXMLResultsTag = document.getElementsByTagName(CHECK_MARX_XML_RESULTS_TAG);
        this.project.setProjectId(getProjectId(cxXMLResultsTag));
        String name = getNodeAttributeValue(cxXMLResultsTag, PROJECT_NAME);
        this.project.setProjectName(getProjectName(name, getScanStart(document)));
    }

    public void setSettings(CheckMarxSettings settings) {
        this.settings = settings;
    }

    private void parseMetrics(Document document) {
        try {
            NodeList nodesWithResults = document.getElementsByTagName(RESULT_TAG);
            findScanRiskLevels(nodesWithResults);
        } catch (Exception e) {
            LOG.error(e);
        }
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
            case HIGH:
                incrementMetric(HIGH);
                break;
            case MEDIUM:
                incrementMetric(MEDIUM);
                break;
            case LOW:
                incrementMetric(LOW);
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
        this.metrics.put(LOW, 0);
        this.metrics.put(MEDIUM, 0);
        this.metrics.put(HIGH, 0);
        this.metrics.put(TOTAL, 0);
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
