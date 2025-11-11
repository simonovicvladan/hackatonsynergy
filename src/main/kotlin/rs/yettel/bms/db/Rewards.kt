package rs.yettel.bms.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.IntegerColumnType

object Rewards : Table(name = "rewards") {
    val id: Column<Long> = long("id").autoIncrement()
    val awardName: Column<String> = text("reward_name")
    val points: Column<Int> = integer("points")
    val eligibleUsers: Column<List<Int>> = array("eligible_users", IntegerColumnType())
    val usedByUsers: Column<List<Int>> = array("used_by_users", IntegerColumnType())

    override val primaryKey = PrimaryKey(id)
}
