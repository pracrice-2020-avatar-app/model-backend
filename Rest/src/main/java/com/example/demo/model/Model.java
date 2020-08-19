package com.example.demo.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Model extends Parent {
    private String modelId;
    private String authorId;
    private String authorName;
    private String modelLink;
    private Map<String, ArrayList<String> > sensors;

    public String getAuthorName() {
        return authorName;
    }

    public void setSensors(HashMap<String, ArrayList<String> > sensorMap) {
        sensors = sensorMap;
    }

    public Map<String, ArrayList<String> > getSensors() {
        return sensors;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void setId(String id) {
        this.modelId = id;
    }

    public String getId() {
        return modelId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getModelLink() {
        return modelLink;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public void setModelLink(String modelLink) {
        this.modelLink = modelLink;
    }
}
