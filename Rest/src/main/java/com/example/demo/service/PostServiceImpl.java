package com.example.demo.service;

import com.example.demo.model.Client;
import com.example.demo.model.Post;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import com.example.demo.service.ClientService;
import org.springframework.stereotype.Service;

@Service
public class PostServiceImpl implements PostService {
    //Хранилище Постов
    private static final Map<Integer, Post> POST_REPOSITORY_MAP = new HashMap<>();


    // Переменная для генерации ID поста
    private static final AtomicInteger POST_ID_HOLDER = new AtomicInteger();
    @Override
    public void createPost (Post post){
        final int postId = POST_ID_HOLDER.incrementAndGet();
        post.setId(postId);
        POST_REPOSITORY_MAP.put(postId,post);
    }

    @Override
    public List<Post> readAllPosts(){
        return new ArrayList<>(POST_REPOSITORY_MAP.values());
    }

    @Override
    public Post readPost(int postId) {
        return POST_REPOSITORY_MAP.get(postId);
    }

    public boolean deletePost(int postId){
        return POST_REPOSITORY_MAP.remove(postId) != null;
    }

    public boolean updatePost(Post post, int postId){
        if (POST_REPOSITORY_MAP.containsKey(postId)) {
            post.setId(postId);
            POST_REPOSITORY_MAP.put(postId,post);
            return true;
        }

        return false;
    }
}