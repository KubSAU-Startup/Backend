package com.meloda.kubsau.plugins

import com.meloda.kubsau.di.controllerModule
import com.meloda.kubsau.di.databaseModule
import com.meloda.kubsau.di.repositoryModule
import com.meloda.kubsau.di.serviceModule
import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(
            databaseModule,
            repositoryModule,
            serviceModule,
            controllerModule
        )
    }
}
