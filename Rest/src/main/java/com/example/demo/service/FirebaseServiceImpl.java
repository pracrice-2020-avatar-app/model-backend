package com.example.demo.service;

import com.example.demo.model.Client;
import com.example.demo.model.Model;
import com.example.demo.model.Parent;
import com.example.demo.model.Post;
import com.google.api.gax.paging.Page;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.cloud.StorageClient;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Paths;
import java.util.Arrays;
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
        InputStream File = new FileInputStream("mvg-output/output_set" + id  + "/reconstruction_sequential/mvs_sequential/scene_dense_mesh_texture_900.png");
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

}
