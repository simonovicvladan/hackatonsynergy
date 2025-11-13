package rs.yettel.bms.repositories

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import rs.yettel.bms.db.Users
import rs.yettel.bms.models.User

object UserRepository {

    fun findAll(): List<User> = transaction {
        Users.selectAll()
            .map(::toUser)
    }

    fun findByEmail(email: String): User? = transaction {
        Users.selectAll()
            .where { Users.email eq email }
            .map(::toUser)
            .singleOrNull()
    }

    fun updateUserFcmToken(email: String, token: String): Boolean = transaction {
        Users.update({ Users.email eq email }) { stmt ->
            token.let { v -> stmt[fcmToken] = v }
        } > 0
    }

    fun addPointsToUsersAndGetFcmTokensForNotifications(
        scannerEmail: String,
        scaneeEmail: String,
        scannerPoints: Int = 300,
        scaneePoints: Int = 100,
    ): ScanUpdateResult = transaction {

        val scaneeRow = Users.selectAll()
            .where { Users.email eq scaneeEmail }
            .singleOrNull()
        val scaneeToken = scaneeRow?.get(Users.fcmToken)
        val scaneeExists = scaneeRow != null

        val scannerRow = Users.selectAll()
            .where { Users.email eq scannerEmail }
            .singleOrNull()
        val scannerToken = scannerRow?.get(Users.fcmToken)
        val scannerExists = scannerRow != null

        scaneeRow?.let { row -> updateUserPointsAndScannedQrCodes(scannerEmail, row, scaneePoints) }
        scannerRow?.let { row -> updateUserPointsAndScannedQrCodes(scaneeEmail, row, scannerPoints) }

        ScanUpdateResult(
            scannerToken = scannerToken,
            scaneeToken = scaneeToken,
            scannerExists = scannerExists,
            scaneeExists = scaneeExists
        )
    }

    private fun updateUserPointsAndScannedQrCodes(qrCodeEmail: String, row: ResultRow, points: Int) {
        val currentPoints = row[Users.currentPointsAmount] ?: 0
        val existingQrCodes = row[Users.scannedQrCodes] ?: emptyList()
        val newQrCodes = if (!existingQrCodes.contains(qrCodeEmail)) {
            existingQrCodes + qrCodeEmail
        } else {
            existingQrCodes
        }
        Users.update({ Users.email eq row[Users.email] }) {
            it[currentPointsAmount] = currentPoints + points
            it[scannedQrCodes] = newQrCodes
        }
    }

    fun hasScannedQrCode(scannerEmail: String, scaneeEmail: String): Boolean = transaction {
        Users.selectAll()
            .where { Users.email eq scaneeEmail }
            .singleOrNull()
            ?.get(Users.scannedQrCodes)
            ?.contains(scannerEmail) ?: false
    }

    private fun toUser(row: ResultRow): User = User(
        msisdn = row[Users.msisdn],
        subscriberId = row[Users.subscriberId],
        uuid = row[Users.uuid],
        name = row[Users.name],
        email = row[Users.email],
        age = row[Users.age],
        tariffPackage = row[Users.tariffPackage],
        category = row[Users.category],
        currentPointsAmount = row[Users.currentPointsAmount],
        scannedQrCodes = row[Users.scannedQrCodes],
        fcmToken = row[Users.fcmToken]
    )
}

data class ScanUpdateResult(
    val scannerToken: String?,
    val scaneeToken: String?,
    val scannerExists: Boolean,
    val scaneeExists: Boolean
)
