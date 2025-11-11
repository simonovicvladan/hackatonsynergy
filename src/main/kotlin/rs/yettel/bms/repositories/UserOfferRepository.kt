package rs.yettel.bms.repositories

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import rs.yettel.bms.db.UserOffers
import rs.yettel.bms.models.UserOffer

object UserOfferRepository {

    fun findUnclaimedOffers(email: String): List<UserOffer> = transaction {
        UserOffers
            .selectAll().where { (UserOffers.email eq email) and (UserOffers.claimed eq 0) }
            .map(::toUserOffer)
    }

    private fun toUserOffer(it: ResultRow) = UserOffer(
        msisdn = it[UserOffers.msisdn],
        email = it[UserOffers.email],
        offerId = it[UserOffers.offerId],
        offerName = it[UserOffers.offerName],
        offerDescription = it[UserOffers.description],
        points = it[UserOffers.points],
        price = it[UserOffers.price],
        discount = it[UserOffers.discount],
        claimed = it[UserOffers.claimed]
    )
}
