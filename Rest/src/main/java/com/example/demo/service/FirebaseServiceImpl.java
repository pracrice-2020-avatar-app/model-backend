package com.example.demo.service;

import com.example.demo.model.Client;
import com.example.demo.model.Parent;
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
    public String saveClientDetails(Client client) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection("clients").document(client.getId().toString()).set(client);
        return collectionsApiFuture.get().getUpdateTime().toString();
    }
    public Client getClientDetails(Integer id) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection("clients").document(id.toString());
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot document = future.get();
        Client client = null;
        if (document.exists()){
            client = document.toObject(Client.class);
            return client;
            }
        return null;

    }

    public void includeClients(ClientService clientService){
        Firestore dbFirestore = FirestoreClient.getFirestore();
        dbFirestore.collection("clients").listDocuments().forEach(client -> {
            try {
                int maxId = client.get().get().toObject(Client.class).getId();
                if (clientService.getMaxId() < maxId)
                    clientService.setMaxId(maxId - 1);
                clientService.create(client.get().get().toObject(Client.class));
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
