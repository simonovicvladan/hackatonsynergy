package rs.yettel.bms.routes

import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import rs.yettel.bms.db.Offers
import rs.yettel.bms.db.Users
import rs.yettel.bms.models.Offer
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

fun Route.offerRoutes() {

    post("/offers/create") {
        val request = try {
            call.receive<CreateOfferRequest>()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Invalid JSON format")
            return@post
        }

        val offerId = transaction {
            Offers.insertAndGetId {
                it[creatorId] = request.creatorId
                it[points] = request.points
            }.value
        }

        val offer = Offer(id = offerId, creatorId = request.creatorId, points = request.points)

        val qrData = "offer:${offer.id}"
        val matrix = MultiFormatWriter().encode(qrData, BarcodeFormat.QR_CODE, 250, 250)
        val image = MatrixToImageWriter.toBufferedImage(matrix)

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "PNG", outputStream)
        val qrBase64 = Base64.getEncoder().encodeToString(outputStream.toByteArray())

        call.respond(HttpStatusCode.OK, OfferResponse(offer, qrBase64))
    }

    post("/offers/scan") {
        val request = try {
            call.receive<ScanOfferRequest>()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Invalid JSON format")
            return@post
        }

        val result = transaction {
            val offerRow = Offers.selectAll().where { Offers.id eq request.offerId }.singleOrNull()

            if (offerRow == null) return@transaction "Offer not found"
            if (offerRow[Offers.isUsed]) return@transaction "Offer already used"

            val pointsToAdd = offerRow[Offers.points]
            val creatorId = offerRow[Offers.creatorId]

            val creatorCurrentPoints = Users.selectAll().where { Users.id eq creatorId }
                .single()[Users.points]
            Users.update({ Users.id eq creatorId }) {
                it[points] = creatorCurrentPoints + pointsToAdd
            }

            val scannerCurrentPoints = Users.selectAll().where { Users.id eq request.scannerId }
                .single()[Users.points]
            Users.update({ Users.id eq request.scannerId }) {
                it[points] = scannerCurrentPoints + pointsToAdd
            }

            Offers.update({ Offers.id eq request.offerId }) {
                it[isUsed] = true
            }

            "Success"
        }

        if (result == "Success") {
            call.respond(HttpStatusCode.OK, "Points added successfully to both users!")
        } else {
            call.respond(HttpStatusCode.BadRequest, result)
        }
    }
}

@Serializable
data class CreateOfferRequest(val creatorId: Int, val points: Int)

@Serializable
data class OfferResponse(val offer: Offer, val qrCodeBase64: String)

@Serializable
data class ScanOfferRequest(val offerId: Int, val scannerId: Int)
