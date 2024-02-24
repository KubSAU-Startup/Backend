package com.meloda.kubsau.route.account

import com.meloda.kubsau.base.respondSuccess
import com.meloda.kubsau.database.sessionsDao
import com.meloda.kubsau.database.usersDao
import com.meloda.kubsau.errors.NoAccessTokenException
import com.meloda.kubsau.errors.SessionExpiredException
import com.meloda.kubsau.errors.UnknownException
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlin.random.Random

fun Route.account() {
    route("/account") {
        getAccountInfoRoute()
        getAllTokensRoute()
    }
}

private fun Route.getAccountInfoRoute() {
    get {
        // TODO: 24/02/2024, Danil Nikolaev: find better way to parse token
        val accessToken = call.request.headers["Authorization"]?.split("Bearer ")?.get(1)

        if (accessToken == null) {
            throw NoAccessTokenException
        } else {
            val session = sessionsDao.singleSession(accessToken = accessToken) ?: throw SessionExpiredException
            val user = usersDao.singleUser(id = session.userId) ?: throw UnknownException

            respondSuccess {
                AccountInfo(
                    id = user.id,
                    type = Random.nextInt(),
                    email = user.email,
                    departmentId = Random.nextInt()
                )
            }
        }
    }
}

private fun Route.getAllTokensRoute() {
    get("all") {
        val sessions = sessionsDao.allSessions()
        respondSuccess { sessions }
    }
}

private data class AccountInfo(
    val id: Int,
    val type: Int,
    val email: String,
    val departmentId: Int
)
