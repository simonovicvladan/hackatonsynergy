package rs.yettel.bms.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import rs.yettel.bms.db.Users
import rs.yettel.bms.models.User

fun Route.userRoutes() {

    post("/login") {
        val request = try {
            call.receive<LoginRequest>()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Invalid JSON format")
            return@post
        }

        val user = transaction {
            val existingUser = Users
                .selectAll()
                .where { Users.name eq request.name }
                .singleOrNull()

            if (existingUser != null) {
                User(
                    id = existingUser[Users.id].value,
                    name = existingUser[Users.name],
                    points = existingUser[Users.points]
                )
            } else {
                val newUserId = Users.insertAndGetId {
                    it[name] = request.name
                    it[points] = 0
                }.value

                User(id = newUserId, name = request.name, points = 0)
            }
        }

        call.respond(HttpStatusCode.OK, user)
    }

    get("/users") {
        val users = transaction {
            val result = Users.selectAll().map {
                User(
                    id = it[Users.id].value,
                    name = it[Users.name],
                    points = it[Users.points]
                )
            }
            result
        }
        call.respond(HttpStatusCode.OK, users)
    }

    post("/users/{id}/points") {
        val idParam = call.parameters["id"]
        if (idParam == null) {
            call.respond(HttpStatusCode.BadRequest, "Missing user ID")
            return@post
        }

        val userId = idParam.toIntOrNull()
        if (userId == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
            return@post
        }

        val request = try {
            call.receive<UpdatePointsRequest>()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Invalid JSON format")
            return@post
        }

        val updatedUser = transaction {
            val row = Users
                .selectAll()
                .where { Users.id eq userId }
                .singleOrNull()

            if (row == null) {
                null
            } else {
                val currentPoints = row[Users.points]
                val newPoints = currentPoints + request.pointsToAdd

                Users.update({ Users.id eq userId }) {
                    it[points] = newPoints
                }

                User(
                    id = row[Users.id].value,
                    name = row[Users.name],
                    points = newPoints
                )
            }
        }

        if (updatedUser == null) {
            call.respond(HttpStatusCode.NotFound, "User not found")
        } else {
            call.respond(HttpStatusCode.OK, updatedUser)
        }
    }
}

@Serializable
data class LoginRequest(val name: String)

@Serializable
data class UpdatePointsRequest(val pointsToAdd: Int)