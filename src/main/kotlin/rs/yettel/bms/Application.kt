package rs.yettel.bms

import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.callloging.*
import rs.yettel.bms.routes.qrCodeRoutes
import rs.yettel.bms.db.DatabaseFactory
import rs.yettel.bms.firebase.FirebaseAdmin
import rs.yettel.bms.firebase.FirebaseService
import rs.yettel.bms.repositories.UserRepository
import rs.yettel.bms.routes.awardRoutes
import rs.yettel.bms.routes.notificationRoutes
import rs.yettel.bms.routes.userRoutes
import rs.yettel.bms.services.QrCodeScanService

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) { json() }
        install(CallLogging)

        install(StatusPages) {
            exception<Throwable> { call, cause ->
                cause.printStackTrace()
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    message = mapOf("error" to (cause.message ?: "Unknown error"))
                )
            }
        }

        DatabaseFactory.init()
        FirebaseAdmin.init()

        val firebaseService = FirebaseService
        val qrCodeScanService = QrCodeScanService(
            userRepository = UserRepository,
            firebaseService = firebaseService,
            scannerPoints = 2000,
            scaneePoints = 1000
        )

        routing {
            get("/") {
                call.respondText("Ktor backend is running with FCM!")
            }
            userRoutes()
            awardRoutes()
            qrCodeRoutes(qrCodeScanService)
            notificationRoutes()
        }
    }.start(wait = true)
}
