package com.meloda.kubsau.route.users

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.database.users.UsersDao
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.users() {
    authenticate {
        route("/users") {
            getAllUsers()
        }
    }
}

private fun Route.getAllUsers() {
    val usersDao by inject<UsersDao>()

    get {
        val users = usersDao.allUsers()

        respondSuccess { users }
    }
}
