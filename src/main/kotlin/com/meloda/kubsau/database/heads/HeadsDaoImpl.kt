package com.meloda.kubsau.database.heads

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.model.Head
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

class HeadsDaoImpl : HeadsDao {

    override suspend fun allHeads(
        offset: Int?,
        limit: Int?
    ): List<Head> = dbQuery {
        Heads
            .selectAll()
            .apply {
                if (limit != null) {
                    limit(limit, (offset ?: 0).toLong())
                }
            }
            .map(::mapResultRow)
    }

    override suspend fun allHeadsByIds(headIds: List<Int>): List<Head> = dbQuery {
        Heads
            .selectAll()
            .where { Heads.id inList headIds }
            .map(::mapResultRow)
    }

    override suspend fun singleHead(headId: Int): Head? = dbQuery {
        Heads
            .selectAll()
            .where { Heads.id eq headId }
            .map(::mapResultRow)
            .singleOrNull()
    }

    override suspend fun addNewHead(
        code: String,
        abbreviation: String,
        title: String,
        facultyId: Int
    ): Head? = dbQuery {
        Heads.insert {
            it[Heads.code] = code
            it[Heads.abbreviation] = abbreviation
            it[Heads.title] = title
            it[Heads.facultyId] = facultyId
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override suspend fun updateHead(
        headId: Int,
        code: String,
        abbreviation: String,
        title: String,
        facultyId: Int
    ): Boolean = dbQuery {
        Heads.update(where = { Heads.id eq headId }) {
            it[Heads.code] = code
            it[Heads.abbreviation] = abbreviation
            it[Heads.title] = title
            it[Heads.facultyId] = facultyId
        } > 0
    }

    override suspend fun deleteHead(headId: Int): Boolean = dbQuery {
        Heads.deleteWhere { Heads.id eq headId } > 0
    }

    override suspend fun deleteHeads(headIds: List<Int>): Boolean = dbQuery {
        Heads.deleteWhere { Heads.id inList headIds } > 0
    }

    override fun mapResultRow(row: ResultRow): Head = Head.mapResultRow(row)
}
