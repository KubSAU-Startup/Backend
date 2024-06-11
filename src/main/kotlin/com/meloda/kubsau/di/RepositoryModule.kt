package com.meloda.kubsau.di

import com.meloda.kubsau.repository.UserRepository
import com.meloda.kubsau.repository.UserRepositoryImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryModule = module {
    singleOf(::UserRepositoryImpl) bind UserRepository::class
}
