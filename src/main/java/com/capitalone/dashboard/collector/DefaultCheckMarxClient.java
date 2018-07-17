package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.CheckMarx;
import com.capitalone.dashboard.model.CheckMarxProject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;

import java.text.SimpleDateFormat;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.util.*;

@Component("DefaultCheckMarxClient")
public class DefaultCheckMarxClient implements CheckMarxClient {
    private static final Log LOG = LogFactory.getLog(DefaultCheckMarxClient.class);

    private static final String PROJECT_ID = "ProjectId";
    private static final String PROJECT_NAME = "ProjectName";
    private static final String SCAN_START = "ScanStart";
    private static final String PROTOCOL_SPLITTER = "://";
    private static final String CHECK_MARX_XML_RESULTS_TAG = "CxXMLResults";
    private static final String RESULT_TAG = "Result";
    private static final String ATTRIBUTE_NAME = "Severity";
    private static final String DATE_FORMAT = "EEEE, MMMM d, yyyy h:mm:ss a";
    private static final String HIGH = "High";
    private static final String MEDIUM = "Medium";
    private static final String LOW = "Low";
    private static final String TOTAL = "Total";

    private Map<String, Integer> metrics = new HashMap<>();
    private CheckMarxSettings settings;

    @Autowired
    public DefaultCheckMarxClient(CheckMarxSettings settings) {
        this.settings = settings;
        this.initializationMetrics();
    }

    @Override
    public CheckMarxProject getProject(String instanceUrl) {
        CheckMarxProject project = new CheckMarxProject();
        try {
            instanceUrl = getUrlWithUserData(instanceUrl);
            Document document = getDocument(instanceUrl);
            if (document != null) {
                NodeList cxXMLResultsTag = document.getElementsByTagName(CHECK_MARX_XML_RESULTS_TAG);
                project.setInstanceUrl(instanceUrl);
                project.setProjectId(getProjectId(cxXMLResultsTag));
                project.setProjectName(getProjectName(cxXMLResultsTag, getScanStart(document)));
            }
        } catch (Exception e) {
            LOG.error(e);
        }
        return project;
    }

    @Override
    public CheckMarx currentCheckMarxMetrics(CheckMarxProject project) {
        try {
            initializationMetrics();
            System.out.println(project.getInstanceUrl());
            Document document = getDocument(project.getInstanceUrl());
            parseDocument(document);
            CheckMarx checkMarx = new CheckMarx();
            this.metrics.put(TOTAL, this.metrics.get(LOW) + this.metrics.get(MEDIUM) + this.metrics.get(HIGH));
            checkMarx.setMetrics(metrics);
            checkMarx.setName(project.getProjectName());
            checkMarx.setUrl(project.getInstanceUrl());
            checkMarx.setTimestamp(getTimeStamp(getScanStart(document)));
            return checkMarx;
        } catch (Exception e) {
            LOG.error(e);
        }
        return null;
    }

    public void setSettings(CheckMarxSettings settings) {
        this.settings = settings;
    }

    private void parseDocument(Document document) {
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

    private void initializationMetrics() {
        this.metrics.put(LOW, 0);
        this.metrics.put(MEDIUM, 0);
        this.metrics.put(HIGH, 0);
        this.metrics.put(TOTAL, 0);
    }

    private Document getDocument(String instanceUrl) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            URL url = new URL(instanceUrl);
            Document document = db.parse(url.openStream());
            document.getDocumentElement().normalize();
            return document;
        } catch (Exception e) {
            LOG.error("Could not parse document from: " + instanceUrl, e);
        }
        return null;
    }

    private long getTimeStamp(String timestamp) {
        if (!timestamp.equals("")) {
            try {
                Date date = getProjectDate(timestamp);
                return date != null ? date.getTime() : 0;
            } catch (NullPointerException e) {
                LOG.error(e);
            }
        }
        return 0;
    }

    private Date getProjectDate(String timestamp)
    {
        try {
            return new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH).parse(timestamp);
        } catch (java.text.ParseException e) {
            LOG.error(timestamp + " is not in expected format " + DATE_FORMAT, e);
        }
        return null;
    }

    private String getUrlWithUserData(String url) {
        StringBuilder strBuilderUrl = new StringBuilder(url);
        String username = settings.getUsername();
        String password = settings.getPassword();
        if (!username.isEmpty() && !password.isEmpty()) {
            int indexOfProtocolEnd = strBuilderUrl.lastIndexOf(PROTOCOL_SPLITTER) + PROTOCOL_SPLITTER.length();
            String userData = username + ":" + password + "@";
            strBuilderUrl.insert(indexOfProtocolEnd, userData);
        }
        return strBuilderUrl.toString();
    }

    private String getProjectId(NodeList cxXMLResultsTag) {
        return getNodeAttributeValue(cxXMLResultsTag, PROJECT_ID);
    }

    private String getProjectName(NodeList cxXMLResultsTag, String scanStart) {
        String name = getNodeAttributeValue(cxXMLResultsTag, PROJECT_NAME);
        Date date = getProjectDate(scanStart);
        if (date == null) {
            return name;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 1);
        return name + ":"
                + calendar.get(Calendar.YEAR) + "-"
                + calendar.get(Calendar.MONTH) + "-"
                + calendar.get(Calendar.DAY_OF_MONTH);
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
