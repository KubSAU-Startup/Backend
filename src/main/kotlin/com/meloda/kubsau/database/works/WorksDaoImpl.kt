package com.meloda.kubsau.database.works

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.model.Work
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class WorksDaoImpl : WorksDao {

    override suspend fun allWorks(): List<Work> = dbQuery {
        Works.selectAll().map(::mapResultRow)
    }

    override suspend fun singleWork(workId: Int): Work? = dbQuery {
        Works
            .selectAll()
            .where { Works.id eq workId }
            .map(::mapResultRow)
            .singleOrNull()
    }

    override suspend fun addNewWork(
        typeId: Int,
        disciplineId: Int,
        studentId: Int,
        registrationDate: Long,
        title: String
    ): Work? = dbQuery {
        Works.insert {
            it[Works.typeId] = typeId
            it[Works.disciplineId] = disciplineId
            it[Works.studentId] = studentId
            it[Works.registrationDate] = registrationDate
            it[Works.title] = title
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override suspend fun deleteWork(workId: Int): Boolean = dbQuery {
        Works.deleteWhere { Works.id eq workId } > 0
    }

    override fun mapResultRow(row: ResultRow): Work = Work.mapResultRow(row)
}
