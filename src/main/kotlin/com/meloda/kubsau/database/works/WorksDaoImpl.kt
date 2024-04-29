package com.meloda.kubsau.database.works

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.model.Work
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

class WorksDaoImpl : WorksDao {

    override suspend fun allWorks(): List<Work> = dbQuery {
        Works.selectAll().map(::mapResultRow)
    }

    override suspend fun allWorksByIds(workIds: List<Int>): List<Work> = dbQuery {
        Works
            .selectAll()
            .where { Works.id inList workIds }
            .map(::mapResultRow)
    }

    override suspend fun singleWork(workId: Int): Work? = dbQuery {
        Works
            .selectAll()
            .where { Works.id eq workId }
            .map(::mapResultRow)
            .singleOrNull()
    }

    override suspend fun addNewWork(
        disciplineId: Int,
        studentId: Int,
        registrationDate: Long,
        title: String?
    ): Work? = dbQuery {
        Works.insert {
            it[Works.disciplineId] = disciplineId
            it[Works.studentId] = studentId
            it[Works.registrationDate] = registrationDate
            it[Works.title] = title
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override suspend fun updateWork(
        workId: Int,
        disciplineId: Int,
        studentId: Int,
        registrationDate: Long,
        title: String?
    ): Int = dbQuery {
        Works.update(where = { Works.id eq workId }) {
            it[Works.disciplineId] = disciplineId
            it[Works.studentId] = studentId
            it[Works.registrationDate] = registrationDate
            it[Works.title] = title
        }
    }

    override suspend fun deleteWork(workId: Int): Boolean = dbQuery {
        Works.deleteWhere { Works.id eq workId } > 0
    }

    override suspend fun deleteWorks(workIds: List<Int>): Boolean = dbQuery {
        Works.deleteWhere { Works.id inList workIds } > 0
    }

    override fun mapResultRow(row: ResultRow): Work = Work.mapResultRow(row)
}
