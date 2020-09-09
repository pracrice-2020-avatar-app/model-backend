package com.example.demo.model;

import java.util.ArrayList;
import java.util.List;

public class Client extends Parent {

    private String clientId;
    private String name;
    private String email;
    private String text;
    private String imageLink;
    private List<String> followers = new ArrayList<>();
    private List<String> followed = new ArrayList<>();
    private List<String> postsId = new ArrayList<>();
    private List<String> modelsId = new ArrayList<>();

    public String getDeviceToken() {
        return deviceToken;
    }

    private String deviceToken;

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getId() {
        return clientId;
    }

    public Integer getFollowersNumber() {
        return followers.size();
    }


    public String getImageLink() {
        return imageLink;
    }

    public String getText() {
        return text;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setId(String id) {
        this.clientId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void addFollowerId(String id){
        followers.add(id);
    }

    public boolean deleteFollowerId(String id){

        return followers.remove(id);
    }

    public List<String> getFollowers(){
        return followers;
    }

    public void addFollowedId(String id){
        followed.add(id);
    }

    public boolean deleteFollowedId(String id){
        return followed.remove(id);
    }

    public List<String> getFollowed(){
        return followed;
    }

    public Integer getFollowedNumber() {
        return followed.size();
    }

    public List<String> getModelsId() {
        return modelsId;
    }

    public List<String> getPostsId() {
        return postsId;
    }

    public void addModelId(String modelId) {
        modelsId.add(modelId);
    }

    public void addPostId(String postId) {
        postsId.add(postId);
    }

    public void deletePostId(String postId){
         postsId.remove(postId);
    }

    public void deleteModelId(String modelId){   modelsId.remove(modelId);}

    public void setFollowed(List<String> followed) {
        this.followed = followed;
    }

    public void setFollowers(List<String> followers) {
        this.followers = followers;
    }

    public void setModelsId(List<String> modelsId) {
        this.modelsId = modelsId;
    }

    public void setPostsId(List<String> postsId) {
        this.postsId = postsId;
    }


}
