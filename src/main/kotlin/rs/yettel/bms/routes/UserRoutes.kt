package rs.yettel.bms.routes

import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.UnprocessableEntity
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import rs.yettel.bms.dto.ErrorResponse
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
                return@get
            }
            call.respond(user)
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val user = UserRepository.findByEmail(request.email)
            if (user == null) {
                call.respondText("User not found", status = NotFound)
                return@post
            }
            call.respond(user)
        }

        get("{userId}/rewards") {
            val id = call.parameters["userId"]
            val userId = id?.toLongOrNull()
            if (userId == null) {
                call.respond(BadRequest, ErrorResponse("Invalid userId"))
                return@get
            }
            val awards = RewardRepository.findAvailableForUser(userId)
            call.respond(awards)
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
                call.respond(UnprocessableEntity, ErrorResponse(error ?: "Failed to claim reward"))
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
data class LoginRequest(val email: String, val password: String)

@Serializable
data class ClaimRewardResponse(val message: String, val remainingPoints: Int)