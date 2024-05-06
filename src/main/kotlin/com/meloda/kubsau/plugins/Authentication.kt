package com.meloda.kubsau.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.meloda.kubsau.PORT
import com.meloda.kubsau.common.AuthController
import com.meloda.kubsau.errors.SessionExpiredException
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

val ISSUER = "http://0.0.0.0:$PORT/"
val AUDIENCE = "http://0.0.0.0:$PORT/auth"
const val REALM = "Access to data"

fun Application.configureAuthentication() {
    install(Authentication) {
        jwt {
            realm = REALM

            verifier(
                JWT
                    .require(Algorithm.HMAC256(AuthController.jwtSecret))
                    .withAudience(AUDIENCE)
                    .withIssuer(ISSUER)
                    .build()
            )

            validate { credential ->
                if (credential.payload.getClaim("login").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }

            challenge { _, _ -> throw SessionExpiredException }
        }
    }
}
