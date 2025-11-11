package rs.yettel.bms.firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.slf4j.LoggerFactory
import java.io.FileInputStream

private val logger = LoggerFactory.getLogger("FirebaseAdmin")

object FirebaseAdmin {
    private var initialized = false

    fun init() {
        if (initialized) return

        val serviceAccountPath = "src/main/resources/firebase/serviceAccountKey.json"

        val serviceAccount = FileInputStream(serviceAccountPath)
        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build()

        FirebaseApp.initializeApp(options)
        initialized = true
        logger.info("Firebase initialized successfully")
    }
}