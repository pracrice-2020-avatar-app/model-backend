package com.example.demo.service;

import com.example.demo.model.Client;
import com.example.demo.model.Post;

import java.util.List;

public interface ClientService {

    /**
     * Создает нового клиента
     * @param client - клиент для создания
     */
    void create(Client client);

    /**
     * Возвращает список всех имеющихся клиентов
     * @return список клиентов
     */
    List<Client> readAll();

    /**
     * Возвращает клиента по его ID
     * @param id - ID клиента
     * @return - объект клиента с заданным ID
     */
    Client read(String id);

    /**
     * Обновляет клиента с заданным ID,
     * в соответствии с переданным клиентом
     * @param client - клиент в соответсвии с которым нужно обновить данные
     * @param id - id клиента которого нужно обновить
     * @return - true если данные были обновлены, иначе false
     */
    boolean update(Client client, String id);

    /**
     * Удаляет клиента с заданным ID
     * @param id - id клиента, которого нужно удалить
     * @return - true если клиент был удален, иначе false
     */

    boolean delete(String id);

    /**
     * Возвращает ID всех подписчиков клиента с заданным ID
     * @param id - id клиента, подписчиков которого нужно показать
     * @return - ID подписчиков
     */

    List<String> readAllFollowers(String id);

    /**
     * Добавляет ID в список подписчиков клиента
     * @param followerId - id подписчика
     * @param id - id клиента
     * @return - true если подписчик был добавлен, иначе false
     */

    boolean addFollower(String followerId,String id);

    /**
     * Удаляет ID подписчика из соответсвующего списка у клиента
     * @param id - id клиента
     * @param followedId - id подписчика
     * @return
     */

    boolean deleteFollower(String id,String followedId);

    List<String> readAllFollowed(String id);

    boolean addFollowed(String followedId,String id);

    boolean deleteFollowed(String id,String followedId);

    void addPostId(String postId, String id);

    void addModelId(String modelId,String id);

    List<String> readPosts(String id);
    
    //void setMaxId(int id);

    //int getMaxId();
}