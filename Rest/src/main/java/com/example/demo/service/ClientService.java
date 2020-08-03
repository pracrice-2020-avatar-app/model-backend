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
    Client read(int id);

    /**
     * Обновляет клиента с заданным ID,
     * в соответствии с переданным клиентом
     * @param client - клиент в соответсвии с которым нужно обновить данные
     * @param id - id клиента которого нужно обновить
     * @return - true если данные были обновлены, иначе false
     */
    boolean update(Client client, int id);

    /**
     * Удаляет клиента с заданным ID
     * @param id - id клиента, которого нужно удалить
     * @return - true если клиент был удален, иначе false
     */

    boolean delete(int id);

    /**
     * Возвращает ID всех подписчиков клиента с заданным ID
     * @param id - id клиента, подписчиков которого нужно показать
     * @return - ID подписчиков
     */

    List<Integer> readAllFollowers(int id);

    /**
     * Добавляет ID в список подписчиков клиента
     * @param followerId - id подписчика
     * @param id - id клиента
     * @return - true если подписчик был добавлен, иначе false
     */

    boolean addFollower(int followerId,int id);

    /**
     * Удаляет ID подписчика из соответсвующего списка у клиента
     * @param id - id клиента
     * @param followedId - id подписчика
     * @return
     */

    boolean deleteFollower(int id,Integer followedId);

    List<Integer> readAllFollowed(int id);

    boolean addFollowed(int followedId,int id);

    boolean deleteFollowed(int id,Integer followedId);

    void addPostId(int postId, int id);

    void addModelId(int modelId,int id);

    List<Integer> readPosts(int id);
    
    void setMaxId(int id);

    int getMaxId();
}