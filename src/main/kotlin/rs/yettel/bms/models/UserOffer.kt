package rs.yettel.bms.models

import kotlinx.serialization.Serializable

@Serializable
data class UserOffer(
    val msisdn: Long,
    val email: String,
    val offerId: Long,
    val offerName: String,
    val points: Int,
    val price: Int,
    val discount: Int,
    val claimed: Int,
    val scannerEmail: String?
)