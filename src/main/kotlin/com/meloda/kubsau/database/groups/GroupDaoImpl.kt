package com.meloda.kubsau.database.groups

import com.meloda.kubsau.config.DatabaseController.dbQuery
import com.meloda.kubsau.controller.EntryFilter
import com.meloda.kubsau.database.directivities.Directivities
import com.meloda.kubsau.database.faculties.Faculties
import com.meloda.kubsau.database.heads.Heads
import com.meloda.kubsau.model.Group
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

class GroupDaoImpl : GroupDao {

    override suspend fun groupExist(groupId: Int): Boolean = dbQuery {
        Groups
            .select(Groups.id)
            .where { Groups.id eq groupId }
            .singleOrNull() != null
    }

    override suspend fun allGroups(facultyId: Int?): List<Group> = dbQuery {
        val dbQuery = Groups
            .innerJoin(Directivities, { Groups.directivityId }, { Directivities.id })
            .innerJoin(Heads, { Directivities.headId }, { Heads.id })
            .innerJoin(Faculties, { Heads.facultyId }, { Faculties.id })
            .selectAll()

        facultyId?.let { dbQuery.andWhere { Faculties.id eq facultyId } }

        dbQuery.map(::mapResultRow)
    }

    override suspend fun allGroupsAsFilters(facultyId: Int?): List<EntryFilter> = dbQuery {
        val dbQuery = Groups
            .innerJoin(Directivities, { Groups.directivityId }, { Directivities.id })
            .innerJoin(Heads, { Directivities.headId }, { Heads.id })
            .select(Groups.id, Groups.title)

        facultyId?.let { dbQuery.andWhere { Heads.facultyId eq facultyId } }

        dbQuery.map(::mapFilterResultRow)
    }

    override suspend fun allGroupsByIds(groupIds: List<Int>): List<Group> = dbQuery {
        Groups
            .selectAll()
            .where { Groups.id inList groupIds }
            .map(::mapResultRow)
    }

    override suspend fun allGroupsByDirectivity(directivityId: Int): List<Group> = dbQuery {
        Groups
            .selectAll()
            .where { Groups.directivityId eq directivityId }
            .map(::mapResultRow)
    }

    override suspend fun singleGroup(groupId: Int): Group? = dbQuery {
        Groups
            .selectAll()
            .where { Groups.id eq groupId }
            .map(::mapResultRow)
            .singleOrNull()
    }

    override suspend fun addNewGroup(title: String, directivityId: Int): Group? = dbQuery {
        Groups.insert {
            it[Groups.title] = title
            it[Groups.directivityId] = directivityId
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override suspend fun updateGroup(groupId: Int, title: String, directivityId: Int): Boolean = dbQuery {
        Groups.update(where = { Groups.id eq groupId }) {
            it[Groups.title] = title
            it[Groups.directivityId] = directivityId
        } > 0
    }

    override suspend fun deleteGroup(groupId: Int): Boolean = dbQuery {
        Groups.deleteWhere { Groups.id eq groupId } > 0
    }

    override suspend fun deleteGroups(groupIds: List<Int>): Boolean = dbQuery {
        Groups.deleteWhere { Groups.id inList groupIds } > 0
    }

    override fun mapResultRow(row: ResultRow): Group = Group.mapResultRow(row)

    override fun mapFilterResultRow(row: ResultRow): EntryFilter = EntryFilter(
        id = row[Groups.id].value,
        title = row[Groups.title]
    )
}
