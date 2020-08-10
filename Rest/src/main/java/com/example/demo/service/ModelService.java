package com.example.demo.service;

import com.example.demo.model.Model;

import java.util.List;

public interface ModelService {

    void createModel(Model model);

    Model readModel(String modelId);

    boolean deleteModel(String modelId);

    void setMaxId(int id);

    int getMaxId();

    List<Model> readAllModels();
}
