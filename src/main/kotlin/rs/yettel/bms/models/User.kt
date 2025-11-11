package rs.yettel.bms.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val msisdn: Long,
    val subscriberId: Long?,
    val uuid: Long?,
    val name: String,
    val email: String?,
    val age: Short?,
    val tariffPackage: String?,
    val category: Short?,
    val currentPointsAmount: Int?,
    val scannedQrCodes: List<String>?
)