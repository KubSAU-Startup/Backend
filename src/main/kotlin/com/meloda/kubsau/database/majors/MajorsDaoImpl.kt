package com.meloda.kubsau.database.majors

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.model.Major
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert

class MajorsDaoImpl : MajorsDao {
    override suspend fun allMajors(): List<Major> = dbQuery {
        Majors.selectAll().map(::mapResultRow)
    }

    override suspend fun singleMajor(majorId: Int): Major? = dbQuery {
        Majors
            .selectAll()
            .where { Majors.id eq majorId }
            .map(::mapResultRow)
            .singleOrNull()
    }

    override suspend fun addNewMajor(
        code: String,
        title: String,
        abbreviation: String
    ): Major? = dbQuery {
        Majors.upsert {
            it[Majors.code] = code
            it[Majors.title] = title
            it[Majors.abbreviation] = abbreviation
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override suspend fun deleteMajor(majorId: Int): Boolean = dbQuery {
        Majors.deleteWhere { Majors.id eq majorId } > 0
    }

    override fun mapResultRow(row: ResultRow): Major = Major(
        id = row[Majors.id].value,
        code = row[Majors.code],
        title = row[Majors.title],
        abbreviation = row[Majors.abbreviation]
    )
}
