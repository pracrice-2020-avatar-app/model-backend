package com.example.rest.model;

import java.util.ArrayList;
import java.util.List;

public class Client extends Parent {

    private Integer id;
    private String name;
    private String email;
    private String text;
    private String imageLink;
    private List<Integer> followers = new ArrayList<>();
    private List<Integer> followed = new ArrayList<>();
    private List<Integer> postsId = new ArrayList<>();
    private List<Integer> modelsId = new ArrayList<>();

    public Integer getId() {
        return id;
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

    public void setId(Integer id) {
        this.id = id;
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

    public void addFollowerId(Integer id){
        followers.add(id);
    }

    public boolean deleteFollowerId(Integer id){

        return followers.remove(id);
    }

    public List<Integer> getFollowers(){
        return followers;
    }

    public void addFollowedId(Integer id){
        followed.add(id);
    }
    public boolean deleteFollowedId(Integer id){
        return followed.remove(id);
    }

    public List<Integer> getFollowed(){
        return followed;
    }

    public Integer getFollowedNumber() {
        return followed.size();
    }

    public List<Integer> getModelsId() {
        return modelsId;
    }

    public List<Integer> getPostsId() {
        return postsId;
    }

    public void addModelId(Integer modelId) {
        modelsId.add(modelId);
    }

    public void addPostId(Integer postId) {
        postsId.add(postId);
    }

    public void deletePostId(Integer postId){
         postsId.remove(postId);
    }

    public void deleteModelId(Integer modelId){   modelsId.remove(modelId);}

    public void setFollowed(List<Integer> followed) {
        this.followed = followed;
    }

    public void setFollowers(List<Integer> followers) {
        this.followers = followers;
    }

    public void setModelsId(List<Integer> modelsId) {
        this.modelsId = modelsId;
    }

    public void setPostsId(List<Integer> postsId) {
        this.postsId = postsId;
    }


}
