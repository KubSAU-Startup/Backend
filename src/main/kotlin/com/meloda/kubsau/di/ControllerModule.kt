package com.meloda.kubsau.di

import com.meloda.kubsau.controller.*
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val controllerModule = module {
    singleOf(::UserController)
    singleOf(::WorkController)
    singleOf(::GroupController)
    singleOf(::DepartmentController)
    singleOf(::DirectivityController)
    singleOf(::HeadController)
}
