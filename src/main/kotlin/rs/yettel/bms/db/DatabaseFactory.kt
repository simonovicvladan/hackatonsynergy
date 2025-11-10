package rs.yettel.bms.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        Database.connect("jdbc:h2:file:./build/db", driver = "org.h2.Driver")

        transaction {
            SchemaUtils.create(Users, Offers)
            println("Database initialized")
        }
    }
}