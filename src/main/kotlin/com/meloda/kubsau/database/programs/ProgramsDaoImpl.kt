package com.meloda.kubsau.database.programs

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.model.Program
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

class ProgramsDaoImpl : ProgramsDao {

    override suspend fun allPrograms(): List<Program> = dbQuery {
        Programs.selectAll().map(::mapResultRow)
    }

    override suspend fun allProgramsByIds(programIds: List<Int>): List<Program> = dbQuery {
        Programs
            .selectAll()
            .where { Programs.id inList programIds }
            .map(::mapResultRow)
    }

    override suspend fun programsBySemester(semester: Int): List<Program> = dbQuery {
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

    override suspend fun addNewProgram(title: String, semester: Int, directivityId: Int): Program? = dbQuery {
        Programs.insert {
            it[Programs.title] = title
            it[Programs.semester] = semester
            it[Programs.directivityId] = directivityId
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override suspend fun updateProgram(
        programId: Int,
        title: String,
        semester: Int,
        directivityId: Int
    ): Boolean = dbQuery {
        Programs.update(where = { Programs.id eq programId }) {
            it[Programs.title] = title
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

    override fun mapResultRow(row: ResultRow): Program = Program.mapResultRow(row)
}
