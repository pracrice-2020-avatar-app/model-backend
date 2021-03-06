package com.example.demo.service;

import com.example.demo.model.*;
import com.google.api.gax.paging.Page;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.cloud.StorageClient;
import com.google.firebase.messaging.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class FirebaseServiceImpl implements FirebaseService{
    public String saveDetails(Parent parent,String type) throws ExecutionException, InterruptedException, FileNotFoundException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection(type).document(parent.getId()).set(parent);
        StorageClient storageClient = StorageClient.getInstance();

        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    public void getFromStorage(String imageLink,String id) {
        StorageClient storageClient = StorageClient.getInstance();
        Page<Blob> bucketPage = storageClient.bucket().list(
                Storage.BlobListOption.prefix(imageLink)
        );
        File dir = new File("Model_images/photos/set" + id);
        dir.getParentFile().mkdirs();
        dir.mkdir();
        for (Blob blob : bucketPage.iterateAll()){

            System.out.println(dir);
            File file = new File(dir + "/" + blob.getName());
            blob.downloadTo(Paths.get(dir + "/" + file.getName()));
        }
    }

    public void uploadModelToStorage(String id) throws FileNotFoundException {
        StorageClient storageClient = StorageClient.getInstance();
        InputStream File = new FileInputStream("C:/Users/Kolldun/IdeaProjects/model-backend/mvg-output/output_set" + id  + "/reconstruction_sequential/mvs_sequential/scene_dense_mesh_texture_900.png");
        String blobString = "ModelsPhoto/" + "Model" + id + "/scene_dense_mesh_texture_900.png";
        storageClient.bucket().create(blobString, File);
    }
    public Client getClientDetails(String id) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection("clients").document(id);
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot document = future.get();
        Client client = null;
        if (document.exists()){
            client = document.toObject(Client.class);
            return client;
            }
        return null;

    }

    @Override
    public void includeClients(ClientService clientService){
        Firestore dbFirestore = FirestoreClient.getFirestore();
        dbFirestore.collection("clients").listDocuments().forEach(client -> {
            try {
                clientService.create(client.get().get().toObject(Client.class));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void includeModels(ModelService modelService){
        Firestore dbFirestore = FirestoreClient.getFirestore();
        dbFirestore.collection("models").listDocuments().forEach(model -> {
            try {
               int maxId = Integer.parseInt(model.get().get().toObject(Model.class).getId());
                if (modelService.getMaxId() < maxId)
                    modelService.setMaxId(maxId - 1);
                modelService.createModel(model.get().get().toObject(Model.class));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void includePosts(PostService postService){
        Firestore dbFirestore = FirestoreClient.getFirestore();
        dbFirestore.collection("posts").listDocuments().forEach(post -> {
            try {
                int maxId = Integer.parseInt(post.get().get().toObject(Post.class).getId());
                if (postService.getMaxId() < maxId)
                    postService.setMaxId(maxId - 1);
                postService.createPost(post.get().get().toObject(Post.class));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public String update(Parent parent, String type) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection(type).document(parent.getId()).set(parent);
        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    @Override
    public void delete(Parent parent,String type){
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> writeResult = dbFirestore.collection(type).document(parent.getId()).delete();
    }

    public String sendByTopic(PushNotifyConf conf, String topic)
            throws InterruptedException, ExecutionException {

        Message message = Message.builder().setTopic(topic)
                .setWebpushConfig(WebpushConfig.builder()
                        .putHeader("ttl", conf.getTtlInSeconds())
                        .setNotification(createBuilder(conf).build())
                        .build())
                .build();

        String response = FirebaseMessaging.getInstance()
                .sendAsync(message)
                .get();
        return response;
    }

    public String sendPersonal(PushNotifyConf conf, String clientToken)
            throws ExecutionException, InterruptedException {
        Message message = Message.builder().setToken(clientToken)
                .setWebpushConfig(WebpushConfig.builder()
                        .putHeader("ttl", conf.getTtlInSeconds())
                        .setNotification(createBuilder(conf).build())
                        .build())
                .build();

        String response = FirebaseMessaging.getInstance()
                .sendAsync(message)
                .get();
        return response;
    }

    public void subscribeUsers(String topic, List<String> clientTokens)
            throws FirebaseMessagingException {
        for (String token : clientTokens) {
            TopicManagementResponse response = FirebaseMessaging.getInstance()
                    .subscribeToTopic(Collections.singletonList(token), topic);
        }
    }

    public String createError(String id) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection("errors").document().set(new Errors(id));
        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    private WebpushNotification.Builder createBuilder(PushNotifyConf conf){
        WebpushNotification.Builder builder = WebpushNotification.builder();
        builder.addAction(new WebpushNotification
                .Action(conf.getClick_action(), "Открыть"))
                .setImage(conf.getIcon())
                .setTitle(conf.getTitle())
                .setBody(conf.getBody());
        return builder;
    }
    private class Errors {
        String uId;

        Errors(String uId){
            this.uId = uId;
        }

        public String getuId() {
            return uId;
        }

        public void setuId(String uId) {
            this.uId = uId;
        }

    }
}


