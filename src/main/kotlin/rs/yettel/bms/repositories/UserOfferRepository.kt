package rs.yettel.bms.repositories

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import rs.yettel.bms.db.UserOffers
import rs.yettel.bms.models.UserOffer

object UserOfferRepository {

    fun findById(offerId: Long): UserOffer? = transaction {
        UserOffers
            .selectAll().where { (UserOffers.offerId eq offerId) and (UserOffers.claimed eq 0) }
            .map { toUserOffer(it) }
            .singleOrNull()
    }

    fun findUnclaimedOffers(email: String): List<UserOffer> = transaction {
        UserOffers
            .selectAll()
            .where {
                (UserOffers.email eq email) and
                        (UserOffers.claimed eq 0) and
                        UserOffers.scannerEmail.isNotNull()
            }
            .map(::toUserOffer)
    }

    fun updateUserScannerEmail(userEmail: String, userScannerEmail: String) = transaction {
        UserOffers.update({ UserOffers.email eq userEmail }) { stmt ->
            userScannerEmail.let { v -> stmt[scannerEmail] = v }
        } > 0
    }

    fun updateUserOfferClaim(userEmail: String) = transaction {
        UserOffers.update({ UserOffers.email eq userEmail }) { stmt ->
            userEmail.let { stmt[claimed] = 1 }
        } > 0
    }

    fun findScannerEmailByOfferId(offerId: Long): String? = transaction {
        UserOffers
            .selectAll()
            .where { (UserOffers.offerId eq offerId) and (UserOffers.claimed eq 0) }
            .map { it[UserOffers.scannerEmail] }
            .singleOrNull()
    }

    private fun toUserOffer(it: ResultRow): UserOffer = UserOffer(
        msisdn = it[UserOffers.msisdn],
        email = it[UserOffers.email],
        offerId = it[UserOffers.offerId],
        offerName = it[UserOffers.offerName],
        points = it[UserOffers.points],
        price = it[UserOffers.price],
        discount = it[UserOffers.discountPrice],
        claimed = it[UserOffers.claimed],
        scannerEmail = it[UserOffers.scannerEmail]
    )
}
