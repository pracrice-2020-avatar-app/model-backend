package com.example.demo.controller;

import com.example.demo.model.Client;
import com.example.demo.model.Model;
import com.example.demo.model.Post;
import com.example.demo.service.*;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.*;
import java.util.Collections;
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
    public Controller(ClientService clientService, PostService postService, ModelService modelService, FirebaseServiceImpl firebaseServiceImpl,FirebaseInitialize firebaseInitialize) throws IOException {
        this.clientService = clientService;
        this.postService = postService;
        this.modelService = modelService;
        this.firebaseServiceImpl = firebaseServiceImpl;
        this.firebaseInitialize = firebaseInitialize;
        firebaseInitialize.init();
        firebaseServiceImpl.includeClients(clientService);
        firebaseServiceImpl.includeModels(modelService);
        firebaseServiceImpl.includePosts(postService);
    }
    @PostMapping(value = "/posts")
    public ResponseEntity<?> createPost(@RequestBody Post post) throws ExecutionException, InterruptedException, FileNotFoundException {
        final Client client = clientService.read(post.getAuthorId());
        if (client != null) {
            postService.createPost(post);
            clientService.addPostId(post.getId(),(post.getAuthorId()));
            firebaseServiceImpl.update(clientService.read(post.getAuthorId()),"clients");
            post.setAuthorName(client.getName());
            post.setImageLink(post.getImageLink());
            firebaseServiceImpl.saveDetails(post,"posts");
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
        final Post post = postService.readPost(Integer.toString(postId));

        return post != null
                ? new ResponseEntity<>(post, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping(value = "/posts/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable(name = "postId") Integer postId) throws ExecutionException, InterruptedException {
        Post post = postService.readPost(postId.toString());
        clientService.read(post.getAuthorId()).deletePostId(postId.toString());
        final boolean deleted = postService.deletePost(postId.toString());
        if (deleted){
            firebaseServiceImpl.delete(post,"posts");
            firebaseServiceImpl.update(clientService.read(post.getAuthorId()),"clients");
        }

        return deleted
                ? new ResponseEntity<>(HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
    }

    @PutMapping(value = "/posts/{postId}")
    public ResponseEntity<?> updatePost(@PathVariable(name = "postId") int postId, @RequestBody Post post) throws ExecutionException, InterruptedException {
        final boolean updated = postService.updatePost(post, Integer.toString(postId));
        if (updated){
            firebaseServiceImpl.update(post,"posts");
            clientService.read(Integer.toString(postId)).setImageLink(post.getImageLink());
            firebaseServiceImpl.update( clientService.read(Integer.toString(postId)),"clients");
        }
        return updated
                ? new ResponseEntity<>(HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
    }

    @PostMapping(value = "/models")
    public ResponseEntity<?> createModel(@RequestBody Model model) throws ExecutionException, InterruptedException, IOException {
        final Client client = clientService.read(model.getAuthorId());
        if (client != null) {
            modelService.createModel(model);
            client.addModelId(model.getId());
            model.setAuthorName(client.getName());
            firebaseServiceImpl.update(client,"clients");
            firebaseServiceImpl.saveDetails(model,"models");
           firebaseServiceImpl.getFromStorage(model.getModelLink(),model.getId());
          //  String command1 = "cd ..";
           // Process proc = Runtime.getRuntime().exec(command1);
         //   command1 = "cd model-backend";
         //   proc = Runtime.getRuntime().exec(command1);
           // try {


                String command2 = "python -u C:/Users/Kolldun/IdeaProjects/model-backend/main.py --Id " + model.getId();
                Process proc = Runtime.getRuntime().exec(command2);
                // Read the output

                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(proc.getInputStream()));

                String line = "";
                while ((line = reader.readLine()) != null) {
                    if (line.equals(" type INITIAL pair ids: X enter Y enter")) {
                        System.out.println("Change error y");
                        firebaseServiceImpl.createError(model.getAuthorId());
                        return new ResponseEntity<>(-1,HttpStatus.BAD_REQUEST);
                    }
                    System.out.print(line + "\n");
                }
                try {
                    proc.waitFor();
                } catch(Exception e) {
                    System.out.println("main error (preview camera vector)");
                    firebaseServiceImpl.createError(model.getAuthorId());
                    return new ResponseEntity<>(-1,HttpStatus.BAD_REQUEST);
                }
         //   }
        //    catch (Exception e){
        //        System.out.println("Failed to create model");
        //        return new ResponseEntity<>(-1,HttpStatus.BAD_REQUEST);
      //      }

            File log = new File("C:/Users/Kolldun/IdeaProjects/model-backend/log.txt");
            FileInputStream fis = new FileInputStream(log);
            byte[] data = new byte[(int) log.length()];
            fis.read(data);
            String datastr = new String(data);
            String[] dataspl = datastr.split("\n");
            fis.close();
            if (dataspl[dataspl.length - 1].substring(0, dataspl[dataspl.length - 1].length() - 1).equals((model.getId() + " Success").toString())) {
                System.out.println("Success");
                firebaseServiceImpl.uploadModelToStorage(model.getId());
                Post post = new Post();
                post.setAuthorId(model.getAuthorId());
                post.setText(")))");
                post.setImageLink("ModelsPhoto/Model" + model.getId() + "/scene_dense_mesh_texture_900.png");
                System.out.println("ModelsPhoto/Model" + model.getId() + "/scene_dense_mesh_texture_900.png");
                createPost(post);
            } else if (dataspl[dataspl.length - 1].substring(0, dataspl[dataspl.length - 1].length() - 1).equals(model.getId() + " Error")) {
                System.out.println("Failed to create model 2");
                firebaseServiceImpl.createError(model.getAuthorId());
                return new ResponseEntity<>(-1,HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(model.getAuthorName(),HttpStatus.CREATED);
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
    public ResponseEntity<Model> readModel(@PathVariable(name = "modelId") Integer modelId) throws FileNotFoundException, ExecutionException, InterruptedException {
        final Model model = modelService.readModel(modelId.toString());

        return model != null
                ? new ResponseEntity<>(model, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping(value = "/models/{modelId}")
    public ResponseEntity<?> deleteModel(@PathVariable(name = "modelId") Integer modelId) throws ExecutionException, InterruptedException {
        Model model = modelService.readModel(modelId.toString());
        clientService.read(model.getAuthorId()).deleteModelId(modelId.toString());
        final boolean deleted = modelService.deleteModel(modelId.toString());
        if (deleted){
            firebaseServiceImpl.delete(model,"models");
            firebaseServiceImpl.update(clientService.read(model.getAuthorId()),"clients");
        }

        return deleted
                ? new ResponseEntity<>(HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
    }


    @PostMapping(value = "/clients")
    public ResponseEntity<?> createClient(@RequestBody Client client) throws ExecutionException, InterruptedException, FileNotFoundException {
        clientService.create(client);
        firebaseServiceImpl.saveDetails(client,"clients");
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
    public ResponseEntity<Client> readClient(@PathVariable(name = "id") String id) {
        final Client client = clientService.read(id);

        return client != null
                ? new ResponseEntity<>(client, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping(value = "/clients/{id}")
    public ResponseEntity<?> updateClient(@PathVariable(name = "id") String id, @RequestBody Client client) throws ExecutionException, InterruptedException {
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
    public ResponseEntity<?> deleteClient(@PathVariable(name = "id") String id) {
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
            client.getPostsId().forEach(postId -> {
                firebaseServiceImpl.delete(postService.readPost(postId),"posts");
            });
            client.getModelsId().forEach(modelId -> {
                firebaseServiceImpl.delete(modelService.readModel(modelId),"models");
            });
        }

        return deleted
                ? new ResponseEntity<>(HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
    }

    @PostMapping(value = "/clients/{id}/followers")
    public ResponseEntity<?> addFollower(@PathVariable(name = "id") String id,@RequestParam( value = "followerId") String followerId) throws ExecutionException, InterruptedException {

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
    public ResponseEntity<?> addFollowed(@PathVariable(name = "id") String id,@RequestParam( value = "followedId") String followedId) throws ExecutionException, InterruptedException {

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
    public ResponseEntity<List<String> > readAllFollowers(@PathVariable(name = "id") String id) {
        final List<String> followers = clientService.readAllFollowers(id);
        System.out.println(followers);
        return followers != null &&  !followers.isEmpty()
                ? new ResponseEntity<>(followers, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping(value = "/clients/{id}/followers")
    public ResponseEntity<?> deleteFollower(@PathVariable(name = "id") String id,@RequestParam("followerId") String followerId) throws ExecutionException, InterruptedException {
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
    public ResponseEntity<List<String>> readAllFollowed(@PathVariable(name = "id") String id) {
        final List<String> followed= clientService.readAllFollowed(id);
        System.out.println(followed);
        return followed != null &&  !followed.isEmpty()
                ? new ResponseEntity<>(followed, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }


    @DeleteMapping(value = "/clients/{id}/followed")
    public ResponseEntity<?> deleteFollowed(@PathVariable(name = "id") String id,@RequestParam("followedId") String followedId) throws ExecutionException, InterruptedException {
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
    public ResponseEntity<List<String>> readClientPosts(@PathVariable(name = "id") String id) {
        final List<String> posts = clientService.readPosts(id);
        return posts != null &&  !posts.isEmpty()
                ? new ResponseEntity<>(posts, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}