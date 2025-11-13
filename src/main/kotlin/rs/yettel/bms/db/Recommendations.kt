package rs.yettel.bms.db

import org.jetbrains.exposed.sql.Table

object Recommendations : Table("sbb_recommendations") {
    val subscriberId = long("subscriber_id")
    val packageName = text("package_name")
    val points = integer("points")
    val rank = integer("rank")
    val discountPrice = double("discount_price")
}

