package com.meloda.kubsau.database

import com.meloda.kubsau.database.DatabaseSingleton.dbQuery
import com.meloda.kubsau.model.User
import com.meloda.kubsau.model.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class UsersDaoImpl : UsersDao {

    private fun resultRowToArticle(row: ResultRow) = User(
        id = row[Users.id],
        email = row[Users.email],
        password = row[Users.password]
    )

    override suspend fun allUsers(): List<User> = dbQuery {
        Users.selectAll().map(::resultRowToArticle)
    }

    override suspend fun singleUser(id: Int): User? = dbQuery {
        Users
            .selectAll()
            .where { Users.id eq id }
            .map(::resultRowToArticle)
            .singleOrNull()
    }

    override suspend fun singleUser(email: String): User? = dbQuery {
        Users
            .selectAll()
            .where { Users.email eq email }
            .map(::resultRowToArticle)
            .singleOrNull()
    }

    override suspend fun addNewUser(email: String, password: String): User? = dbQuery {
        Users.insert {
            it[Users.email] = email
            it[Users.password] = password
        }.resultedValues?.singleOrNull()?.let(::resultRowToArticle)
    }

    override suspend fun editUser(id: Int, email: String, password: String): Boolean = dbQuery {
        Users.update({ Users.id eq id }) {
            it[Users.email] = email
            it[Users.password] = password
        } > 0
    }

    override suspend fun deleteUser(id: Int): Boolean = dbQuery {
        Users.deleteWhere { Users.id eq id } > 0
    }
}

val usersDao: UsersDao = UsersDaoImpl()
