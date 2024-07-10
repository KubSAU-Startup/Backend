package com.meloda.kubsau.repository

import com.meloda.kubsau.database.groups.GroupDao
import com.meloda.kubsau.model.Group

interface GroupRepository {
    suspend fun isGroupExist(groupId: Int): Boolean
    suspend fun getAllGroups(facultyId: Int?): List<Group>
    suspend fun getGroupsByIds(groupIds: List<Int>): List<Group>
    suspend fun getGroupById(groupId: Int): Group?
    suspend fun getGroupsInDirectivity(directivityId: Int): List<Group>
    suspend fun addGroup(title: String, directivityId: Int): Group?
    suspend fun editGroup(groupId: Int, title: String, directivityId: Int): Boolean
    suspend fun deleteGroup(groupId: Int): Boolean
    suspend fun deleteGroups(groupIds: List<Int>): Boolean
}

class GroupRepositoryImpl(private val dao: GroupDao) : GroupRepository {
    override suspend fun isGroupExist(groupId: Int): Boolean = dao.groupExist(groupId)

    override suspend fun getAllGroups(facultyId: Int?): List<Group> = dao.allGroups(facultyId)

    override suspend fun getGroupsByIds(groupIds: List<Int>): List<Group> = dao.allGroupsByIds(groupIds)

    override suspend fun getGroupById(groupId: Int): Group? = dao.singleGroup(groupId)
    override suspend fun getGroupsInDirectivity(directivityId: Int): List<Group> =
        dao.allGroupsByDirectivity(directivityId)

    override suspend fun addGroup(title: String, directivityId: Int): Group? = dao.addNewGroup(title, directivityId)

    override suspend fun editGroup(groupId: Int, title: String, directivityId: Int): Boolean =
        dao.updateGroup(groupId, title, directivityId)

    override suspend fun deleteGroup(groupId: Int): Boolean = dao.deleteGroup(groupId)

    override suspend fun deleteGroups(groupIds: List<Int>): Boolean = dao.deleteGroups(groupIds)
}
