package com.example.demo.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.cloud.StorageClient;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

@Service
public class FirebaseInitialize {

    public void init() throws IOException {
        try {
            FileInputStream serviceAccount = new FileInputStream("vr-chat-mobile-firebase-adminsdk-n48fu-af6632ceaa.json");
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://vr-chat-mobile.firebaseio.com")
                    .setStorageBucket("vr-chat-mobile.appspot.com")
                    .build();

            FirebaseApp.initializeApp(options);
            Firestore db = FirestoreClient.getFirestore();
            StorageClient storageClient = StorageClient.getInstance();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
