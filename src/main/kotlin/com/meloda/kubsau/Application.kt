package com.meloda.kubsau

import com.meloda.kubsau.database.DatabaseController
import com.meloda.kubsau.database.departments.departmentsDao
import com.meloda.kubsau.database.sessions.sessionsDao
import com.meloda.kubsau.database.users.usersDao
import com.meloda.kubsau.plugins.configureServer
import kotlinx.coroutines.runBlocking

fun main() {
    DatabaseController.init()
    createDummyDepartments()
    createDummyUsers()
    createDummySessions()
    configureServer()
}

private fun createDummyDepartments() {
    departmentsDao.apply {
        runBlocking {
            if (allDepartments().size < 3) {
                addNewDepartment("Прикладная информатика", "+7 (800) 555-35-35")
                addNewDepartment("Бизнес-информатика", "+7 (800) 555-35-36")
                addNewDepartment("Иностранный язык", "+7 (800) 555-35-37")
            }
        }
    }
}

private fun createDummyUsers() {
    usersDao.apply {
        runBlocking {
            if (allUsers().size < 3) {
                addNewUser(email = "lischenkodev@gmail.com", password = "123456", type = 1, departmentId = 1)
                addNewUser(email = "m.kozhukhar@gmail.com", password = "789012", type = 1, departmentId = 2)
                addNewUser(email = "ya.abros@gmail.com", password = "345678", type = 1, departmentId = 3)
            }
        }
    }
}

private fun createDummySessions() {
    sessionsDao.apply {
        runBlocking {
            if (allSessions().size < 3) {
                val users = usersDao.allUsers().take(3)

                listOf(
                    "lof23ynucoiu23yn4090923cu09823",
                    "oo32iu0239p4u2083u208uc90283uc",
                    "lkne1o28yn9e2od8y129dye91dden1"
                ).forEachIndexed { index, token ->
                    addNewSession(users[index].id, token)
                }
            }
        }
    }
}

