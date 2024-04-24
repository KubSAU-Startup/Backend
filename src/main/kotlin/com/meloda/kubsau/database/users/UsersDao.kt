package com.meloda.kubsau.database.users

import com.meloda.kubsau.database.Dao
import com.meloda.kubsau.model.User

interface UsersDao : Dao<User> {

    suspend fun allUsers(): List<User>
    suspend fun allUsersByIds(userIds: List<Int>): List<User>
    suspend fun singleUser(userId: Int): User?
    suspend fun singleUser(login: String): User?
    suspend fun addNewUser(login: String, password: String, type: Int, departmentId: Int): User?
    suspend fun updateUser(userId: Int, login: String, password: String): Int
    suspend fun deleteUser(userId: Int): Boolean
    suspend fun deleteUsers(userIds: List<Int>): Boolean
}
