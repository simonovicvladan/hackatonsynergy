package rs.yettel.bms.routes

import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import rs.yettel.bms.firebase.FirebaseService
import rs.yettel.bms.repositories.UserRepository

fun Route.notificationRoutes() {

    post("/register-fcm-token") {
        val request = call.receive<RegisterFcmTokenRequest>()
        val updatedRows = UserRepository.updateUserFcmToken(request.email, request.token)
        if (updatedRows) {
            call.respondText("FCM token registered successfully for user ${request.email}")
        } else {
            call.respond(NotFound, "User ${request.email} not found")
        }
    }

    post("/send-notification") {
        val request = call.receive<NotificationRequest>()

        val token = request.message.token
        val title = request.message.notification.title
        val body = request.message.notification.body
        val data = request.message.data

        val responseId = FirebaseService.sendNotificationToToken(
            token = token,
            title = title,
            body = body,
            data = data
        )
        call.respondText("Notification sent successfully! Response ID: $responseId")
    }
}

@Serializable
data class NotificationRequest(val message: Message)

@Serializable
data class Message(
    val token: String,
    val notification: NotificationContent,
    val data: Map<String, String>? = null
)

@Serializable
data class NotificationContent(
    val title: String,
    val body: String
)

@Serializable
data class RegisterFcmTokenRequest(val email: String, val token: String)