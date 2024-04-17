package com.meloda.kubsau.route.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.database.sessions.SessionsDao
import com.meloda.kubsau.database.users.UsersDao
import com.meloda.kubsau.errors.UnknownException
import com.meloda.kubsau.errors.ValidationException
import com.meloda.kubsau.plugins.AUDIENCE
import com.meloda.kubsau.plugins.ISSUER
import com.meloda.kubsau.plugins.SECRET
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.authRoutes() {
    route("/auth") {
        getToken()
        getAll()
        deleteUser()
    }
}

private fun Route.getToken() {
    val usersDao by inject<UsersDao>()
    val sessionsDao by inject<SessionsDao>()

    get {
        try {
            val params = call.request.queryParameters
            val login = params["login"] ?: throw ValidationException("login is empty")
            val password = params["password"] ?: throw ValidationException("password is empty")

            val users = usersDao.allUsers()

            val loginsPasswords = users.map { user -> user.login to user.password }

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
    val usersDao by inject<UsersDao>()

    get("all") {
        val users = usersDao.allUsers()
        respondSuccess { users }
    }
}

// TODO: 24/02/2024, Danil Nikolaev: remove or move out
private fun Route.deleteUser() {
    val usersDao by inject<UsersDao>()

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
