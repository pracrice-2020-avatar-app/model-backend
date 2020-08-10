package com.example.demo.model;

public class Post extends Parent {
    private String postId;
    private String authorId;
    private String authorName;
    private String text;
    private String imageLink;

    public String getId() {
        return postId;
    }

    public String getText() {
        return text;
    }

    public String getImageLink() {
        return imageLink;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public void setId(String id) {
        this.postId = id;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
    public String getAuthorName() {
        return authorName;
    }
}
