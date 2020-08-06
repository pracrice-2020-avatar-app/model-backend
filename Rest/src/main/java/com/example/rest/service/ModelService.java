package com.example.rest.service;

import com.example.demo.model.Model;

import java.util.List;

public interface ModelService {

    void createModel(Model model);

    Model readModel(Integer modelId);

    boolean deleteModel(Integer modelId);

    List<Model> readAllModels();
}
