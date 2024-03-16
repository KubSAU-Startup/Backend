package com.meloda.kubsau.database.sessions

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.model.Session
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class SessionsDaoImpl : SessionsDao {

    override fun mapResultRow(row: ResultRow) = Session(
        userId = row[Sessions.userId],
        accessToken = row[Sessions.accessToken]
    )

    override suspend fun allSessions(): List<Session> = dbQuery {
        Sessions.selectAll().map(::mapResultRow)
    }

    override suspend fun singleSession(userId: Int): Session? = dbQuery {
        Sessions
            .selectAll()
            .where { Sessions.userId eq userId }
            .map(this::mapResultRow)
            .singleOrNull()
    }

    override suspend fun singleSession(accessToken: String): Session? = dbQuery {
        Sessions
            .selectAll()
            .where { Sessions.accessToken eq accessToken }
            .map(this::mapResultRow)
            .singleOrNull()
    }

    override suspend fun addNewSession(userId: Int, accessToken: String): Session? = dbQuery {
        Sessions.upsert {
            it[Sessions.userId] = userId
            it[Sessions.accessToken] = accessToken
        }.resultedValues?.singleOrNull()?.let(this::mapResultRow)
    }

    override suspend fun deleteSession(userId: Int, accessToken: String): Boolean = dbQuery {
        Sessions.deleteWhere {
            (Sessions.userId eq userId) and (Sessions.accessToken eq accessToken)
        } > 0
    }
}
