package com.meloda.kubsau.model

import com.meloda.kubsau.database.users.Users
import org.jetbrains.exposed.sql.ResultRow

data class User(
    val id: Int,
    val login: String,
    val passwordHash: String,
    val employeeId: Int
) {

    companion object {

        fun mapResultRow(row: ResultRow): User = User(
            id = row[Users.id].value,
            login = row[Users.login],
            passwordHash = row[Users.passwordHash],
            employeeId = row[Users.employeeId]
        )
    }
}
