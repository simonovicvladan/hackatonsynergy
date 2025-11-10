package rs.yettel.bms.models

import kotlinx.serialization.Serializable

@Serializable
data class Offer(
    val id: Int? = null,
    val creatorId: Int,
    val points: Int,
    val isUsed: Boolean = false
)

