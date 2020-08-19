package com.example.demo.model;

import java.util.ArrayList;
import java.util.HashMap;

public class Model extends Parent {
    private String modelId;
    private String authorId;
    private String authorName;
    private String modelLink;
    private HashMap<Integer, ArrayList<Float> > SensorMap = new HashMap<>();

    public String getAuthorName() {
        return authorName;
    }

    public void setSensorMap(HashMap<Integer, ArrayList<Float>> sensorMap) {
        this.SensorMap = sensorMap;
    }

    public HashMap<Integer, ArrayList<Float>> getSensorMap() {
        return SensorMap;
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
