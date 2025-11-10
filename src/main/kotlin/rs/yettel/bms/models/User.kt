package rs.yettel.bms.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int? = null,
    val name: String,
    val points: Int = 0
)

