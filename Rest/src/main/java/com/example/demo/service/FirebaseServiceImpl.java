package com.example.demo.service;

import com.example.demo.model.Client;
import com.example.demo.model.Model;
import com.example.demo.model.Parent;
import com.example.demo.model.Post;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class FirebaseServiceImpl implements FirebaseService{
    public String saveDetails(Parent parent,String type) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection(type).document(parent.getId().toString()).set(parent);
        return collectionsApiFuture.get().getUpdateTime().toString();
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
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection(type).document(parent.getId().toString()).set(parent);
        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    @Override
    public void delete(Parent parent,String type){
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> writeResult = dbFirestore.collection(type).document(parent.getId().toString()).delete();
    }

}
