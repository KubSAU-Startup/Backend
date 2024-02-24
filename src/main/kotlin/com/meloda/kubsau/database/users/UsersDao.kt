package com.meloda.kubsau.database.users

import com.meloda.kubsau.database.Dao
import com.meloda.kubsau.model.User

interface UsersDao : Dao<User> {

    suspend fun allUsers(): List<User>
    suspend fun singleUser(id: Int): User?
    suspend fun singleUser(email: String): User?
    suspend fun addNewUser(email: String, password: String, type: Int, departmentId: Int): User?
    suspend fun editUser(id: Int, email: String, password: String): Boolean
    suspend fun deleteUser(id: Int): Boolean
}
