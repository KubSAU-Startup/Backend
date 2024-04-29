package com.meloda.kubsau.database.faculties

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.model.Faculty
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class FacultiesDaoImpl : FacultiesDao {

    override suspend fun allFaculties(): List<Faculty> = dbQuery {
        Faculties.selectAll().map(::mapResultRow)
    }

    override suspend fun addNewFaculty(title: String): Faculty? = dbQuery {
        Faculties.insert {
            it[Faculties.title] = title
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override fun mapResultRow(row: ResultRow): Faculty = Faculty.mapResultRow(row)
}
