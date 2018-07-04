package com.capitalone.dashboard.model;


import com.capitalone.dashboard.model.BaseModel;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="code_security")
public class CodeSecurity extends BaseModel {
    private ObjectId collectorItemId;
    private long timestamp;

    private String projectName;
    private String version;
    private String url;
    private int numberOfHigh;
    private int numberOfMedium;
    private int numberOfLow;
    private ObjectId buildId;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return projectName;
    }

    public void setName(String name) {
        this.projectName = name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public ObjectId getCollectorItemId() {
        return collectorItemId;
    }

    public void setCollectorItemId(ObjectId collectorItemId) {
        this.collectorItemId = collectorItemId;
    }

    public ObjectId getBuildId() {
        return buildId;
    }

    public void setBuildId(ObjectId buildId) {
        this.buildId = buildId;
    }

    public int getNumberOfHigh() {
        return numberOfHigh;
    }

    public void setNumberOfHigh(int value) {
        this.numberOfHigh = value;
    }

    public int getNumberOfMedium() {
        return numberOfMedium;
    }

    public void setNumberOfMedium(int value) {
        this.numberOfMedium = value;
    }

    public int getNumberOfLow() {
        return numberOfLow;
    }

    public void setNumberOfLow(int value) {
        this.numberOfLow = value;
    }
}
