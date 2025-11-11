package rs.yettel.bms.repositories

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import rs.yettel.bms.db.Users
import rs.yettel.bms.models.User

import kotlinx.serialization.json.Json

object UserRepository {

    fun findAll(): List<User> = transaction {
        Users.selectAll()
            .map(::toUser)
    }

    fun findByMsisdn(msisdn: Long): User? = transaction {
        Users.selectAll()
            .where { Users.msisdn eq msisdn }
            .map { toUser(it) }
            .singleOrNull()
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
        scannedPoints = row[Users.scannedPoints]?.let {
            try {
                Json.decodeFromString<List<String>>(it)
            } catch (e: Exception) {
                null
            }
        }
    )
}
