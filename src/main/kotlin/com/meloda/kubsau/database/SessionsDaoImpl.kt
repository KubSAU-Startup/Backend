package com.meloda.kubsau.database

import com.meloda.kubsau.database.DatabaseSingleton.dbQuery
import com.meloda.kubsau.model.Session
import com.meloda.kubsau.model.Sessions
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class SessionsDaoImpl : SessionsDao {

    private fun resultRowToArticle(row: ResultRow) = Session(
        userId = row[Sessions.userId],
        accessToken = row[Sessions.accessToken]
    )

    override suspend fun allSessions(): List<Session> = dbQuery {
        Sessions.selectAll().map(::resultRowToArticle)
    }

    override suspend fun singleSession(userId: Int): Session? = dbQuery {
        Sessions
            .selectAll()
            .where { Sessions.userId eq userId }
            .map(::resultRowToArticle)
            .singleOrNull()
    }

    override suspend fun singleSession(accessToken: String): Session? = dbQuery {
        Sessions
            .selectAll()
            .where { Sessions.accessToken eq accessToken }
            .map(::resultRowToArticle)
            .singleOrNull()
    }

    override suspend fun addNewSession(userId: Int, accessToken: String): Session? = dbQuery {
        Sessions.insert {
            it[Sessions.userId] = userId
            it[Sessions.accessToken] = accessToken
        }.resultedValues?.singleOrNull()?.let(::resultRowToArticle)
    }

    override suspend fun deleteSession(userId: Int, accessToken: String): Boolean = dbQuery {
        Sessions.deleteWhere {
            (Sessions.userId eq userId) and (Sessions.accessToken eq accessToken)
        } > 0
    }
}

val sessionsDao: SessionsDao = SessionsDaoImpl()
