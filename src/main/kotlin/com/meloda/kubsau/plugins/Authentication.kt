package com.meloda.kubsau.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.meloda.kubsau.PORT
import com.meloda.kubsau.errors.SessionExpiredException
import com.meloda.kubsau.startTime
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

// TODO: 14/03/2024, Danil Nikolaev: extract to environment variables
const val SECRET = "bdb979c8ff03d1e206b33c81206b72d54edd627f26dfe98d93aab1b202b92817"
val ISSUER = "http://0.0.0.0:$PORT/"
val AUDIENCE = "http://0.0.0.0:$PORT/auth"
const val REALM = "Access to data"

fun Application.configureAuthentication() {
    println("SECRET: $SECRET")
    install(Authentication) {
        jwt {
            realm = REALM

            verifier(
                JWT
                    .require(Algorithm.HMAC256(SECRET))
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

    val endTime = System.currentTimeMillis()
    val difference = endTime - startTime


    println("Server is ready in ${difference}ms")
}
