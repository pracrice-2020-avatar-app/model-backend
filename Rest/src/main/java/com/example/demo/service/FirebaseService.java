package com.example.demo.service;

import com.example.demo.model.Client;
import com.example.demo.model.Parent;

import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;

public interface FirebaseService {
    String saveDetails(Parent parent, String type) throws ExecutionException, InterruptedException, FileNotFoundException;

    Client getClientDetails(String id) throws ExecutionException, InterruptedException;

    void includeClients(ClientService clientService);

    void includeModels(ModelService modelService);

    void includePosts(PostService postService);

    String update(Parent parent,String type) throws ExecutionException, InterruptedException;

    void delete(Parent parent,String type);

    void getFromStorage(String imageLink,String id);
}
