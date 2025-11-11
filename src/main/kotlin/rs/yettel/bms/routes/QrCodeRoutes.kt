package rs.yettel.bms.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import rs.yettel.bms.firebase.FirebaseService
import rs.yettel.bms.repositories.UserRepository

fun Route.qrCodeRoutes() {
    route("/qrcode-scan") {
        post {
            val request = call.receive<QrCodeScanRequest>()

            try {
                val (scannerToken, scaneeToken) = UserRepository.addPointsToUsersAndGetFcmTokens(
                    scannerEmail = request.scannerEmail,
                    scaneeEmail = request.scaneeEmail,
                    qrCode = request.qrCode
                )

                CoroutineScope(Dispatchers.IO).launch {
                    scaneeToken?.let { token ->
                        FirebaseService.sendNotificationToToken(
                            token = token,
                            title = "You earned points!",
                            body = "You received 1000 points for being scanned by ${request.scannerEmail}",
                            data = mapOf(
                                "type" to "scanee",
                                "scanner" to request.scannerEmail,
                                "scanee" to request.scaneeEmail,
                                "points" to "1000"
                            )
                        )
                    }

                    scannerToken?.let { token ->
                        FirebaseService.sendNotificationToToken(
                            token = token,
                            title = "You earned points!",
                            body = "You received 2000 points for scanning ${request.scaneeEmail}",
                            data = mapOf(
                                "type" to "scanner",
                                "scanner" to request.scannerEmail,
                                "scanee" to request.scaneeEmail,
                                "points" to "2000"
                            )
                        )
                    }
                }

                call.respondText("QR code scan processed. Notifications are being sent asynchronously.")
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    status = io.ktor.http.HttpStatusCode.InternalServerError,
                    message = mapOf("error" to (e.message ?: "Unknown error"))
                )
            }
        }
    }
}

@Serializable
data class QrCodeScanRequest(
    val scannerEmail: String,
    val scaneeEmail: String,
    val qrCode: String? = null
)