package com.meloda.kubsau.route.users

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.database.users.UsersDao
import com.meloda.kubsau.errors.ContentNotFoundException
import com.meloda.kubsau.errors.ValidationException
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
        val userIds = call.request.queryParameters["userIds"]
            ?.split(",")
            ?.map(String::trim)
            ?.mapNotNull(String::toIntOrNull)
            ?: emptyList()

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
        val userId = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("id is empty")
        val user = usersDao.singleUser(userId) ?: throw ContentNotFoundException

        respondSuccess { user }
    }
}
