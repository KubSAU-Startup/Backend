package com.meloda.kubsau.route.users

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.common.getIntList
import com.meloda.kubsau.common.getIntOrThrow
import com.meloda.kubsau.database.users.UsersDao
import com.meloda.kubsau.errors.ContentNotFoundException
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.usersRoutes() {
    authenticate {
        route("/users") {
            getUsers()
            getUserById()
        }
    }
}

private fun Route.getUsers() {
    val usersDao by inject<UsersDao>()

    get {
        val userIds = call.request.queryParameters.getIntList(
            key = "userIds",
            defaultValue = emptyList()
        )

        val users = if (userIds.isEmpty()) {
            usersDao.allUsers()
        } else {
            usersDao.allUsersByIds(userIds)
        }

        respondSuccess { users }
    }
}

private fun Route.getUserById() {
    val usersDao by inject<UsersDao>()

    get("{id}") {
        val userId = call.parameters.getIntOrThrow("id")
        val user = usersDao.singleUser(userId) ?: throw ContentNotFoundException

        respondSuccess { user }
    }
}
