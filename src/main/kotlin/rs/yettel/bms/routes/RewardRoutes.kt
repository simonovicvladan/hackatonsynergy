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

fun Route.rewardRoutes() {

    route("/rewards") {

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
data class CreateRewardRequest(
    val rewardName: String,
    val points: Int,
    val eligibleUsers: List<Int> = emptyList(),
    val usedByUsers: List<Int> = emptyList()
)

@Serializable
data class UpdateRewardRequest(
    val rewardName: String? = null,
    val points: Int? = null,
    val eligibleUsers: List<Int>? = null,
    val usedByUsers: List<Int>? = null
)