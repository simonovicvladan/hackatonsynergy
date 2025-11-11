package rs.yettel.bms.routes

import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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
    }
}