package rs.yettel.bms.repositories

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
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

    fun findAvailableForUser(msisdn: Long): List<Reward> = transaction {
        Rewards.selectAll()
            .map(::toReward)
            .filter { msisdn !in it.usedByUsers }
    }

    fun findClaimedForUser(msisdn: Long): List<Reward> = transaction {
        Rewards.selectAll()
            .map(::toReward)
            .filter { msisdn in it.usedByUsers }
    }

    fun claimReward(msisdn: Long, rewardId: Long): Pair<Boolean, String?> = transaction {
        val userRow = Users.selectAll().where { Users.msisdn eq msisdn }.singleOrNull()
            ?: return@transaction false to "User not found"

        val rewardRow = Rewards.selectAll().where { Rewards.id eq rewardId }.singleOrNull()
            ?: return@transaction false to "Reward not found"

        val currentPoints = userRow[Users.currentPointsAmount] ?: 0
        val rewardPoints = rewardRow[Rewards.points]

        val usedByUsers = rewardRow[Rewards.usedByUsers]

        if (msisdn in usedByUsers) return@transaction false to "Reward already claimed by user $msisdn"
        if (currentPoints < rewardPoints) return@transaction false to "Insufficient points: Required $rewardPoints, but gained $currentPoints"

        Users.update({ Users.msisdn eq msisdn }) {
            it[currentPointsAmount] = currentPoints - rewardPoints
        }

        val newUsedBy = usedByUsers + msisdn
        Rewards.update({ Rewards.id eq rewardId }) {
            it[Rewards.usedByUsers] = newUsedBy
        }

        return@transaction true to null
    }

    private fun toReward(row: ResultRow): Reward = Reward(
        id = row[Rewards.id],
        rewardName = row[Rewards.rewardName],
        points = row[Rewards.points],
        eligibleUsers = row[Rewards.eligibleUsers],
        usedByUsers = row[Rewards.usedByUsers]
    )
}