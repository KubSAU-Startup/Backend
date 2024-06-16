package com.meloda.kubsau.di

import com.meloda.kubsau.controller.GroupController
import com.meloda.kubsau.controller.UserController
import com.meloda.kubsau.controller.WorkController
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val controllerModule = module {
    singleOf(::UserController)
    singleOf(::WorkController)
    singleOf(::GroupController)
}
