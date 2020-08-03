package com.example.demo.service;

import com.example.demo.model.Client;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ClientServiceImpl implements ClientService {

    // Хранилище клиентов
    private static final Map<Integer, Client> CLIENT_REPOSITORY_MAP = new HashMap<>();

    // Переменная для генерации ID клиента
    private static final AtomicInteger CLIENT_ID_HOLDER = new AtomicInteger();

    private static int maxId = 0;

    @Override
    public void create(Client client) {
        final int clientId = CLIENT_ID_HOLDER.incrementAndGet();
        client.setId(clientId);
        CLIENT_REPOSITORY_MAP.put(clientId, client);
    }

    @Override
    public void setMaxId(int id){
        maxId = id;
        CLIENT_ID_HOLDER.set(maxId);
    }

    @Override
    public int getMaxId(){
        return maxId;
    }

    @Override
    public List<Client> readAll() {
        return new ArrayList<>(CLIENT_REPOSITORY_MAP.values());
    }

    @Override
    public Client read(int id) {
        return CLIENT_REPOSITORY_MAP.get(id);
    }

    @Override
    public List<Integer> readAllFollowers(int id){ return CLIENT_REPOSITORY_MAP.get(id).getFollowers(); }

    @Override
    public List<Integer> readAllFollowed(int id){ return CLIENT_REPOSITORY_MAP.get(id).getFollowed(); }

    @Override
    public boolean update(Client client, int id) {
        if (CLIENT_REPOSITORY_MAP.containsKey(id)) {
            client.setId(id);
            client.setFollowers(CLIENT_REPOSITORY_MAP.get(id).getFollowers());
            client.setFollowed(CLIENT_REPOSITORY_MAP.get(id).getFollowed());
            client.setModelsId(CLIENT_REPOSITORY_MAP.get(id).getModelsId());
            client.setPostsId(CLIENT_REPOSITORY_MAP.get(id).getPostsId());
            CLIENT_REPOSITORY_MAP.put(id, client);

            return true;
        }

        return false;
    }

    @Override
    public boolean delete(int id) {
        if (CLIENT_REPOSITORY_MAP.containsKey(id)){
            CLIENT_REPOSITORY_MAP.get(id).getFollowers().forEach(follower -> CLIENT_REPOSITORY_MAP.get(follower).deleteFollowedId(id));
            CLIENT_REPOSITORY_MAP.get(id).getFollowed().forEach(followed -> CLIENT_REPOSITORY_MAP.get(followed).deleteFollowerId(id));
            CLIENT_REPOSITORY_MAP.remove(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean addFollower(int id,int followerId) {
        if (CLIENT_REPOSITORY_MAP.containsKey(id) && id != followerId){
            if (!CLIENT_REPOSITORY_MAP.get(id).getFollowers().contains(followerId)) {
                CLIENT_REPOSITORY_MAP.get(id).addFollowerId(followerId);
                CLIENT_REPOSITORY_MAP.get(followerId).addFollowedId(id);
            }
                return true;
        }
        return false;

    }
    @Override
    public boolean addFollowed(int id,int followedId) {
        if (CLIENT_REPOSITORY_MAP.containsKey(id) && id != followedId){
            if(!CLIENT_REPOSITORY_MAP.get(id).getFollowers().contains(followedId)) {
                CLIENT_REPOSITORY_MAP.get(id).addFollowedId(followedId);
                CLIENT_REPOSITORY_MAP.get(followedId).addFollowerId(id);
            }
            return true;
        }
        return false;

    }

    @Override
    public boolean deleteFollower(int id,Integer followerId){
        return CLIENT_REPOSITORY_MAP.get(id).deleteFollowerId(followerId) && CLIENT_REPOSITORY_MAP.get(followerId).deleteFollowedId(id);
    }

    @Override
    public boolean deleteFollowed(int id,Integer followedId){
        return CLIENT_REPOSITORY_MAP.get(id).deleteFollowedId(followedId) && CLIENT_REPOSITORY_MAP.get(followedId).deleteFollowerId(id);
    }

    @Override
    public List<Integer> readPosts(int id) {
        return CLIENT_REPOSITORY_MAP.get(id).getPostsId();
    }

    @Override
    public void addModelId(int modelId, int id) {
       CLIENT_REPOSITORY_MAP.get(id).addModelId(modelId);
    }

    @Override
    public void addPostId(int postId, int id) {
        CLIENT_REPOSITORY_MAP.get(id).addPostId(postId);
    }
}