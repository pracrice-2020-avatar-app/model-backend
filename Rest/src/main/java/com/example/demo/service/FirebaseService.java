package com.example.demo.service;

import com.example.demo.model.Client;
import com.example.demo.model.Parent;

import java.util.concurrent.ExecutionException;

public interface FirebaseService {
    String saveClientDetails(Client client) throws ExecutionException, InterruptedException;

    Client getClientDetails(Integer id) throws ExecutionException, InterruptedException;

    void includeClients(ClientService clientService);

    String update(Parent parent,String type) throws ExecutionException, InterruptedException;

    void delete(Parent parent,String type);
}
