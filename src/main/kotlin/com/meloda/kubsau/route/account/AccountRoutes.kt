package com.meloda.kubsau.route.account

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.database.sessions.sessionsDao
import com.meloda.kubsau.database.users.usersDao
import com.meloda.kubsau.errors.SessionExpiredException
import com.meloda.kubsau.errors.UnknownException
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import kotlin.random.Random

fun Route.account() {
    route("/account") {
        getAccountInfoRoute()
        getAllTokensRoute()
    }
}

private fun Route.getAccountInfoRoute() {
    authenticate {
        get {
            val principal = call.principal<JWTPrincipal>()

            val login = principal?.payload?.getClaim("login")?.asString() ?: throw UnknownException

            val userId = usersDao.singleUser(login = login)?.id ?: throw UnknownException

            val session = sessionsDao.singleSession(userId = userId) ?: throw SessionExpiredException
            val user = usersDao.singleUser(id = session.userId) ?: throw UnknownException

            respondSuccess {
                AccountInfo(
                    id = user.id,
                    type = Random.nextInt(),
                    login = user.login,
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
    val login: String,
    val departmentId: Int
)
