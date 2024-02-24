package com.meloda.kubsau.route.auth

import com.meloda.kubsau.base.respondSuccess
import com.meloda.kubsau.database.usersDao
import com.meloda.kubsau.dummy.DUMMY_TOKENS
import com.meloda.kubsau.errors.UnknownException
import com.meloda.kubsau.errors.ValidationException
import com.meloda.kubsau.model.User
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Route.auth() {
    route("/auth") {
        getToken()
        getAll()
        deleteUser()
    }
}

private fun Route.getToken() {
    get {
        try {
            val params = call.request.queryParameters
            val login = params["login"]
            val password = params["password"]

            val users = usersDao.allUsers()

            val logins = users.map(User::email)
            val passwords = users.map(User::password)

            if (!logins.contains(login)) {
                throw WrongCredentialsException
            }

            val loginIndex = logins.indexOf(login)

            if (passwords[loginIndex] != password) {
                throw WrongCredentialsException
            }

            val userToken = DUMMY_TOKENS[loginIndex]

            respondSuccess {
                AuthResponse(
                    userId = loginIndex + 1,
                    accessToken = userToken
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}

private fun Route.getAll() {
    get("all") {
        val users = usersDao.allUsers()
        respondSuccess { users }
    }
}

// TODO: 24/02/2024, Danil Nikolaev: remove or move out
private fun Route.deleteUser() {
    delete("{id}") {
        val userId = call.parameters["id"]?.toInt()

        if (userId == null) {
            throw ValidationException("id is empty")
        } else {
            val success = usersDao.deleteUser(userId)
            if (success) {
                respondSuccess { 1 }
            } else {
                throw UnknownException
            }
        }
    }
}

private data class AuthResponse(
    val userId: Int,
    val accessToken: String
)

data object WrongCredentialsException : Throwable()
