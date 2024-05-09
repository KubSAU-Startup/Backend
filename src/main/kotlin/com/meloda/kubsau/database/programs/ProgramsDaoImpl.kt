package com.meloda.kubsau.database.programs

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.model.Program
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

class ProgramsDaoImpl : ProgramsDao {

    override suspend fun allPrograms(
        offset: Int?,
        limit: Int?
    ): List<Program> = dbQuery {
        Programs
            .selectAll()
            .apply { if (limit != null) limit(limit, (offset ?: 0).toLong()) }
            .map(::mapResultRow)
    }

    override suspend fun allProgramsByIds(
        offset: Int?,
        limit: Int?,
        programIds: List<Int>
    ): List<Program> = dbQuery {
        Programs
            .selectAll()
            .apply { if (limit != null) limit(limit, (offset ?: 0).toLong()) }
            .where { Programs.id inList programIds }
            .map(::mapResultRow)
    }

    override suspend fun allProgramsByFilters(
        offset: Int?,
        limit: Int?,
        semester: Int?,
        directivityId: Int?
    ): List<Program> = dbQuery {
        val query = Programs
            .selectAll()
            .apply { if (limit != null) limit(limit, (offset ?: 0).toLong()) }

        semester?.let { query.andWhere { Programs.semester eq semester } }
        directivityId?.let { query.andWhere { Programs.directivityId eq directivityId } }

        query.map(::mapResultRow)
    }

    override suspend fun allProgramsBySemester(semester: Int): List<Program> = dbQuery {
        Programs
            .selectAll()
            .where { Programs.semester eq semester }
            .map(::mapResultRow)
    }

    override suspend fun singleProgram(programId: Int): Program? = dbQuery {
        Programs
            .selectAll()
            .where { Programs.id eq programId }
            .map(::mapResultRow)
            .singleOrNull()
    }

    override suspend fun addNewProgram(semester: Int, directivityId: Int): Program? = dbQuery {
        Programs.insert {
            it[Programs.semester] = semester
            it[Programs.directivityId] = directivityId
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override suspend fun updateProgram(
        programId: Int,
        semester: Int,
        directivityId: Int
    ): Boolean = dbQuery {
        Programs.update(where = { Programs.id eq programId }) {
            it[Programs.semester] = semester
            it[Programs.directivityId] = directivityId
        } > 0
    }

    override suspend fun deleteProgram(programId: Int): Boolean = dbQuery {
        Programs.deleteWhere { Programs.id eq programId } > 0
    }

    override suspend fun deletePrograms(programIds: List<Int>): Boolean = dbQuery {
        Programs.deleteWhere { Programs.id inList programIds } > 0
    }

    override fun mapResultRow(row: ResultRow): Program = Program.mapFromDb(row)
}
