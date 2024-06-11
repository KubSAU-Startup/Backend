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
                credential.payload.getClaim("id").asInt()?.let { userId ->
                    userDao.singleUser(userId)?.let { id ->
                        try {
                            UserPrincipal(
                                user = id,
                                type = credential.payload.getClaim("type").asInt(),
                                facultyId = credential.payload.getClaim("facultyId")?.asInt(),
                                departmentId = credential.payload.getClaim("departmentId")?.asInt()
                            )
                        } catch (e: Exception) {
                            null
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
    val departmentId: Int?,
) : Principal
