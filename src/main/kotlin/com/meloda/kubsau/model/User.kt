package com.meloda.kubsau.model

import com.meloda.kubsau.database.users.Users
import org.jetbrains.exposed.sql.ResultRow

data class User(
    val id: Int,
    val login: String,
    // TODO: 24/02/2024, Danil Nikolaev: SECURITY, FOR FUCK's SAKE
    val password: String,
    val type: Int,
    val departmentId: Int
) {
    companion object {

        fun mapResultRow(row: ResultRow): User = User(
            id = row[Users.id].value,
            login = row[Users.login],
            password = row[Users.password],
            type = row[Users.type],
            departmentId = row[Users.departmentId]
        )
    }
}
