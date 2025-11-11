package rs.yettel.bms.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import rs.yettel.bms.firebase.FirebaseService
import rs.yettel.bms.repositories.ScanUpdateResult
import rs.yettel.bms.repositories.UserRepository
import rs.yettel.bms.routes.QrCodeScanRequest
import rs.yettel.bms.routes.ScanResult

class QrCodeScanService(
    private val userRepository: UserRepository,
    private val firebaseService: FirebaseService,
    private val scannerPoints: Int = 2000,
    private val scaneePoints: Int = 1000
) {
    private val logger = LoggerFactory.getLogger(QrCodeScanService::class.java)
    private val notificationScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun processScan(request: QrCodeScanRequest): ScanResult {
        return try {
            if (userRepository.hasScannedQrCode(request.scannerEmail, request.scaneeEmail)) {
                return ScanResult.AlreadyScanned("You have already scanned this QR code")
            }

            val result = userRepository.addPointsToUsersAndGetFcmTokensForNotifications(
                scannerEmail = request.scannerEmail,
                scaneeEmail = request.scaneeEmail,
                scannerPoints = scannerPoints,
                scaneePoints = scaneePoints
            )

            when {
                !result.scannerExists -> {
                    ScanResult.UserNotFound("Scanner user not found: ${request.scannerEmail}")
                }

                !result.scaneeExists -> {
                    ScanResult.UserNotFound("Scanee user not found: ${request.scaneeEmail}")
                }

                else -> {
                    sendNotificationsAsync(request, result)
                    ScanResult.Success(scannerPoints, scaneePoints)
                }
            }
        } catch (e: Exception) {
            logger.error("Error processing QR scan", e)
            ScanResult.Error("Failed to process scan", e)
        }
    }

    private fun sendNotificationsAsync(request: QrCodeScanRequest, result: ScanUpdateResult) {
        notificationScope.launch {
            result.scaneeToken?.let { token ->
                try {
                    firebaseService.sendNotificationToToken(
                        token = token,
                        title = "You earned points!",
                        body = "You got $scaneePoints points from your scanner ${request.scannerEmail}",
                        data = mapOf(
                            "type" to "scanee",
                            "scanner" to request.scannerEmail,
                            "scanee" to request.scaneeEmail,
                            "points" to scaneePoints.toString()
                        )
                    )
                } catch (e: Exception) {
                    logger.error("Failed to send notification to scanee", e)
                }
            }

            result.scannerToken?.let { token ->
                try {
                    firebaseService.sendNotificationToToken(
                        token = token,
                        title = "You earned points!",
                        body = "You got $scannerPoints points for scanning ${request.scaneeEmail}",
                        data = mapOf(
                            "type" to "scanner",
                            "scanner" to request.scannerEmail,
                            "scanee" to request.scaneeEmail,
                            "points" to scannerPoints.toString()
                        )
                    )
                } catch (e: Exception) {
                    logger.error("Failed to send notification to scanner", e)
                }
            }
        }
    }
}