package rs.yettel.bms.db

import org.jetbrains.exposed.sql.Table

object Users : Table("users") {
    val msisdn = long("msisdn").uniqueIndex()
    val id = long("id").nullable()
    val uuid = long("uuid").nullable()
    val name = text("name")
    val email = text("email").nullable()
    val age = short("age").nullable()
    val tariffPackage = text("tariff_package").nullable()
    val category = short("category").nullable()
    val currentPointsAmount = integer("current_points_amount").nullable()
    val scannedPoints = text("scanned_points").nullable()
    val fcmToken = text("fcm_token").nullable()


    override val primaryKey = PrimaryKey(msisdn)
}
