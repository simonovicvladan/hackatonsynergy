package rs.yettel.bms.firebase

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification

object FirebaseService {
    fun sendNotificationToToken(
        token: String,
        title: String,
        body: String,
        data: Map<String, String>? = null
    ): String {
        val messageBuilder = Message.builder()
            .setToken(token)
            .setNotification(
                Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build()
            )

        data?.let { messageBuilder.putAllData(it) }

        val message = messageBuilder.build()
        return FirebaseMessaging.getInstance().send(message)
    }
}