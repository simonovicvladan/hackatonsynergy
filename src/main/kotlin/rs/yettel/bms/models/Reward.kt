package rs.yettel.bms.models

import kotlinx.serialization.Serializable

@Serializable
data class Reward(
    val id: Long? = null,
    val rewardName: String,
    val points: Int,
    val eligibleUsers: List<Long> = emptyList(),
    val usedByUsers: List<Long> = emptyList()
)