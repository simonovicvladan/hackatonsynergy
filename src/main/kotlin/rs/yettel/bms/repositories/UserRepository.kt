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

        scaneeRow?.let { row ->
            updateUserPoints(row, scaneePoints)
        }
        scannerRow?.let { row ->
            updateUserPoints(row, scannerPoints)
            updateScannerScannedQrCodes(scaneeEmail, row)
        }

        ScanUpdateResult(
            scannerToken = scannerToken,
            scaneeToken = scaneeToken,
            scannerExists = scannerExists,
            scaneeExists = scaneeExists
        )
    }

    private fun updateUserPoints(row: ResultRow, points: Int) {
        val currentPoints = row[Users.currentPointsAmount] ?: 0
        Users.update({ Users.email eq row[Users.email] }) {
            it[currentPointsAmount] = currentPoints + points
        }
    }

    private fun updateScannerScannedQrCodes(scannerEmail: String, row: ResultRow) {
        val existingQrCodes = row[Users.scannedQrCodes] ?: emptyList()
        val newQrCodes = if (!existingQrCodes.contains(scannerEmail)) {
            existingQrCodes + scannerEmail
        } else {
            existingQrCodes
        }
        Users.update({ Users.email eq row[Users.email] }) {
            it[scannedQrCodes] = newQrCodes
        }
    }

    fun hasScannedQrCode(scannerEmail: String, scaneeEmail: String): Boolean = transaction {
        Users.selectAll()
            .where { Users.email eq scannerEmail }
            .singleOrNull()
            ?.get(Users.scannedQrCodes)
            ?.contains(scaneeEmail) ?: false
    }

    fun updateUserOfferPoints(email: String, points: Int) = transaction {
        val scannerRow = Users
            .selectAll()
            .where { Users.email eq email }
            .singleOrNull()

        scannerRow?.let { row ->
            updateUserPoints(row, points)
        }
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
