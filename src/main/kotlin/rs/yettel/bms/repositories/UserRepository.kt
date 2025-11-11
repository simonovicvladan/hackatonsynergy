package rs.yettel.bms.repositories

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
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

    fun addPointsToUsersAndGetFcmTokens(
        scannerEmail: String,
        scaneeEmail: String,
        scannerPoints: Int = 2000,
        scaneePoints: Int = 1000,
        qrCode: String? = null
    ): ScanUpdateResult = transaction {

        val scaneeRow = Users.selectAll()
            .where { Users.email eq scaneeEmail }
            .singleOrNull()

        val scaneeToken = scaneeRow?.get(Users.fcmToken)
        val scaneeExists = scaneeRow != null

        scaneeRow?.let { row ->
            updateUserPoints(scaneeEmail, row, scaneePoints, qrCode)
        }

        val scannerRow = Users.selectAll()
            .where { Users.email eq scannerEmail }
            .singleOrNull()

        val scannerToken = scannerRow?.get(Users.fcmToken)
        val scannerExists = scannerRow != null

        scannerRow?.let { row ->
            updateUserPoints(scannerEmail, row, scannerPoints, qrCode)
        }

        ScanUpdateResult(
            scannerToken = scannerToken,
            scaneeToken = scaneeToken,
            scannerExists = scannerExists,
            scaneeExists = scaneeExists
        )
    }

    private fun updateUserPoints(
        email: String,
        row: ResultRow,
        points: Int,
        qrCode: String?
    ) {
        val currentPoints = row[Users.currentPointsAmount] ?: 0
        val existingQrCodes = row[Users.scannedQrCodes] ?: emptyList()
        val newQrCodes = if (qrCode != null && !existingQrCodes.contains(qrCode)) {
            existingQrCodes + qrCode
        } else {
            existingQrCodes
        }

        Users.update({ Users.email eq email }) {
            it[currentPointsAmount] = currentPoints + points
            it[scannedQrCodes] = newQrCodes
        }
    }

    fun hasScannedQrCode(email: String, qrCode: String): Boolean = transaction {
        Users.selectAll()
            .where { Users.email eq email }
            .singleOrNull()
            ?.get(Users.scannedQrCodes)
            ?.contains(qrCode) ?: false
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
