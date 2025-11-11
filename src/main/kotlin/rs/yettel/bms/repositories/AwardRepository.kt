package rs.yettel.bms.repositories

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import rs.yettel.bms.db.Awards
import rs.yettel.bms.models.Award
import rs.yettel.bms.routes.CreateAwardRequest
import rs.yettel.bms.routes.UpdateAwardRequest

object AwardRepository {

    fun findAll(): List<Award> = transaction {
        Awards.selectAll().map(AwardRepository::toAward)
    }

    fun findById(id: Long): Award? = transaction {
        Awards.selectAll()
            .where { Awards.id eq id }
            .map(AwardRepository::toAward)
            .singleOrNull()
    }

    fun create(request: CreateAwardRequest): Long = transaction {
        Awards.insert { stmt ->
            stmt[awardName] = request.awardName
            stmt[points] = request.points
            stmt[eligibleUsers] = request.eligibleUsers.toList()
            stmt[usedByUsers] = request.usedByUsers.toList()
        } get Awards.id
    }

    fun update(id: Long, request: UpdateAwardRequest): Boolean = transaction {
        Awards.update({ Awards.id eq id }) { stmt ->
            request.awardName?.let { v -> stmt[awardName] = v }
            request.points?.let { v -> stmt[points] = v }
            request.eligibleUsers?.let { lst -> stmt[eligibleUsers] = lst.toList() }
            request.usedByUsers?.let { lst -> stmt[usedByUsers] = lst.toList() }
        } > 0
    }

    fun delete(id: Long): Boolean = transaction {
        Awards.deleteWhere { Awards.id eq id } > 0
    }

    private fun toAward(row: ResultRow): Award = Award(
        id = row[Awards.id],
        awardName = row[Awards.awardName],
        points = row[Awards.points],
        eligibleUsers = row[Awards.eligibleUsers].toList(),
        usedByUsers = row[Awards.usedByUsers].toList()
    )
}