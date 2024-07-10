package com.meloda.kubsau.di

import com.meloda.kubsau.repository.*
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryModule = module {
    singleOf(::UserRepositoryImpl) bind UserRepository::class
    singleOf(::WorkRepositoryImpl) bind WorkRepository::class
    singleOf(::DepartmentRepositoryImpl) bind DepartmentRepository::class
    singleOf(::DirectivityRepositoryImpl) bind DirectivityRepository::class
    singleOf(::DisciplineRepositoryImpl) bind DisciplineRepository::class
    singleOf(::EmployeeDepartmentRepositoryImpl) bind EmployeeDepartmentRepository::class
    singleOf(::EmployeeRepositoryImpl) bind EmployeeRepository::class
    singleOf(::GroupRepositoryImpl) bind GroupRepository::class
    singleOf(::HeadRepositoryImpl) bind HeadRepository::class
}
