package com.meloda.kubsau.database.faculties

import com.meloda.kubsau.config.DatabaseController.dbQuery
import com.meloda.kubsau.model.Faculty
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class FacultyDaoImpl : FacultyDao {

    override suspend fun allFaculties(
        offset: Int?,
        limit: Int?
    ): List<Faculty> = dbQuery {
        Faculties
            .selectAll()
            .apply {
                if (limit != null) {
                    limit(limit, (offset ?: 0).toLong())
                }
            }
            .map(::mapResultRow)
    }

    override suspend fun allFacultiesByIds(facultyIds: List<Int>): List<Faculty> = dbQuery {
        Faculties
            .selectAll()
            .where { Faculties.id inList facultyIds }
            .map(::mapResultRow)
    }

    override suspend fun singleFaculty(facultyId: Int): Faculty? = dbQuery {
        Faculties
            .selectAll()
            .where { Faculties.id eq facultyId }
            .map(::mapResultRow)
            .singleOrNull()
    }

    override suspend fun addNewFaculty(title: String): Faculty? = dbQuery {
        Faculties.insert {
            it[Faculties.title] = title
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override fun mapResultRow(row: ResultRow): Faculty = Faculty.mapFromDb(row)
}
