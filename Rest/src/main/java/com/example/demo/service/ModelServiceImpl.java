package com.example.demo.service;

import com.example.demo.model.Model;
import com.example.demo.model.Post;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ModelServiceImpl implements ModelService {
    //Хранилище моделей
    private static final Map<String, Model> MODEL_REPOSITORY_MAP = new HashMap<>();


    // Переменная для генерации ID модели
    private static final AtomicInteger MODEL_ID_HOLDER = new AtomicInteger();

    private static int maxId = 0;

    @Override
    public void createModel(Model model){
        Integer id = MODEL_ID_HOLDER.incrementAndGet();
        model.setId(id.toString());
        MODEL_REPOSITORY_MAP.put(id.toString(),model);
    }

    @Override
    public void setMaxId(int id){
        maxId = id;
        MODEL_ID_HOLDER.set(maxId);
    }

    @Override
    public int getMaxId(){
        return maxId;
    }

    @Override
    public Model readModel(String modeId){
        return MODEL_REPOSITORY_MAP.get(modeId);
    }

    @Override
    public boolean deleteModel(String modelId) {
        return MODEL_REPOSITORY_MAP.remove(modelId) != null;
    }

    @Override
    public List<Model> readAllModels() {
        return new ArrayList<>(MODEL_REPOSITORY_MAP.values());
    }
}
