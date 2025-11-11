package rs.yettel.bms.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("DatabaseFactory")

object DatabaseFactory {
    fun init() {
        Database.connect(
            url = "jdbc:postgresql://localhost:5432/yettel_bms",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = "postgres"
        )

        transaction {
            SchemaUtils.create(Users, Rewards)
            logger.info("PostgreSQL database initialized successfully")
        }
    }
}