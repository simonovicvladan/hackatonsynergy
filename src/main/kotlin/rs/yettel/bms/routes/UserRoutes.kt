package rs.yettel.bms.routes

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
import rs.yettel.bms.repositories.UserOfferRepository
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

        get("{email}/rewards") {
            val email = call.parameters["email"] ?: ""
            val user = UserRepository.findByEmail(email)
            if (user == null) {
                call.respondText("User not found", status = NotFound)
                return@get
            }
            val awards = RewardRepository.findAvailableForUser(user.subscriberId)
            call.respond(awards)
        }

        post("{email}/claim-reward/{rewardId}") {
            val email = call.parameters["email"] ?: ""
            val user = UserRepository.findByEmail(email)
            if (user == null) {
                call.respondText("User not found", status = NotFound)
                return@post
            }
            val rewardId = call.parameters["rewardId"]?.toLongOrNull()

            val (success, error) = RewardRepository.claimReward(user.subscriberId!!, rewardId!!)
            if (!success) {
                call.respond(UnprocessableEntity, ErrorResponse(error ?: "Failed to claim reward"))
                return@post
            }

            val remainingPoints = UserRepository.findById(user.subscriberId)?.currentPointsAmount ?: 0

            call.respond(
                OK, ClaimRewardResponse(
                    message = "Reward claimed successfully",
                    remainingPoints = remainingPoints
                )
            )
        }

        get("{email}/offers") {
            val email = call.parameters["email"] ?: ""
            val offer = UserOfferRepository.findUnclaimedOffers(email)
            call.respond(offer)
        }
    }
}

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class ClaimRewardResponse(val message: String, val remainingPoints: Int)