package com.meloda.kubsau.database.heads

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.model.Head
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class HeadsDaoImpl : HeadsDao {

    override suspend fun allHeads(): List<Head> = dbQuery {
        Heads.selectAll().map(::mapResultRow)
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

    override fun mapResultRow(row: ResultRow): Head = Head.mapResultRow(row)
}
