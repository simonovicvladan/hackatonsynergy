package rs.yettel.bms.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val error: String)

