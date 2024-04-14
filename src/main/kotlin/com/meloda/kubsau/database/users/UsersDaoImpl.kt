package com.meloda.kubsau.database.users

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.model.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class UsersDaoImpl : UsersDao {

    override fun mapResultRow(row: ResultRow) = User(
        id = row[Users.id].value,
        login = row[Users.login],
        password = row[Users.password],
        type = row[Users.type],
        departmentId = row[Users.departmentId]
    )

    override suspend fun allUsers(): List<User> = dbQuery {
        Users.selectAll().map(::mapResultRow)
    }

    override suspend fun singleUser(id: Int): User? = dbQuery {
        Users
            .selectAll()
            .where { Users.id eq id }
            .map(::mapResultRow)
            .singleOrNull()
    }

    override suspend fun singleUser(login: String): User? = dbQuery {
        Users
            .selectAll()
            .where { Users.login eq login }
            .map(::mapResultRow)
            .singleOrNull()
    }

    override suspend fun addNewUser(
        login: String,
        password: String,
        type: Int,
        departmentId: Int
    ): User? = dbQuery {
        Users.insert {
            it[Users.login] = login
            it[Users.password] = password
            it[Users.type] = type
            it[Users.departmentId] = departmentId
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override suspend fun editUser(id: Int, login: String, password: String): Boolean = dbQuery {
        Users.update({ Users.id eq id }) {
            it[Users.login] = login
            it[Users.password] = password
        } > 0
    }

    override suspend fun deleteUser(id: Int): Boolean = dbQuery {
        Users.deleteWhere { Users.id eq id } > 0
    }
}
