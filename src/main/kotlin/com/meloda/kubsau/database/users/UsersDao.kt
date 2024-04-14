package com.meloda.kubsau.database.users

import com.meloda.kubsau.database.Dao
import com.meloda.kubsau.model.User

interface UsersDao : Dao<User> {

    suspend fun allUsers(): List<User>
    suspend fun singleUser(id: Int): User?
    suspend fun singleUser(login: String): User?
    suspend fun addNewUser(login: String, password: String, type: Int, departmentId: Int): User?
    suspend fun editUser(id: Int, login: String, password: String): Boolean
    suspend fun deleteUser(id: Int): Boolean
}
