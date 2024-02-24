package com.meloda.kubsau.database

import com.meloda.kubsau.model.Session

interface SessionsDao {

    suspend fun allSessions(): List<Session>
    suspend fun singleSession(userId: Int): Session?
    suspend fun singleSession(accessToken: String): Session?
    suspend fun addNewSession(userId: Int, accessToken: String): Session?
    suspend fun deleteSession(userId: Int, accessToken: String): Boolean
}
