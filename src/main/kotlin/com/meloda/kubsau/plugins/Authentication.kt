package com.meloda.kubsau.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.meloda.kubsau.PORT
import com.meloda.kubsau.config.SecretsController
import com.meloda.kubsau.database.users.UserDao
import com.meloda.kubsau.model.User
import com.meloda.kubsau.model.WrongTokenFormatException
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.koin.ktor.ext.inject

val ISSUER = "http://0.0.0.0:$PORT/"
val AUDIENCE = "http://0.0.0.0:$PORT/auth"
const val REALM = "Access to data"

fun Application.configureAuthentication() {
    val userDao by inject<UserDao>()

    install(Authentication) {
        jwt {
            realm = REALM

            verifier(
                JWT
                    .require(Algorithm.HMAC256(SecretsController.jwtSecret))
                    .withAudience(AUDIENCE)
                    .withIssuer(ISSUER)
                    .build()
            )

            validate { credential ->
                credential.payload.run {
                    getClaim("id")?.asInt()?.let { userId ->
                        userDao.singleUser(userId)?.let { id ->
                            try {
                                UserPrincipal(
                                    user = id,
                                    type = getClaim("type").asInt(),
                                    facultyId = getClaim("facultyId")?.asInt(),
                                    selectedDepartmentId = getClaim("selectedDepartmentId")?.asInt(),
                                    departmentIds = getClaim("departmentIds").asString().split(", ")
                                        .mapNotNull { it.trim().toIntOrNull() }
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                                null
                            }
                        }
                    }
                }
            }

            challenge { _, _ -> throw WrongTokenFormatException }
        }
    }
}

data class UserPrincipal(
    val user: User,
    val type: Int,
    val facultyId: Int?,
    val selectedDepartmentId: Int?,
    val departmentIds: List<Int>
) : Principal
