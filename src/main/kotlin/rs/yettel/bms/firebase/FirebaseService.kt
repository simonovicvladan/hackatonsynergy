package rs.yettel.bms.firebase

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("FirebaseService")

object FirebaseService {
    fun sendNotificationToToken(
        token: String,
        title: String,
        body: String,
        data: Map<String, String>? = null
    ): Result<String> {
        return try {
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
            val messageId = FirebaseMessaging.getInstance().send(message)

            logger.info("Notification for device token $token sent successfully. Message ID: $messageId")
            Result.success(messageId)

        } catch (e: FirebaseMessagingException) {
            logger.error("Firebase notification failed: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            logger.error("Unexpected error sending notification: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun sendNotificationToMultipleTokens(
        tokens: List<String>,
        title: String,
        body: String,
        data: Map<String, String>? = null
    ): Map<String, Result<String>> {
        return tokens.associateWith { token ->
            sendNotificationToToken(token, title, body, data)
        }
    }
}