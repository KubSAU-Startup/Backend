package com.meloda.kubsau.database.groups

import com.meloda.kubsau.database.FilterableDao
import com.meloda.kubsau.model.Group
import com.meloda.kubsau.route.journal.JournalFilter

interface GroupsDao : FilterableDao<Group, JournalFilter> {

    suspend fun allGroups(): List<Group>
    suspend fun allGroupsAsFilters(): List<JournalFilter>
    suspend fun allGroupsByIds(groupIds: List<Int>): List<Group>
    suspend fun singleGroup(groupId: Int): Group?
    suspend fun addNewGroup(title: String, directivityId: Int): Group?
    suspend fun updateGroup(groupId: Int, title: String, directivityId: Int): Int
    suspend fun deleteGroup(groupId: Int): Boolean
    suspend fun deleteGroups(groupIds: List<Int>): Boolean
}
