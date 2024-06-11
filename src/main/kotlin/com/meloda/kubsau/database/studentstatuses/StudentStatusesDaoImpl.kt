package com.meloda.kubsau.database.studentstatuses

import com.meloda.kubsau.config.DatabaseController.dbQuery
import com.meloda.kubsau.model.StudentStatus
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class StudentStatusesDaoImpl : StudentStatusesDao {

    override suspend fun allStatuses(): List<StudentStatus> = dbQuery {
        StudentStatuses.selectAll().map(::mapResultRow)
    }

    override suspend fun allStatusesByIds(statusIds: List<Int>): List<StudentStatus> = dbQuery {
        StudentStatuses
            .selectAll()
            .where { StudentStatuses.id inList statusIds }
            .map(::mapResultRow)
    }

    override suspend fun singleStatus(statusId: Int): StudentStatus? = dbQuery {
        StudentStatuses
            .selectAll()
            .where { StudentStatuses.id eq statusId }
            .map(::mapResultRow)
            .singleOrNull()
    }

    override suspend fun addNewStatus(title: String): StudentStatus? = dbQuery {
        StudentStatuses.insert {
            it[StudentStatuses.title] = title
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override fun mapResultRow(row: ResultRow): StudentStatus = StudentStatus.mapFromDb(row)
}
