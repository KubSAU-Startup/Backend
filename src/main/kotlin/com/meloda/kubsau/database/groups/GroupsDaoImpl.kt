package com.meloda.kubsau.database.groups

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.model.Group
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

class GroupsDaoImpl : GroupsDao {

    override suspend fun allGroups(): List<Group> = dbQuery {
        Groups.selectAll().map(::mapResultRow)
    }

    override suspend fun allGroupsByIds(groupIds: List<Int>): List<Group> = dbQuery {
        Groups
            .selectAll()
            .where { Groups.id inList groupIds }
            .map(::mapResultRow)
    }

    override suspend fun singleGroup(groupId: Int): Group? = dbQuery {
        Groups
            .selectAll()
            .where { Groups.id eq groupId }
            .map(::mapResultRow)
            .singleOrNull()
    }

    override suspend fun addNewGroup(title: String, majorId: Int): Group? = dbQuery {
        Groups.insert {
            it[Groups.title] = title
            it[Groups.majorId] = majorId
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override suspend fun updateGroup(groupId: Int, title: String, majorId: Int): Int = dbQuery {
        Groups.update(where = { Groups.id eq groupId }) {
            it[Groups.title] = title
            it[Groups.majorId] = majorId
        }
    }

    override suspend fun deleteGroup(groupId: Int): Boolean = dbQuery {
        Groups.deleteWhere { Groups.id eq groupId } > 0
    }

    override suspend fun deleteGroups(groupIds: List<Int>): Boolean = dbQuery {
        Groups.deleteWhere { Groups.id inList groupIds } > 0
    }

    override fun mapResultRow(row: ResultRow): Group = Group.mapResultRow(row)
}
