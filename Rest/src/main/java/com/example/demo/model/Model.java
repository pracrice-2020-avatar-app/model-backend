package com.example.demo.model;

public class Model extends Parent {
    private String modelId;
    private String authorId;
    private String authorName;
    private String modelLink;

    public String getAuthorName() {
        return authorName;
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
