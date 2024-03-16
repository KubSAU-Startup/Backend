package com.meloda.kubsau.database.works

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.model.Work
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert

class WorksDaoImpl : WorksDao {

    override fun mapResultRow(row: ResultRow): Work = Work(
        id = row[Works.id].value,
        typeId = row[Works.typeId],
        disciplineId = row[Works.disciplineId],
        studentId = row[Works.studentId],
        registrationDate = row[Works.registrationDate],
        title = row[Works.title]
    )

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
        Works.upsert {
            it[Works.typeId] = typeId
            it[Works.disciplineId] = disciplineId
            it[Works.studentId] = typeId
            it[Works.registrationDate] = typeId
            it[Works.title] = title
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override suspend fun deleteWork(workId: Int): Boolean = dbQuery {
        Works.deleteWhere { Works.id eq workId } > 0
    }
}
