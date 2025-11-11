package rs.yettel.bms.routes

import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NoContent
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import rs.yettel.bms.repositories.RewardRepository

fun Route.awardRoutes() {

    route("/awards") {

        get {
            val awards = RewardRepository.findAll()
            call.respond(awards)
        }

        get("{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respondText("Invalid or missing award ID", status = BadRequest)
                return@get
            }

            val award = RewardRepository.findById(id)
            if (award == null) {
                call.respondText("Award not found", status = NotFound)
            } else {
                call.respond(award)
            }
        }

        post {
            val request = call.receive<CreateAwardRequest>()
            val newId = RewardRepository.create(request)
            call.respondText("Award created successfully with ID $newId", status = Created)
        }

        put("{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respondText("Invalid or missing award ID", status = BadRequest)
                return@put
            }

            val request = call.receive<UpdateAwardRequest>()
            val success = RewardRepository.update(id, request)
            if (success) {
                call.respondText("Award updated successfully!")
            } else {
                call.respondText("Award not found", status = NotFound)
            }
        }

        delete("{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respondText("Invalid or missing award ID", status = BadRequest)
                return@delete
            }

            val success = RewardRepository.delete(id)
            if (success) {
                call.respondText("Award deleted successfully!", status = NoContent)
            } else {
                println("Delete failed")
            }
        }
    }
}

@Serializable
data class CreateAwardRequest(
    val awardName: String,
    val points: Int,
    val eligibleUsers: List<Int> = emptyList(),
    val usedByUsers: List<Int> = emptyList()
)

@Serializable
data class UpdateAwardRequest(
    val awardName: String? = null,
    val points: Int? = null,
    val eligibleUsers: List<Int>? = null,
    val usedByUsers: List<Int>? = null
)