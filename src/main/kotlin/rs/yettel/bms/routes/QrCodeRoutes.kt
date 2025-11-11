package rs.yettel.bms.routes

import io.ktor.http.HttpStatusCode.Companion.Accepted
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import rs.yettel.bms.services.QrCodeScanService

private val logger = LoggerFactory.getLogger("QrCodeRoutes")

fun Route.qrCodeRoutes(scanService: QrCodeScanService) {
    route("/qrcode-scan") {
        post {
            val request = call.receive<QrCodeScanRequest>()
            if (request.scannerEmail.isBlank() || request.scaneeEmail.isBlank()) {
                call.respond(BadRequest, ErrorResponse("Scanee and Scanner emails are required"))
                return@post
            }
            if (request.scannerEmail == request.scaneeEmail) {
                call.respond(BadRequest, ErrorResponse("Cannot scan your own QR code"))
                return@post
            }

            when (val result = scanService.processScan(request)) {
                is ScanResult.Success -> call.respond(
                        Accepted,
                        QrCodeScanResponse(
                            message = "QR code scan processed successfully",
                            scannerPoints = result.scannerPoints,
                            scaneePoints = result.scaneePoints
                        )
                    )

                is ScanResult.UserNotFound -> {
                    call.respond(NotFound, ErrorResponse(result.message))
                }

                is ScanResult.AlreadyScanned -> {
                    call.respond(Conflict, ErrorResponse(result.message))
                }

                is ScanResult.Error -> {
                    logger.error("QR scan error: ${result.message}", result.exception)
                    call.respond(InternalServerError, ErrorResponse(result.message))
                }
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

@Serializable
data class QrCodeScanResponse(
    val message: String,
    val scannerPoints: Int,
    val scaneePoints: Int
)

@Serializable
data class ErrorResponse(val error: String)

sealed class ScanResult {
    data class Success(val scannerPoints: Int, val scaneePoints: Int) : ScanResult()
    data class UserNotFound(val message: String) : ScanResult()
    data class AlreadyScanned(val message: String) : ScanResult()
    data class Error(val message: String, val exception: Exception? = null) : ScanResult()
}