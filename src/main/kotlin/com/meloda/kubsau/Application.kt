package com.meloda.kubsau

import com.meloda.kubsau.database.DatabaseController
import com.meloda.kubsau.database.departments.departmentsDao
import com.meloda.kubsau.database.users.usersDao
import com.meloda.kubsau.plugins.configureServer
import kotlinx.coroutines.runBlocking

fun main() {
    DatabaseController.init()
    createDummyDepartments()
    createDummyUsers()
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
                addNewUser(login = "lischenkodev@gmail.com", password = "123456", type = 1, departmentId = 1)
                addNewUser(login = "m.kozhukhar@gmail.com", password = "789012", type = 1, departmentId = 2)
                addNewUser(login = "ya.abros@gmail.com", password = "345678", type = 1, departmentId = 3)
            }
        }
    }
}
