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
import rs.yettel.bms.repositories.UserOfferRepository
import rs.yettel.bms.repositories.UserRepository

fun Route.userRoutes() {

    route("/users") {

        get {
            val users = UserRepository.findAll()
            call.respond(users)
        }

        get("{email}") {
            val email = call.parameters["email"]!!
            val user = UserRepository.findByEmail(email)
            if (user == null) {
                call.respond(NotFound, ErrorResponse("User not found"))
                return@get
            }
            call.respond(user)
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val user = UserRepository.findByEmail(request.email)
            if (user == null) {
                call.respond(NotFound, ErrorResponse("User ${request.email} not found"))
                return@post
            }
            call.respond(user)
        }

        get("{email}/rewards") {
            val email = call.parameters["email"]!!
            val user = UserRepository.findByEmail(email)
            if (user == null) {
                call.respond(NotFound, ErrorResponse("User $email not found"))
                return@get
            }
            val awards = RewardRepository.findAvailableForUser(user.msisdn)
            call.respond(awards)
        }

        get("{email}/claimed-rewards") {
            val email = call.parameters["email"]!!
            val user = UserRepository.findByEmail(email)
            if (user == null) {
                call.respond(NotFound, ErrorResponse("User $email not found"))
                return@get
            }
            val claimedRewards = RewardRepository.findClaimedForUser(user.msisdn)
            call.respond(claimedRewards)
        }

        post("{email}/claim-reward/{rewardId}") {
            val email = call.parameters["email"]!!
            val user = UserRepository.findByEmail(email)
            if (user == null) {
                call.respond(NotFound, ErrorResponse("User $email not found"))
                return@post
            }
            val rewardId = call.parameters["rewardId"]?.toLongOrNull()
            if (rewardId == null) {
                call.respond(BadRequest, ErrorResponse("rewardId must not be null"))
                return@post
            }
            val (success, error) = RewardRepository.claimReward(user.msisdn, rewardId)
            if (!success) {
                call.respond(UnprocessableEntity, ErrorResponse(error ?: "Failed to claim reward"))
                return@post
            }

            val remainingPoints = UserRepository.findByEmail(email)?.currentPointsAmount ?: 0

            call.respond(
                OK, ClaimRewardResponse(
                    message = "Reward claimed successfully",
                    remainingPoints = remainingPoints
                )
            )
        }

        get("{email}/offers") {
            val email = call.parameters["email"]!!
            val offer = UserOfferRepository.findUnclaimedOffers(email)
            call.respond(offer)
        }

        post("{email}/claim-offer/{offerId}") {
            val scaneeEmail = call.parameters["email"]!!
            val offerId = call.parameters["offerId"]!!.toLongOrNull()
            val scannerEmail = UserOfferRepository.findScannerEmailByOfferId(offerId!!)

            if (scannerEmail == null) {
                call.respond(NotFound, ErrorResponse("For scanee $scaneeEmail, Scanner Email not found"))
                return@post
            }

            val offer = UserOfferRepository.findById(offerId)
            if (offer == null) {
                call.respond(NotFound, ErrorResponse("Offer $offerId not found"))
                return@post
            }
            UserRepository.updateUserOfferPoints(scaneeEmail, offer.points)
            UserRepository.updateUserOfferPoints(scannerEmail, offer.points)

            UserOfferRepository.updateUserOfferClaim(scaneeEmail)

            call.respond(OK, ClaimOfferResponse(message = "Offer claimed successfully"))
        }
    }
}

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class ClaimRewardResponse(val message: String, val remainingPoints: Int)


@Serializable
data class ClaimOfferResponse(val message: String)