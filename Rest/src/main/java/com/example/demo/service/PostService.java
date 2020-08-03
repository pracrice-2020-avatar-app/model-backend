package com.example.demo.service;

import com.example.demo.model.Client;
import com.example.demo.model.Post;

import java.util.List;

public interface PostService {
    void createPost(Post post);

    List<Post> readAllPosts();

    Post readPost(int postId);

    boolean deletePost(int postId);

    boolean updatePost(Post post, int postId);

}
