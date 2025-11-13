package rs.yettel.bms

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.slf4j.event.Level
import rs.yettel.bms.db.DatabaseFactory
import rs.yettel.bms.firebase.FirebaseAdmin
import rs.yettel.bms.firebase.FirebaseService
import rs.yettel.bms.repositories.UserOfferRepository
import rs.yettel.bms.repositories.UserRepository
import rs.yettel.bms.routes.notificationRoutes
import rs.yettel.bms.routes.qrCodeRoutes
import rs.yettel.bms.routes.rewardRoutes
import rs.yettel.bms.routes.userRoutes
import rs.yettel.bms.services.QrCodeScanService

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(CORS) {
            allowMethod(HttpMethod.Options)
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Delete)
            allowHeader(HttpHeaders.Authorization)
            allowHeader(HttpHeaders.ContentType)
            anyHost()
            allowCredentials = true
            allowNonSimpleContentTypes = true
        }
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(CallLogging) {
            level = Level.INFO
            filter { call -> call.request.path().startsWith("/") }
        }

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
            userOfferRepository = UserOfferRepository,
            firebaseService = firebaseService,
            scannerPoints = 300,
            scaneePoints = 100
        )

        routing {
            get("/") {
                call.respondText("Ktor backend server is running!")
            }
            userRoutes()
            rewardRoutes()
            qrCodeRoutes(qrCodeScanService)
            notificationRoutes()
        }
    }.start(wait = true)
}
