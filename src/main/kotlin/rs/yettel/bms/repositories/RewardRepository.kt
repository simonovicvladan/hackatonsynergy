package rs.yettel.bms.repositories

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import rs.yettel.bms.db.Rewards
import rs.yettel.bms.db.Users
import rs.yettel.bms.models.Reward

object RewardRepository {

    fun findAll(): List<Reward> = transaction {
        Rewards.selectAll().map(RewardRepository::toReward)
    }

    fun findById(id: Long): Reward? = transaction {
        Rewards.selectAll()
            .where { Rewards.id eq id }
            .map(RewardRepository::toReward)
            .singleOrNull()
    }

    fun findAvailableForUser(id: Long): List<Reward> = transaction {
        Rewards.selectAll()
            .map { toReward(it) }
            .filter { id !in it.usedByUsers }
    }

    fun claimReward(userId: Long, rewardId: Long): Pair<Boolean, String?> = transaction {
        val userRow = Users.selectAll().where { Users.subscriberId eq userId }.singleOrNull()
            ?: return@transaction false to "User not found"

        val rewardRow = Rewards.selectAll().where { Rewards.id eq rewardId }.singleOrNull()
            ?: return@transaction false to "Reward not found"

        val currentPoints = userRow[Users.currentPointsAmount] ?: 0
        val rewardPoints = rewardRow[Rewards.points]

        val eligibleUsers = rewardRow[Rewards.eligibleUsers]
        val usedByUsers = rewardRow[Rewards.usedByUsers]

        if (userId !in eligibleUsers) return@transaction false to "User not eligible for this reward"
        if (userId in usedByUsers) return@transaction false to "Reward already claimed by this user"
        if (currentPoints < rewardPoints) return@transaction false to "Insufficient points"

        Users.update({ Users.subscriberId eq userId }) {
            it[Users.currentPointsAmount] = currentPoints - rewardPoints
        }

        val newUsedBy = usedByUsers + userId
        Rewards.update({ Rewards.id eq rewardId }) {
            it[Rewards.usedByUsers] = newUsedBy
        }

        return@transaction true to null
    }

    fun delete(id: Long): Boolean = transaction {
        Rewards.deleteWhere { Rewards.id eq id } > 0
    }

    private fun toReward(row: ResultRow): Reward = Reward(
        id = row[Rewards.id],
        rewardName = row[Rewards.rewardName],
        points = row[Rewards.points],
        eligibleUsers = row[Rewards.eligibleUsers],
        usedByUsers = row[Rewards.usedByUsers]
    )
}