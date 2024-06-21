package com.meloda.kubsau.database.groups

import com.meloda.kubsau.base.FilterableDao
import com.meloda.kubsau.controller.EntryFilter
import com.meloda.kubsau.model.Group

interface GroupDao : FilterableDao<Group, EntryFilter> {

    suspend fun groupExist(groupId: Int): Boolean
    suspend fun allGroups(facultyId: Int?): List<Group>
    suspend fun allGroupsAsFilters(facultyId: Int?): List<EntryFilter>
    suspend fun allGroupsByIds(groupIds: List<Int>): List<Group>
    suspend fun allGroupsByDirectivity(directivityId: Int): List<Group>
    suspend fun singleGroup(groupId: Int): Group?
    suspend fun addNewGroup(title: String, directivityId: Int): Group?
    suspend fun updateGroup(groupId: Int, title: String, directivityId: Int): Boolean
    suspend fun deleteGroup(groupId: Int): Boolean
    suspend fun deleteGroups(groupIds: List<Int>): Boolean
}
