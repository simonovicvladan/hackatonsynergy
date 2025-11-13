package rs.yettel.bms.db

import org.jetbrains.exposed.sql.Table

object UserOffers : Table("user_offers") {
    val msisdn = long("msisdn")
    val email = text("email")
    val offerId = long("offer_id")
    val offerName = text("offer_name")
    val description = text("description").default("")
    val points = integer("points")
    val price = integer("price")
    val discount = integer("discount")
    val claimed = integer("claimed").default(0)
    val scannerEmail = text("scanner_email")
}
