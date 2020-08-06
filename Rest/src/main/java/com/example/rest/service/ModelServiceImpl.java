package com.example.rest.service;

import com.example.demo.model.Model;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ModelServiceImpl implements ModelService {
    //Хранилище моделей
    private static final Map<Integer, Model> MODEL_REPOSITORY_MAP = new HashMap<>();


    // Переменная для генерации ID модели
    private static final AtomicInteger MODEL_ID_HOLDER = new AtomicInteger();

    @Override
    public void createModel(Model model){
        Integer id = MODEL_ID_HOLDER.incrementAndGet();
        model.setId(id);
        MODEL_REPOSITORY_MAP.put(id,model);
    }

    @Override
    public Model readModel(Integer modeId){
        return MODEL_REPOSITORY_MAP.get(modeId);
    }

    @Override
    public boolean deleteModel(Integer modelId) {
        return MODEL_REPOSITORY_MAP.remove(modelId) != null;
    }

    @Override
    public List<Model> readAllModels() {
        return new ArrayList<>(MODEL_REPOSITORY_MAP.values());
    }
}
