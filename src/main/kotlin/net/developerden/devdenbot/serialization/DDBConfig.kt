package net.developerden.devdenbot.serialization

import kotlinx.serialization.Serializable

@Serializable
data class DDBConfig(
    val token: String,
    val prefix: String,
    val ownerID: Long,
    val colour: Int
)
