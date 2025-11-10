package rs.yettel.bms.db

import org.jetbrains.exposed.dao.id.IntIdTable

object Users : IntIdTable("users") {
    val name = varchar("name", 100)
    val points = integer("points").default(0)
}