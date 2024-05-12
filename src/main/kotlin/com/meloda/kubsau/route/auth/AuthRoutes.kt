package com.meloda.kubsau.route.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.common.SecretsController
import com.meloda.kubsau.common.getStringOrThrow
import com.meloda.kubsau.database.sessions.SessionsDao
import com.meloda.kubsau.database.users.UsersDao
import com.meloda.kubsau.errors.ContentNotFoundException
import com.meloda.kubsau.errors.UnknownException
import com.meloda.kubsau.model.User
import com.meloda.kubsau.plugins.AUDIENCE
import com.meloda.kubsau.plugins.ISSUER
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.authRoutes() {
    route("/auth") {
        addSession()

        authenticate {
            deleteSession()
        }
    }
}

private fun Route.addSession() {
    val usersDao by inject<UsersDao>()
    val sessionsDao by inject<SessionsDao>()

    post {
        val parameters = call.receiveParameters()

        val login = parameters.getStringOrThrow("login")
        val password = parameters.getStringOrThrow("password")

        val users = usersDao.allUsers()

        val logins = users.map(User::login)
        val passwords = users.map(User::password)

        if (login !in logins) {
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
            .sign(Algorithm.HMAC256(SecretsController.jwtSecret))

        sessionsDao.addNewSession(user.id, accessToken)

        respondSuccess {
            AuthResponse(
                userId = user.id,
                accessToken = accessToken
            )
        }
    }
}

private fun Route.deleteSession() {
    val usersDao by inject<UsersDao>()
    val sessionsDao by inject<SessionsDao>()

    delete {
        val principal = call.principal<JWTPrincipal>()

        val login = principal?.payload?.getClaim("login")?.asString() ?: throw UnknownException
        val userId = usersDao.singleUser(login = login)?.id ?: throw UnknownException
        val session = sessionsDao.singleSession(userId = userId) ?: throw ContentNotFoundException

        if (sessionsDao.deleteSession(
                userId = session.userId,
                accessToken = session.accessToken
            )
        ) {
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
