package com.meloda.kubsau.database.worktypes

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.model.WorkType
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

class WorkTypesDaoImpl : WorkTypesDao {

    override suspend fun allWorkTypes(): List<WorkType> = dbQuery {
        WorkTypes.selectAll().map(::mapResultRow)
    }

    override suspend fun allWorkTypesByIds(workTypeIds: List<Int>): List<WorkType> = dbQuery {
        WorkTypes
            .selectAll()
            .where { WorkTypes.id inList workTypeIds }
            .map(::mapResultRow)
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

    override suspend fun updateWorkType(workTypeId: Int, title: String, isEditable: Boolean): Int = dbQuery {
        WorkTypes.update(where = { WorkTypes.id eq workTypeId }) {
            it[WorkTypes.title] = title
            it[WorkTypes.isEditable] = if (isEditable) 1 else 0
        }
    }

    override suspend fun deleteWorkType(workTypeId: Int): Boolean = dbQuery {
        WorkTypes.deleteWhere { WorkTypes.id eq workTypeId } > 0
    }

    override suspend fun deleteWorkType(title: String): Boolean = dbQuery {
        WorkTypes.deleteWhere { WorkTypes.title eq title } > 0
    }

    override suspend fun deleteWorkTypes(workTypeIds: List<Int>): Boolean = dbQuery {
        WorkTypes.deleteWhere { WorkTypes.id inList workTypeIds } > 0
    }

    override fun mapResultRow(row: ResultRow): WorkType = WorkType.mapResultRow(row)
}
