package vn.edu.smd.core.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Firebase Cloud Messaging Configuration
 * Initializes Firebase Admin SDK for sending push notifications
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.service-account-path:./firebase-service-account.json}")
    private String serviceAccountPath;

    @Value("${firebase.enabled:true}")
    private boolean firebaseEnabled;

    @PostConstruct
    public void initialize() {
        if (!firebaseEnabled) {
            log.warn("Firebase is disabled in configuration");
            return;
        }

        try {
            // Check if Firebase is already initialized
            if (!FirebaseApp.getApps().isEmpty()) {
                log.info("Firebase already initialized");
                return;
            }

            // Load service account credentials
            FileInputStream serviceAccount = new FileInputStream(serviceAccountPath);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            
            log.info("✅ Firebase Admin SDK initialized successfully");
            log.info("📱 Push notifications are enabled for all roles");

        } catch (IOException e) {
            log.error("❌ Failed to initialize Firebase: {}", e.getMessage());
            log.error("💡 Ensure firebase-service-account.json exists at: {}", serviceAccountPath);
            log.error("💡 Download it from: Firebase Console > Project Settings > Service Accounts");
        }
    }
}
