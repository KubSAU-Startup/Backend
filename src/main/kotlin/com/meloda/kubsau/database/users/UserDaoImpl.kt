package com.meloda.kubsau.database.users

import com.meloda.kubsau.config.DatabaseController.dbQuery
import com.meloda.kubsau.model.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

class UserDaoImpl : UserDao {

    override suspend fun allUsers(): List<User> = dbQuery {
        Users.selectAll().map(::mapResultRow)
    }

    override suspend fun allUsersByIds(userIds: List<Int>): List<User> = dbQuery {
        Users
            .selectAll()
            .where { Users.id inList userIds }
            .map(::mapResultRow)
    }

    override suspend fun singleUser(userId: Int): User? = dbQuery {
        Users
            .selectAll()
            .where { Users.id eq userId }
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
        employeeId: Int
    ): User? = dbQuery {
        Users.insert {
            it[Users.login] = login
            it[Users.passwordHash] = password
            it[Users.employeeId] = employeeId
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override suspend fun updateUser(userId: Int, login: String, password: String): Boolean = dbQuery {
        Users.update({ Users.id eq userId }) {
            it[Users.login] = login
            it[Users.passwordHash] = password
        } > 0
    }

    override suspend fun deleteUser(userId: Int): Boolean = dbQuery {
        Users.deleteWhere { Users.id eq userId } > 0
    }

    override suspend fun deleteUsers(userIds: List<Int>): Boolean = dbQuery {
        Users.deleteWhere { Users.id inList userIds } > 0
    }

    override fun mapResultRow(row: ResultRow): User = User.mapResultRow(row)
}
