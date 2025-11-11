package rs.yettel.bms.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.LongColumnType

object Rewards : Table(name = "rewards") {
    val id: Column<Long> = long("id").autoIncrement()
    val rewardName: Column<String> = text("reward_name")
    val points: Column<Int> = integer("points")
    val eligibleUsers: Column<List<Long>> = array("eligible_users", LongColumnType())
    val usedByUsers: Column<List<Long>> = array("used_by_users", LongColumnType())

    override val primaryKey = PrimaryKey(id)
}
