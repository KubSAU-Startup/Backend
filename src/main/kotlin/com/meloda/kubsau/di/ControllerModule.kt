package com.meloda.kubsau.di

import com.meloda.kubsau.controller.UserController
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val controllerModule = module {
    singleOf(::UserController)
}
