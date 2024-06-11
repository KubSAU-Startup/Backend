package com.meloda.kubsau.di

import com.meloda.kubsau.database.departments.DepartmentsDao
import com.meloda.kubsau.database.departments.DepartmentsDaoImpl
import com.meloda.kubsau.database.directivities.DirectivitiesDao
import com.meloda.kubsau.database.directivities.DirectivitiesDaoImpl
import com.meloda.kubsau.database.disciplines.DisciplinesDao
import com.meloda.kubsau.database.disciplines.DisciplinesDaoImpl
import com.meloda.kubsau.database.employees.EmployeeDao
import com.meloda.kubsau.database.employees.EmployeeDaoImpl
import com.meloda.kubsau.database.employeesdepartments.EmployeesDepartmentsDao
import com.meloda.kubsau.database.employeesdepartments.EmployeesDepartmentsDaoImpl
import com.meloda.kubsau.database.employeesfaculties.EmployeesFacultiesDao
import com.meloda.kubsau.database.employeesfaculties.EmployeesFacultiesDaoImpl
import com.meloda.kubsau.database.faculties.FacultiesDao
import com.meloda.kubsau.database.faculties.FacultiesDaoImpl
import com.meloda.kubsau.database.grades.GradesDao
import com.meloda.kubsau.database.grades.GradesDaoImpl
import com.meloda.kubsau.database.groups.GroupsDao
import com.meloda.kubsau.database.groups.GroupsDaoImpl
import com.meloda.kubsau.database.heads.HeadsDao
import com.meloda.kubsau.database.heads.HeadsDaoImpl
import com.meloda.kubsau.database.programs.ProgramsDao
import com.meloda.kubsau.database.programs.ProgramsDaoImpl
import com.meloda.kubsau.database.programsdisciplines.ProgramsDisciplinesDao
import com.meloda.kubsau.database.programsdisciplines.ProgramsDisciplinesDaoImpl
import com.meloda.kubsau.database.students.StudentsDao
import com.meloda.kubsau.database.students.StudentsDaoImpl
import com.meloda.kubsau.database.studentstatuses.StudentStatusesDao
import com.meloda.kubsau.database.studentstatuses.StudentStatusesDaoImpl
import com.meloda.kubsau.database.users.UserDao
import com.meloda.kubsau.database.users.UserDaoImpl
import com.meloda.kubsau.database.works.WorksDao
import com.meloda.kubsau.database.works.WorksDaoImpl
import com.meloda.kubsau.database.worktypes.WorkTypesDao
import com.meloda.kubsau.database.worktypes.WorkTypesDaoImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val databaseModule = module {
    singleOf(::DepartmentsDaoImpl) bind DepartmentsDao::class
    singleOf(::DirectivitiesDaoImpl) bind DirectivitiesDao::class
    singleOf(::DisciplinesDaoImpl) bind DisciplinesDao::class
    singleOf(::EmployeeDaoImpl) bind EmployeeDao::class
    singleOf(::EmployeesDepartmentsDaoImpl) bind EmployeesDepartmentsDao::class
    singleOf(::EmployeesFacultiesDaoImpl) bind EmployeesFacultiesDao::class
    singleOf(::FacultiesDaoImpl) bind FacultiesDao::class
    singleOf(::GradesDaoImpl) bind GradesDao::class
    singleOf(::GroupsDaoImpl) bind GroupsDao::class
    singleOf(::HeadsDaoImpl) bind HeadsDao::class
    singleOf(::ProgramsDaoImpl) bind ProgramsDao::class
    singleOf(::ProgramsDisciplinesDaoImpl) bind ProgramsDisciplinesDao::class
    singleOf(::StudentsDaoImpl) bind StudentsDao::class
    singleOf(::StudentStatusesDaoImpl) bind StudentStatusesDao::class
    singleOf(::UserDaoImpl) bind UserDao::class
    singleOf(::WorksDaoImpl) bind WorksDao::class
    singleOf(::WorkTypesDaoImpl) bind WorkTypesDao::class
}
