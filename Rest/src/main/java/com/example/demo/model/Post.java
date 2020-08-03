package com.example.demo.model;

public class Post extends Parent {
    private Integer id;
    private Integer authorId;
    private String authorName;
    private String text;
    private String imageLink;

    public Integer getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getImageLink() {
        return imageLink;
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
    public String getAuthorName() {
        return authorName;
    }
}
