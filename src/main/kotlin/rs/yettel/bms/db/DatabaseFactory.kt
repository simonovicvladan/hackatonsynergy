package rs.yettel.bms.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        Database.connect(
            url = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/yettel_bms",
            driver = System.getenv("DB_DRIVER") ?: "org.postgresql.Driver",
            user = System.getenv("DB_USER") ?: "postgres",
            password = System.getenv("DB_PASSWORD") ?: "postgres"
        )

        transaction {
            SchemaUtils.create(Users)
            println("PostgreSQL database initialized")
        }
    }
}