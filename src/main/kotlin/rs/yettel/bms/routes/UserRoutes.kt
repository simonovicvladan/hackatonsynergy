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
import rs.yettel.bms.firebase.FirebaseService
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
            val user = UserRepository.findByEmail(email)
            if (user == null) {
                call.respond(NotFound, ErrorResponse("User $email not found"))
                return@get
            }
            val offer = UserOfferRepository.findUnclaimedOffers(email)
            call.respond(offer)
        }

        // TODO rename database column scanner_email to scanee_email

        post("{email}/claim-offer/{offerId}") {
            val scannerEmail = call.parameters["email"]!!
            val offerId = call.parameters["offerId"]!!.toLongOrNull()
            val scannerOffer = UserOfferRepository.findScaneeEmailByScannerEmailAndOfferId(scannerEmail, offerId!!)

            if (scannerOffer == null) {
                call.respond(NotFound, ErrorResponse("For scanner $scannerEmail, scanee email or offer not found"))
                return@post
            }
            UserRepository.updateUserOfferPoints(scannerOffer.email, scannerOffer.points)
            UserRepository.updateUserOfferPoints(scannerOffer.scannerEmail!!, scannerOffer.points)

            UserOfferRepository.updateUserOfferClaim(scannerOffer.email, offerId)

            val scanner = UserRepository.findByEmail(scannerEmail)
            val scanee = UserRepository.findByEmail(scannerOffer.scannerEmail)
            if (scanee == null || scanner == null) {
                call.respond(NotFound, ErrorResponse("User ${scannerOffer.email}/${scannerEmail} not found"))
                return@post
            }

            val tokens = listOfNotNull(
                scanee.fcmToken,
                scanner.fcmToken
            )
            FirebaseService.sendNotificationToMultipleTokens(
                tokens = tokens,
                title = "You earned ${scannerOffer.points} for accepting offer",
                body = "Offer ${scannerOffer.offerName} successfully claimed! Check points balance",
                data = mapOf(
                    "type" to "scanee",
                    "scanner" to scanner.name,
                    "scanee" to scanee.name,
                    "points" to scannerOffer.points.toString()
                )
            )

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