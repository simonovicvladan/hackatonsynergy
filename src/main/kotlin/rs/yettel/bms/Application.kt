package rs.yettel.bms

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.callloging.*
import rs.yettel.bms.db.DatabaseFactory
import rs.yettel.bms.firebase.FirebaseAdmin
import rs.yettel.bms.routes.notificationRoutes
import rs.yettel.bms.routes.userRoutes

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) { json() }
        install(CallLogging)

        DatabaseFactory.init()
        FirebaseAdmin.init()

        routing {
            get("/") {
                call.respondText("Ktor backend is running with FCM!")
            }
            userRoutes()
            notificationRoutes()
        }
    }.start(wait = true)
}
