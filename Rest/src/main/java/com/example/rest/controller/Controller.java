package com.example.rest.controller;


import com.example.rest.service.*;
import com.example.rest.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
public class Controller {

    private final ClientService clientService;
    private final PostService postService;
    private final ModelService modelService;
    private final FirebaseServiceImpl firebaseServiceImpl;
    private final FirebaseInitialize firebaseInitialize;

    @Autowired
    public Controller(ClientService clientService, PostService postService, ModelService modelService, FirebaseServiceImpl firebaseServiceImpl, FirebaseInitialize firebaseInitialize) throws IOException {
        this.clientService = clientService;
        this.postService = postService;
        this.modelService = modelService;
        this.firebaseServiceImpl = firebaseServiceImpl;
        this.firebaseInitialize = firebaseInitialize;
        firebaseInitialize.init();
        firebaseServiceImpl.includeClients(clientService);

    }
    @PostMapping(value = "/posts")
    public ResponseEntity<?> createPost(@RequestBody Post post) {
        final Client client = clientService.read(post.getAuthorId());
        if (client != null) {
            postService.createPost(post);
            clientService.addPostId(post.getId(),post.getAuthorId());
            post.setAuthorName(client.getName());
            post.setImageLink(client.getImageLink());
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        return new ResponseEntity<>(post.getAuthorName(),HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/posts")
    public ResponseEntity<List<Post>> readAllPosts() {
        final List<Post> posts = postService.readAllPosts();

        return posts != null &&  !posts.isEmpty()
                ? new ResponseEntity<>(posts, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/posts/{postId}")
    public ResponseEntity<Post> readPost(@PathVariable(name = "postId") int postId) {
        final Post post = postService.readPost(postId);

        return post != null
                ? new ResponseEntity<>(post, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping(value = "/posts/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable(name = "postId") Integer postId) {
        clientService.read(postService.readPost(postId).getAuthorId()).deletePostId(postId);
        final boolean deleted = postService.deletePost(postId);

        return deleted
                ? new ResponseEntity<>(HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
    }

    @PutMapping(value = "/posts/{postId}")
    public ResponseEntity<?> updatePost(@PathVariable(name = "postId") int postId, @RequestBody Post post) {
        final boolean updated = postService.updatePost(post, postId);
        return updated
                ? new ResponseEntity<>(HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
    }

    @PostMapping(value = "/models")
    public ResponseEntity<?> createModel(@RequestBody Model model) {
        final Client client = clientService.read(model.getAuthorId());
        if (client != null) {
            modelService.createModel(model);
            client.addModelId(model.getId());
            clientService.addModelId(model.getId(), model.getAuthorId());
            model.setAuthorName(client.getName());
            return new ResponseEntity<>(model.getId(),HttpStatus.CREATED);
        }
        return new ResponseEntity<>(model.getAuthorName(),HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/models")
    public ResponseEntity<List<Model>> readAllModels() {
        final List<Model> models = modelService.readAllModels();

        return models != null &&  !models.isEmpty()
                ? new ResponseEntity<>(models, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/models/{modelId}")
    public ResponseEntity<Model> readModel(@PathVariable(name = "modelId") Integer modelId) {
        final Model model = modelService.readModel(modelId);

        return model != null
                ? new ResponseEntity<>(model, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping(value = "/models/{modelId}")
    public ResponseEntity<?> deleteModel(@PathVariable(name = "modelId") Integer modelId) {
        clientService.read(modelService.readModel(modelId).getAuthorId()).deleteModelId(modelId);
        final boolean deleted = modelService.deleteModel(modelId);

        return deleted
                ? new ResponseEntity<>(HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
    }


    @PostMapping(value = "/clients")
    public ResponseEntity<?> createClient(@RequestBody Client client) throws ExecutionException, InterruptedException {
        clientService.create(client);
        firebaseServiceImpl.saveClientDetails(client);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping(value = "/clients")
    public ResponseEntity<List<Client>> readAllClients() {
        final List<Client> clients = clientService.readAll();

        return clients != null &&  !clients.isEmpty()
                ? new ResponseEntity<>(clients, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/clients/{id}")
    public ResponseEntity<Client> readClient(@PathVariable(name = "id") int id) {
        final Client client = clientService.read(id);

        return client != null
                ? new ResponseEntity<>(client, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping(value = "/clients/{id}")
    public ResponseEntity<?> updateClient(@PathVariable(name = "id") int id, @RequestBody Client client) throws ExecutionException, InterruptedException {
        final boolean updated = clientService.update(client, id);
        if (updated){
            firebaseServiceImpl.update(client,"clients");
            clientService.read(id).getPostsId().forEach(post -> {
                postService.readPost(post).setAuthorName(client.getName());
                postService.readPost(post).setImageLink(client.getImageLink());
            });
            clientService.read(id).getModelsId().forEach(model -> {
                modelService.readModel(model).setAuthorName(client.getName());
            });
        }
        return updated
                ? new ResponseEntity<>(HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
    }

    @DeleteMapping(value = "/clients/{id}")
    public ResponseEntity<?> deleteClient(@PathVariable(name = "id") int id) {
        Client client = clientService.read(id);
        final boolean deleted = clientService.delete(id);

        if (deleted){
            firebaseServiceImpl.delete(client,"clients");
            client.getFollowers().forEach(follower -> {
                try {
                    firebaseServiceImpl.update(clientService.read(follower),"clients");
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
            client.getFollowed().forEach(followed -> {
                try {
                    firebaseServiceImpl.update(clientService.read(followed),"clients");
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
            client.getPostsId().forEach(postService::deletePost);
            client.getModelsId().forEach(modelService::deleteModel);
        }

        return deleted
                ? new ResponseEntity<>(HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
    }

    @PostMapping(value = "/clients/{id}/followers")
    public ResponseEntity<?> addFollower(@PathVariable(name = "id") int id,@RequestParam( value = "followerId") int followerId) throws ExecutionException, InterruptedException {

        final boolean follower = clientService.addFollower(id,followerId);
        if (follower) {
            firebaseServiceImpl.update(clientService.read(id), "clients");
            firebaseServiceImpl.update(clientService.read(followerId), "clients");
        }
        return follower
                ? new ResponseEntity<>(HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
    }
    @PostMapping(value = "/clients/{id}/followed")
    public ResponseEntity<?> addFollowed(@PathVariable(name = "id") int id,@RequestParam( value = "followedId") int followedId) throws ExecutionException, InterruptedException {

        final boolean followed = clientService.addFollowed(id,followedId);
        if (followed) {
            firebaseServiceImpl.update(clientService.read(id), "clients");
            firebaseServiceImpl.update(clientService.read(followedId), "clients");
        }
        return followed
                ? new ResponseEntity<>(HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
    }



    @GetMapping(value = "/clients/{id}/followers")
    public ResponseEntity<List<Integer> > readAllFollowers(@PathVariable(name = "id") int id) {
        final List<Integer> followers = clientService.readAllFollowers(id);
        System.out.println(followers);
        return followers != null &&  !followers.isEmpty()
                ? new ResponseEntity<>(followers, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping(value = "/clients/{id}/followers")
    public ResponseEntity<?> deleteFollower(@PathVariable(name = "id") int id,@RequestParam("followerId") Integer followerId) throws ExecutionException, InterruptedException {
        final boolean deleted = clientService.deleteFollower(id,followerId);
        if (deleted) {
            firebaseServiceImpl.update(clientService.read(id), "clients");
            firebaseServiceImpl.update(clientService.read(followerId), "clients");
        }

        return deleted
                ? new ResponseEntity<>(HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
    }

    @GetMapping(value = "/clients/{id}/followed")
    public ResponseEntity<List<Integer>> readAllFollowed(@PathVariable(name = "id") int id) {
        final List<Integer> followed= clientService.readAllFollowed(id);
        System.out.println(followed);
        return followed != null &&  !followed.isEmpty()
                ? new ResponseEntity<>(followed, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }


    @DeleteMapping(value = "/clients/{id}/followed")
    public ResponseEntity<?> deleteFollowed(@PathVariable(name = "id") int id,@RequestParam("followedId") Integer followedId) throws ExecutionException, InterruptedException {
        final boolean deleted = clientService.deleteFollowed(id,followedId);
        if (deleted) {
            firebaseServiceImpl.update(clientService.read(id), "clients");
            firebaseServiceImpl.update(clientService.read(followedId), "clients");
        }

        return deleted
                ? new ResponseEntity<>(HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
    }

    @GetMapping(value = "/clients/{id}/posts")
    public ResponseEntity<List<Integer>> readClientPosts(@PathVariable(name = "id") int id) {
        final List<Integer> posts = clientService.readPosts(id);
        return posts != null &&  !posts.isEmpty()
                ? new ResponseEntity<>(posts, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}