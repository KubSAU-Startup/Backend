package com.meloda.kubsau.di

import com.meloda.kubsau.service.*
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val serviceModule = module {
    singleOf(::UserServiceImpl) bind UserService::class
    singleOf(::WorkServiceImpl) bind WorkService::class
    singleOf(::DepartmentServiceImpl) bind DepartmentService::class
    singleOf(::DirectivityServiceImpl) bind DirectivityService::class
}
