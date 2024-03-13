package com.meloda.kubsau.database.sessions

import org.jetbrains.exposed.sql.Table

object Sessions : Table() {
    val userId = integer("userId")
    val accessToken = text("accessToken").uniqueIndex()
}
