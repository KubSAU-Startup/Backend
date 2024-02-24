package com.meloda.kubsau.database

import com.meloda.kubsau.model.User

interface UsersDao {

    suspend fun allUsers(): List<User>
    suspend fun singleUser(id: Int): User?
    suspend fun singleUser(email: String): User?
    suspend fun addNewUser(email: String, password: String): User?
    suspend fun editUser(id: Int, email: String, password: String): Boolean
    suspend fun deleteUser(id: Int): Boolean
}
