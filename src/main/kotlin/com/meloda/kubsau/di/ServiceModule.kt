package com.meloda.kubsau.di

import com.meloda.kubsau.service.UserService
import com.meloda.kubsau.service.UserServiceImpl
import com.meloda.kubsau.service.WorkService
import com.meloda.kubsau.service.WorkServiceImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val serviceModule = module {
    singleOf(::UserServiceImpl) bind UserService::class
    singleOf(::WorkServiceImpl) bind WorkService::class
}
