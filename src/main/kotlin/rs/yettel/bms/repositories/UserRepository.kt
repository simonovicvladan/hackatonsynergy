package rs.yettel.bms.repositories

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import rs.yettel.bms.db.Users
import rs.yettel.bms.models.User
import rs.yettel.bms.routes.RegisterTokenRequest

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

    fun registerFcmToken(request: RegisterTokenRequest): Boolean = transaction {
        Users.update({ Users.email eq request.email }) { stmt ->
            request.token.let { v -> stmt[fcmToken] = v }
        } > 0
    }

    fun addPointsToUsersAndGetFcmTokens(
        scannerEmail: String,
        scaneeEmail: String,
        scannerPoints: Int = 2000,
        scaneePoints: Int = 1000,
        qrCode: String? = null
    ): Pair<String?, String?> = transaction {

        val scaneeRow = Users.selectAll().where { Users.email eq scaneeEmail }.singleOrNull()
        val scaneeToken = scaneeRow?.get(Users.fcmToken)
        if (scaneeRow != null) {
            val existingQrCodes = scaneeRow[Users.scannedQrCodes] ?: emptyList()
            val newQrCodes = if (qrCode != null) existingQrCodes + qrCode else existingQrCodes
            val currentPoints = scaneeRow[Users.currentPointsAmount] ?: 0

            Users.update({ Users.email eq scaneeEmail }) {
                it[currentPointsAmount] = currentPoints + scaneePoints
                it[scannedQrCodes] = newQrCodes
            }
        }

        val scannerRow = Users.selectAll().where { Users.email eq scannerEmail }.singleOrNull()
        val scannerToken = scannerRow?.get(Users.fcmToken)
        if (scannerRow != null) {
            val existingQrCodes = scannerRow[Users.scannedQrCodes] ?: emptyList()
            val newQrCodes = if (qrCode != null) existingQrCodes + qrCode else existingQrCodes
            val currentPoints = scannerRow[Users.currentPointsAmount] ?: 0

            Users.update({ Users.email eq scannerEmail }) {
                it[currentPointsAmount] = currentPoints + scannerPoints
                it[scannedQrCodes] = newQrCodes
            }
        }

        Pair(scannerToken, scaneeToken)
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
