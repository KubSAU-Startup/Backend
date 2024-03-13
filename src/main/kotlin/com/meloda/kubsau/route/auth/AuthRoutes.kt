package com.meloda.kubsau.route.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.database.sessions.sessionsDao
import com.meloda.kubsau.database.users.usersDao
import com.meloda.kubsau.errors.UnknownException
import com.meloda.kubsau.errors.ValidationException
import com.meloda.kubsau.plugins.AUDIENCE
import com.meloda.kubsau.plugins.ISSUER
import com.meloda.kubsau.plugins.SECRET
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

            val loginsPasswords = users.map { user -> Pair(user.login, user.password) }

            val logins = loginsPasswords.map { it.first }
            val passwords = loginsPasswords.map { it.second }

            if (!logins.contains(login)) {
                throw WrongCredentialsException
            }

            val loginIndex = logins.indexOf(login)

            if (passwords[loginIndex] != password) {
                throw WrongCredentialsException
            }

            val user = users[loginIndex]

            val accessToken = JWT.create()
                .withAudience(AUDIENCE)
                .withIssuer(ISSUER)
                .withClaim("login", login)
                .sign(Algorithm.HMAC256(SECRET))

            sessionsDao.addNewSession(user.id, accessToken)

            respondSuccess {
                AuthResponse(
                    userId = user.id,
                    accessToken = accessToken
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
        val userId = call.parameters["id"]?.toInt() ?: throw ValidationException("id is empty")

        val success = usersDao.deleteUser(userId)
        if (success) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}

private data class AuthResponse(
    val userId: Int,
    val accessToken: String
)

data object WrongCredentialsException : Throwable()
