package com.meloda.kubsau.database.worktypes

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.model.WorkType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class WorkTypesDaoImpl : WorkTypesDao {

    override fun mapResultRow(row: ResultRow): WorkType = WorkType(
        id = row[WorkTypes.id].value,
        title = row[WorkTypes.title],
        isEditable = row[WorkTypes.isEditable] == 1
    )

    override suspend fun allWorkTypes(): List<WorkType> = dbQuery {
        WorkTypes.selectAll().map(::mapResultRow)
    }

    override suspend fun singleWorkType(workTypeId: Int): WorkType? = dbQuery {
        WorkTypes
            .selectAll()
            .where { WorkTypes.id eq workTypeId }
            .map(::mapResultRow)
            .singleOrNull()
    }

    override suspend fun singleWorkType(title: String): WorkType? = dbQuery {
        WorkTypes
            .selectAll()
            .where { WorkTypes.title eq title }
            .map(::mapResultRow)
            .singleOrNull()
    }

    override suspend fun addNewWorkType(title: String, isEditable: Boolean): WorkType? = dbQuery {
        WorkTypes.insert {
            it[WorkTypes.title] = title
            it[WorkTypes.isEditable] = if (isEditable) 1 else 0
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override suspend fun deleteWorkType(workTypeId: Int): Boolean = dbQuery {
        WorkTypes.deleteWhere { WorkTypes.id eq workTypeId } > 0
    }

    override suspend fun deleteWorkType(title: String): Boolean = dbQuery {
        WorkTypes.deleteWhere { WorkTypes.title eq title } > 0
    }
}
