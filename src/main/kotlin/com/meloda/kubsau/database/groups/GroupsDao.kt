package com.meloda.kubsau.database.groups

import com.meloda.kubsau.database.Dao
import com.meloda.kubsau.model.Group

interface GroupsDao : Dao<Group> {

    suspend fun allGroups(): List<Group>
    suspend fun singleGroup(groupId: Int): Group?
    suspend fun addNewGroup(title: String, majorId: Int): Group?
    suspend fun deleteGroup(groupId: Int): Boolean
}
