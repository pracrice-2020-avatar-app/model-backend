package com.example.rest.service;
import com.example.rest.model.*;

import java.util.List;

public interface PostService {
    void createPost(Post post);

    List<Post> readAllPosts();

    Post readPost(int postId);

    boolean deletePost(int postId);

    boolean updatePost(Post post, int postId);

}
