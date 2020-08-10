package com.example.demo.service;

import com.example.demo.model.Client;
import com.example.demo.model.Post;

import java.util.List;

public interface PostService {
    void createPost(Post post);

    List<Post> readAllPosts();

    Post readPost(String postId);

    boolean deletePost(String postId);

    boolean updatePost(Post post, String postId);

    void setMaxId(int id);

    int getMaxId();


}
