package rs.yettel.bms.models

import kotlinx.serialization.Serializable

@Serializable
data class Award(
    val id: Long? = null,
    val awardName: String,
    val points: Int,
    val eligibleUsers: List<Int> = emptyList(),
    val usedByUsers: List<Int> = emptyList()
)