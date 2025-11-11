package rs.yettel.bms.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        Database.connect(
            url = "jdbc:postgresql://localhost:5432/yettel_bms",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = "postgres"
        )

        transaction {
            SchemaUtils.create(Users)
            println("PostgreSQL database initialized")
        }
    }
}