package rs.yettel.bms.routes

import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import rs.yettel.bms.repositories.UserRepository

fun Route.userRoutes() {

    route("/users") {

        get {
            val users = UserRepository.findAll()
            call.respond(users)
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val user = UserRepository.findByEmail(request.email)
            if (user == null) {
                call.respondText("User not found", status = NotFound)
                return@post
            }

            val updatedRows = UserRepository.updateUserFcmToken(user.email ?: "", request.token)
            if (updatedRows) {
                user.fcmToken = request.token
                call.respond(user)
            } else {
                call.respondText("Failed to update FCM token", status = InternalServerError)
            }
        }
    }
}

@Serializable
data class LoginRequest(val email: String, val password: String, val token: String)