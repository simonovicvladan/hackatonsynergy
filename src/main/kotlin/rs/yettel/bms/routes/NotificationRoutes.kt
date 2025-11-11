package rs.yettel.bms.routes

import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import rs.yettel.bms.firebase.FirebaseService
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import rs.yettel.bms.db.Users

fun Route.notificationRoutes() {

    post("/send-notification") {
        val request = call.receive<NotificationRequest>()

        val responseId = FirebaseService.sendNotificationToToken(
            token = request.token,
            title = request.title,
            body = request.body
        )

        call.respondText("Notification sent successfully! Response ID: $responseId")
    }

    post("/register-token") {
        val request = call.receive<RegisterTokenRequest>()

        val updatedRows = transaction {
            Users.update({ Users.msisdn eq request.msisdn }) {
                it[fcmToken] = request.token
            }
        }

        if (updatedRows > 0) {
            call.respondText("Token registered successfully for user ${request.msisdn}")
        } else {
            call.respondText(
                "User with msisdn ${request.msisdn} not found",
                status = NotFound
            )
        }
    }

    post("/send-to-user") {
        val request = call.receive<SendToUserRequest>()

        val token = transaction {
            Users
                .select(Users.fcmToken)
                .where { Users.msisdn eq request.msisdn }
                .map { it[Users.fcmToken] }
                .firstOrNull()
        }

        if (token == null) {
            call.respondText(
                "User ${request.msisdn} does not register FCM token.",
                status = NotFound
            )
            return@post
        }

        val responseId = FirebaseService.sendNotificationToToken(
            token = token,
            title = request.title,
            body = request.body
        )

        call.respondText("Notification sent to user ${request.msisdn}. Response ID: $responseId")
    }
}

@Serializable
data class NotificationRequest(
    val token: String,
    val title: String,
    val body: String
)

@Serializable
data class RegisterTokenRequest(
    val msisdn: Long,
    val token: String
)

@Serializable
data class SendToUserRequest(
    val msisdn: Long,
    val title: String,
    val body: String
)