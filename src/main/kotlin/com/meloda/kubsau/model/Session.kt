package com.meloda.kubsau.model

import org.jetbrains.exposed.sql.Table

// TODO: 24/02/2024, Danil Nikolaev: reimplement
data class Session(
    val userId: Int,
    val accessToken: String
)

object Sessions : Table() {
    val userId = integer("userId")
    val accessToken = varchar("accessToken", 1024)
}
