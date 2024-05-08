package com.meloda.kubsau.database.worktypes

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.model.WorkType
import com.meloda.kubsau.route.works.EntryFilter
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

class WorkTypesDaoImpl : WorkTypesDao {

    override suspend fun allWorkTypes(): List<WorkType> = dbQuery {
        WorkTypes.selectAll().map(::mapResultRow)
    }

    override suspend fun allWorkTypesAsFilters(): List<EntryFilter> = dbQuery {
        WorkTypes
            .select(WorkTypes.id, WorkTypes.title)
            .map(::mapFilterResultRow)
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

    override suspend fun addNewWorkType(title: String, needTitle: Boolean): WorkType? = dbQuery {
        WorkTypes.insert {
            it[WorkTypes.title] = title
            it[WorkTypes.needTitle] = needTitle
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override suspend fun updateWorkType(workTypeId: Int, title: String, needTitle: Boolean): Boolean = dbQuery {
        WorkTypes.update(where = { WorkTypes.id eq workTypeId }) {
            it[WorkTypes.title] = title
            it[WorkTypes.needTitle] = needTitle
        } > 0
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

    override fun mapFilterResultRow(row: ResultRow): EntryFilter = EntryFilter(
        id = row[WorkTypes.id].value,
        title = row[WorkTypes.title]
    )
}
