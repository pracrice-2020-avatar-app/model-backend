package com.example.demo.model;

public class Model extends Parent {
    private Integer modelId;
    private Integer authorId;
    private String authorName;
    private String modelLink;

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void setId(Integer id) {
        this.modelId = id;
    }

    public Integer getId() {
        return modelId;
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public String getModelLink() {
        return modelLink;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }

    public void setModelLink(String modelLink) {
        this.modelLink = modelLink;
    }
}
