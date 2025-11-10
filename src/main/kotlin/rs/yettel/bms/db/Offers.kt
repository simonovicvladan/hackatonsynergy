package rs.yettel.bms.db

import org.jetbrains.exposed.dao.id.IntIdTable

object Offers : IntIdTable("offers") {
    val creatorId = integer("creator_id").references(Users.id)
    val points = integer("points")
    val isUsed = bool("is_used").default(false)
}