package com.meloda.kubsau.di

import com.meloda.kubsau.database.departments.DepartmentsDao
import com.meloda.kubsau.database.departments.DepartmentsDaoImpl
import com.meloda.kubsau.database.disciplines.DisciplinesDao
import com.meloda.kubsau.database.disciplines.DisciplinesDaoImpl
import com.meloda.kubsau.database.groups.GroupsDao
import com.meloda.kubsau.database.groups.GroupsDaoImpl
import com.meloda.kubsau.database.majors.MajorsDao
import com.meloda.kubsau.database.majors.MajorsDaoImpl
import com.meloda.kubsau.database.sessions.SessionsDao
import com.meloda.kubsau.database.sessions.SessionsDaoImpl
import com.meloda.kubsau.database.students.StudentsDao
import com.meloda.kubsau.database.students.StudentsDaoImpl
import com.meloda.kubsau.database.teachers.TeachersDao
import com.meloda.kubsau.database.teachers.TeachersDaoImpl
import com.meloda.kubsau.database.users.UsersDao
import com.meloda.kubsau.database.users.UsersDaoImpl
import com.meloda.kubsau.database.works.WorksDao
import com.meloda.kubsau.database.works.WorksDaoImpl
import com.meloda.kubsau.database.worktypes.WorkTypesDao
import com.meloda.kubsau.database.worktypes.WorkTypesDaoImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val databaseModule = module {
    singleOf(::DepartmentsDaoImpl) bind DepartmentsDao::class
    singleOf(::DisciplinesDaoImpl) bind DisciplinesDao::class
    singleOf(::GroupsDaoImpl) bind GroupsDao::class
    singleOf(::MajorsDaoImpl) bind MajorsDao::class
    singleOf(::SessionsDaoImpl) bind SessionsDao::class
    singleOf(::StudentsDaoImpl) bind StudentsDao::class
    singleOf(::TeachersDaoImpl) bind TeachersDao::class
    singleOf(::UsersDaoImpl) bind UsersDao::class
    singleOf(::WorksDaoImpl) bind WorksDao::class
    singleOf(::WorkTypesDaoImpl) bind WorkTypesDao::class
}
