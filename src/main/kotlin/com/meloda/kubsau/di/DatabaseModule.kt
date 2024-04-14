package com.meloda.kubsau.di

import com.meloda.kubsau.database.departments.DepartmentsDao
import com.meloda.kubsau.database.departments.DepartmentsDaoImpl
import com.meloda.kubsau.database.disciplines.DisciplinesDao
import com.meloda.kubsau.database.disciplines.DisciplinesDaoImpl
import com.meloda.kubsau.database.groups.GroupsDao
import com.meloda.kubsau.database.groups.GroupsDaoImpl
import com.meloda.kubsau.database.journals.JournalsDao
import com.meloda.kubsau.database.journals.JournalsDaoImpl
import com.meloda.kubsau.database.majors.MajorsDao
import com.meloda.kubsau.database.majors.MajorsDaoImpl
import com.meloda.kubsau.database.programs.ProgramsDao
import com.meloda.kubsau.database.programs.ProgramsDaoImpl
import com.meloda.kubsau.database.programsdisciplines.ProgramsDisciplinesDao
import com.meloda.kubsau.database.programsdisciplines.ProgramsDisciplinesDaoImpl
import com.meloda.kubsau.database.sessions.SessionsDao
import com.meloda.kubsau.database.sessions.SessionsDaoImpl
import com.meloda.kubsau.database.specializations.SpecializationsDao
import com.meloda.kubsau.database.specializations.SpecializationsDaoImpl
import com.meloda.kubsau.database.specializationsdisciplines.SpecializationsDisciplinesDao
import com.meloda.kubsau.database.specializationsdisciplines.SpecializationsDisciplinesDaoImpl
import com.meloda.kubsau.database.students.StudentsDao
import com.meloda.kubsau.database.students.StudentsDaoImpl
import com.meloda.kubsau.database.teachers.TeachersDao
import com.meloda.kubsau.database.teachers.TeachersDaoImpl
import com.meloda.kubsau.database.teachersdisciplines.TeachersDisciplinesDao
import com.meloda.kubsau.database.teachersdisciplines.TeachersDisciplinesDaoImpl
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
    singleOf(::JournalsDaoImpl) bind JournalsDao::class
    singleOf(::MajorsDaoImpl) bind MajorsDao::class
    singleOf(::ProgramsDaoImpl) bind ProgramsDao::class
    singleOf(::ProgramsDisciplinesDaoImpl) bind ProgramsDisciplinesDao::class
    singleOf(::SessionsDaoImpl) bind SessionsDao::class
    singleOf(::SpecializationsDaoImpl) bind SpecializationsDao::class
    singleOf(::SpecializationsDisciplinesDaoImpl) bind SpecializationsDisciplinesDao::class
    singleOf(::StudentsDaoImpl) bind StudentsDao::class
    singleOf(::TeachersDaoImpl) bind TeachersDao::class
    singleOf(::TeachersDisciplinesDaoImpl) bind TeachersDisciplinesDao::class
    singleOf(::UsersDaoImpl) bind UsersDao::class
    singleOf(::WorksDaoImpl) bind WorksDao::class
    singleOf(::WorkTypesDaoImpl) bind WorkTypesDao::class
}
