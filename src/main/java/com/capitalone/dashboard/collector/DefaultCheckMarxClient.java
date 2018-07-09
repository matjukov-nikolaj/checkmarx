package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.CheckMarxProject;
import com.capitalone.dashboard.model.CodeSecurity;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.text.SimpleDateFormat;
import org.apache.commons.net.ftp.FTPClient;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component("DefaultCheckMarxClient")
public class DefaultCheckMarxClient implements CheckMarxClient {
    private static final Log LOG = LogFactory.getLog(DefaultCheckMarxClient.class);

    private static final String XML_EXTENSION = "xml";
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

    private int numberOfLow;
    private int numberOfMedium;
    private int numberOfHigh;
    private CheckMarxSettings settings;

    @Autowired
    public DefaultCheckMarxClient(CheckMarxSettings settings) {
        this.settings = settings;
        this.initializationRiskLevels();
    }

    @Override
    public List<CheckMarxProject> getProjects(String instanceUrl) {
        List<CheckMarxProject> projects = new ArrayList<>();
        try {
            instanceUrl = getUrlWithUserData(instanceUrl);
            List<String> projectNames = getProjectsNamesFromFTPClient(instanceUrl);
            for (String name : projectNames) {
                String url = instanceUrl + name;
                Document document = getDocument(url);
                NodeList cxXMLResultsTag = document.getElementsByTagName(CHECK_MARX_XML_RESULTS_TAG);
                CheckMarxProject project = new CheckMarxProject();
                project.setInstanceUrl(url);
                project.setProjectId(getProjectId(cxXMLResultsTag));
                project.setProjectName(getProjectName(cxXMLResultsTag));
                projects.add(project);
            }
        } catch (Exception e) {
            LOG.error(e);
        }
        return projects;
    }


    @Override
    public CodeSecurity currentCodeSecurity(CheckMarxProject project) {
        try {
            Document document = getDocument(project.getInstanceUrl());
            parseDocument(document);
            CodeSecurity codeSecurity = new CodeSecurity();
            codeSecurity.setNumberOfHigh(numberOfHigh);
            codeSecurity.setNumberOfMedium(numberOfMedium);
            codeSecurity.setNumberOfLow(numberOfLow);
            codeSecurity.setName(project.getProjectName());
            codeSecurity.setUrl(project.getInstanceUrl());
            codeSecurity.setTimestamp(getTimeStamp(getScanStart(document)));
            initializationRiskLevels();
            return codeSecurity;
        } catch (Exception e) {
            LOG.error(e);
        }
        return null;
    }

    private void parseDocument(Document document) {
        try {
            NodeList nodesWithResults = document.getElementsByTagName(RESULT_TAG);
            findScanRiskLevels(nodesWithResults);
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    private List<String> getProjectsNamesFromFTPClient(String instanceUrl) {
        List<String> projectFiles = new ArrayList<>();
        try {
            FTPClient ftpClient = new FTPClient();
            URL url = new URL(instanceUrl);
            ftpClient.connect(url.getHost(), url.getPort());
            ftpClient.login(settings.getUsername(), settings.getPassword());
            String[] fileNames = ftpClient.listNames(url.getPath());
            projectFiles.addAll(getXmlFiles(fileNames));
        } catch (Exception e) {
            LOG.error(e);
        }
        return projectFiles;
    }

    private List<String> getXmlFiles(String[] fileNames) {
        List<String> xmlFiles = new ArrayList<>();
        String fileExtension = "";
        for (String name : fileNames) {
            fileExtension = FilenameUtils.getExtension(name);
            if (fileExtension.equals(XML_EXTENSION)) {
                xmlFiles.add(name);
            }
        }
        return xmlFiles;
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
                numberOfHigh++;
                break;
            case MEDIUM:
                numberOfMedium++;
                break;
            case LOW:
                numberOfLow++;
                break;
            default:
                break;
        }
    }

    private void initializationRiskLevels() {
        numberOfLow = 0;
        numberOfMedium = 0;
        numberOfHigh = 0;
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
                return new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH).parse(timestamp).getTime();
            } catch (java.text.ParseException e) {
                LOG.error(timestamp + " is not in expected format " + DATE_FORMAT, e);
            }
        }
        return 0;
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

    private String getProjectName(NodeList cxXMLResultsTag) {
        return getNodeAttributeValue(cxXMLResultsTag, PROJECT_NAME);
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
