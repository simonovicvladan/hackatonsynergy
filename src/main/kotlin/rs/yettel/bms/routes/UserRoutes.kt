package rs.yettel.bms.routes

import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import rs.yettel.bms.repositories.RewardRepository
import rs.yettel.bms.repositories.UserRepository

fun Route.userRoutes() {

    route("/users") {

        get {
            val users = UserRepository.findAll()
            call.respond(users)
        }

        get("{email}") {
            val email = call.parameters["email"] ?: ""
            val user = UserRepository.findByEmail(email)
            if (user == null) {
                call.respondText("User not found", status = NotFound)
            } else {
                call.respond(user)
            }
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

        get("{userId}/rewards") {
            val id = call.parameters["userId"]
            val userId = id?.toLongOrNull()

            if (userId == null) {
                call.respond(BadRequest, mapOf("error" to "Invalid userId"))
                return@get
            }

            val awards = RewardRepository.findAvailableForUser(userId)
            call.respond(OK, awards)
        }

        post("{userId}/claim-reward/{rewardId}") {
            val userId = call.parameters["userId"]?.toLongOrNull()
            val rewardId = call.parameters["rewardId"]?.toLongOrNull()

            if (userId == null || rewardId == null) {
                call.respond(BadRequest, ErrorResponse("Invalid userId or rewardId"))
                return@post
            }

            val (success, error) = RewardRepository.claimReward(userId, rewardId)
            if (!success) {
                call.respond(BadRequest, ErrorResponse(error ?: "Failed to claim reward"))
                return@post
            }

            val remainingPoints = UserRepository.findById(userId)?.currentPointsAmount ?: 0

            call.respond(
                OK, ClaimRewardResponse(
                    message = "Reward claimed successfully",
                    remainingPoints = remainingPoints
                )
            )
        }
    }
}

@Serializable
data class LoginRequest(val email: String, val password: String, val token: String)

@Serializable
data class ClaimRewardResponse(
    val message: String,
    val remainingPoints: Int
)