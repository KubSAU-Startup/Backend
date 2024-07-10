package com.meloda.kubsau.service

import com.meloda.kubsau.model.AccountInfo
import com.meloda.kubsau.model.User
import com.meloda.kubsau.plugins.UserPrincipal
import com.meloda.kubsau.repository.UserRepository

interface UserService {
    suspend fun getAllUsers(): List<User>
    suspend fun getUsersByIds(userIds: List<Int>): List<User>
    suspend fun getUserById(userId: Int): User?
    suspend fun getAccountInfo(principal: UserPrincipal): AccountInfo?

    suspend fun updateAccountInfo(
        principal: UserPrincipal,
        currentPassword: String,
        newPassword: String
    ): Boolean
}

class UserServiceImpl(private val userRepository: UserRepository) : UserService {
    override suspend fun getAllUsers(): List<User> = userRepository.getAllUsers()
    override suspend fun getUsersByIds(userIds: List<Int>): List<User> = userRepository.getUsersByIds(userIds)
    override suspend fun getUserById(userId: Int): User? = userRepository.getUserById(userId)

    override suspend fun getAccountInfo(principal: UserPrincipal): AccountInfo? =
        userRepository.getAccountInfo(principal)

    override suspend fun updateAccountInfo(
        principal: UserPrincipal,
        currentPassword: String,
        newPassword: String
    ): Boolean = userRepository.updateAccountInfo(principal, currentPassword, newPassword)
}
