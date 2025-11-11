package rs.yettel.bms.repositories

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import rs.yettel.bms.db.Rewards
import rs.yettel.bms.models.Reward
import rs.yettel.bms.routes.CreateAwardRequest
import rs.yettel.bms.routes.UpdateAwardRequest

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

    fun create(request: CreateAwardRequest): Long = transaction {
        Rewards.insert { stmt ->
            stmt[awardName] = request.awardName
            stmt[points] = request.points
            stmt[eligibleUsers] = request.eligibleUsers.toList()
            stmt[usedByUsers] = request.usedByUsers.toList()
        } get Rewards.id
    }

    fun update(id: Long, request: UpdateAwardRequest): Boolean = transaction {
        Rewards.update({ Rewards.id eq id }) { stmt ->
            request.awardName?.let { v -> stmt[awardName] = v }
            request.points?.let { v -> stmt[points] = v }
            request.eligibleUsers?.let { lst -> stmt[eligibleUsers] = lst.toList() }
            request.usedByUsers?.let { lst -> stmt[usedByUsers] = lst.toList() }
        } > 0
    }

    fun delete(id: Long): Boolean = transaction {
        Rewards.deleteWhere { Rewards.id eq id } > 0
    }

    private fun toReward(row: ResultRow): Reward = Reward(
        id = row[Rewards.id],
        rewardName = row[Rewards.awardName],
        points = row[Rewards.points],
        eligibleUsers = row[Rewards.eligibleUsers],
        usedByUsers = row[Rewards.usedByUsers]
    )
}