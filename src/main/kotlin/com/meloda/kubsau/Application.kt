package com.meloda.kubsau

import com.meloda.kubsau.database.DatabaseSingleton
import com.meloda.kubsau.database.sessionsDao
import com.meloda.kubsau.database.usersDao
import com.meloda.kubsau.dummy.DUMMY_TOKENS
import com.meloda.kubsau.dummy.DUMMY_USERS
import com.meloda.kubsau.plugins.configureServer
import kotlinx.coroutines.runBlocking

fun main() {
    DatabaseSingleton.init()
    createDummyUsers()
    createDummySessions()
    configureServer()
}

private fun createDummyUsers() {
    usersDao.apply {
        runBlocking {
            if (allUsers().size < DUMMY_USERS.size) {
                DUMMY_USERS.forEach { addNewUser(it.first, it.second) }
            }
        }
    }
}

private fun createDummySessions() {
    sessionsDao.apply {
        runBlocking {
            if (allSessions().size < DUMMY_TOKENS.size) {
                val users = usersDao.allUsers().take(3)

                DUMMY_TOKENS.forEachIndexed { index, token ->
                    addNewSession(users[index].id, token)
                }
            }
        }
    }
}

