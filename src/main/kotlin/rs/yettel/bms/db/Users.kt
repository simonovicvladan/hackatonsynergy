package rs.yettel.bms.db

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.TextColumnType

object Users : Table("users") {
    val msisdn = long("msisdn").uniqueIndex()
    val subscriberId = long("subscriber_id").nullable()
    val uuid = long("uuid").nullable()
    val name = text("name")
    val email = text("email").nullable()
    val age = short("age").nullable()
    val tariffPackage = text("tariff_package").nullable()
    val category = short("category").nullable()
    val currentPointsAmount = integer("current_points_amount").nullable()
    val scannedQrCodes: Column<List<String>?> = array<String>("scanned_qr_codes", TextColumnType()).nullable()
    val fcmToken = text("fcm_token").nullable()

    override val primaryKey = PrimaryKey(msisdn)
}
