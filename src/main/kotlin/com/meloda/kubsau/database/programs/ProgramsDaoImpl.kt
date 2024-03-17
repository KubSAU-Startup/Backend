package com.meloda.kubsau.database.programs

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.model.Program
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class ProgramsDaoImpl : ProgramsDao {
    override suspend fun allPrograms(): List<Program> = dbQuery {
        Programs.selectAll().map(::mapResultRow)
    }

    override suspend fun programsBySemester(semester: Int): List<Program> = dbQuery {
        Programs
            .selectAll()
            .where { Programs.semester eq semester }
            .map(::mapResultRow)
    }

    override suspend fun addNewProgram(title: String, semester: Int): Program? = dbQuery {
        Programs.insert {
            it[Programs.title] = title
            it[Programs.semester] = semester
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override suspend fun deleteProgram(programId: Int): Boolean = dbQuery {
        Programs.deleteWhere { Programs.id eq programId } > 0
    }

    override fun mapResultRow(row: ResultRow): Program = Program(
        id = row[Programs.id].value,
        title = row[Programs.title],
        semester = row[Programs.semester]
    )
}
