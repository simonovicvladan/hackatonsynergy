package rs.yettel.bms.routes

import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import rs.yettel.bms.repositories.UserRepository

@Serializable
data class LoginRequest(val msisdn: Long)

fun Route.userRoutes() {

    route("/users") {

        get {
            val users = UserRepository.getAllUsers()
            call.respond(users)
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val user = UserRepository.getUserByMsisdn(request.msisdn)

            if (user != null) {
                call.respond(user)
            } else {
                call.respondText("User not found", status = NotFound)
            }
        }
    }
}