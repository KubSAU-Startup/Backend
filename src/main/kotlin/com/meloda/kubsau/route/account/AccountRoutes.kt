package com.meloda.kubsau.route.account

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.database.sessions.SessionsDao
import com.meloda.kubsau.database.users.UsersDao
import com.meloda.kubsau.errors.SessionExpiredException
import com.meloda.kubsau.errors.UnknownException
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.accountRoutes() {
    authenticate {
        route("/account") {
            getAccountInfoRoute()
        }
    }
}

private fun Route.getAccountInfoRoute() {
    val usersDao by inject<UsersDao>()
    val sessionsDao by inject<SessionsDao>()

    get {
        val principal = call.principal<JWTPrincipal>()

        val login = principal?.payload?.getClaim("login")?.asString() ?: throw UnknownException

        val userId = usersDao.singleUser(login = login)?.id ?: throw UnknownException

        val session = sessionsDao.singleSession(userId = userId) ?: throw SessionExpiredException
        val user = usersDao.singleUser(id = session.userId) ?: throw UnknownException

        respondSuccess {
            AccountInfo(
                id = user.id,
                type = user.type,
                login = user.login,
                departmentId = user.departmentId
            )
        }
    }
}

private data class AccountInfo(
    val id: Int,
    val type: Int,
    val login: String,
    val departmentId: Int
)
