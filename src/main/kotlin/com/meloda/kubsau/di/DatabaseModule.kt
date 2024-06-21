package com.meloda.kubsau.di

import com.meloda.kubsau.database.departmentfaculty.DepartmentsFacultiesDao
import com.meloda.kubsau.database.departmentfaculty.DepartmentsFacultiesDaoImpl
import com.meloda.kubsau.database.departments.DepartmentDao
import com.meloda.kubsau.database.departments.DepartmentDaoImpl
import com.meloda.kubsau.database.directivities.DirectivityDao
import com.meloda.kubsau.database.directivities.DirectivityDaoImpl
import com.meloda.kubsau.database.disciplines.DisciplineDao
import com.meloda.kubsau.database.disciplines.DisciplineDaoImpl
import com.meloda.kubsau.database.employees.EmployeeDao
import com.meloda.kubsau.database.employees.EmployeeDaoImpl
import com.meloda.kubsau.database.employeesdepartments.EmployeeDepartmentDao
import com.meloda.kubsau.database.employeesdepartments.EmployeeDepartmentDaoImpl
import com.meloda.kubsau.database.employeesfaculties.EmployeeFacultyDao
import com.meloda.kubsau.database.employeesfaculties.EmployeeFacultyDaoImpl
import com.meloda.kubsau.database.faculties.FacultyDao
import com.meloda.kubsau.database.faculties.FacultyDaoImpl
import com.meloda.kubsau.database.grades.GradeDao
import com.meloda.kubsau.database.grades.GradeDaoImpl
import com.meloda.kubsau.database.groups.GroupDao
import com.meloda.kubsau.database.groups.GroupDaoImpl
import com.meloda.kubsau.database.heads.HeadDao
import com.meloda.kubsau.database.heads.HeadDaoImpl
import com.meloda.kubsau.database.programs.ProgramDao
import com.meloda.kubsau.database.programs.ProgramDaoImpl
import com.meloda.kubsau.database.programsdisciplines.ProgramDisciplineDao
import com.meloda.kubsau.database.programsdisciplines.ProgramDisciplineDaoImpl
import com.meloda.kubsau.database.students.StudentDao
import com.meloda.kubsau.database.students.StudentDaoImpl
import com.meloda.kubsau.database.users.UserDao
import com.meloda.kubsau.database.users.UserDaoImpl
import com.meloda.kubsau.database.works.WorkDao
import com.meloda.kubsau.database.works.WorkDaoImpl
import com.meloda.kubsau.database.worktypes.WorkTypeDao
import com.meloda.kubsau.database.worktypes.WorkTypeDaoImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val databaseModule = module {
    singleOf(::DepartmentDaoImpl) bind DepartmentDao::class
    singleOf(::DirectivityDaoImpl) bind DirectivityDao::class
    singleOf(::DisciplineDaoImpl) bind DisciplineDao::class
    singleOf(::EmployeeDaoImpl) bind EmployeeDao::class
    singleOf(::EmployeeDepartmentDaoImpl) bind EmployeeDepartmentDao::class
    singleOf(::EmployeeFacultyDaoImpl) bind EmployeeFacultyDao::class
    singleOf(::FacultyDaoImpl) bind FacultyDao::class
    singleOf(::GradeDaoImpl) bind GradeDao::class
    singleOf(::GroupDaoImpl) bind GroupDao::class
    singleOf(::HeadDaoImpl) bind HeadDao::class
    singleOf(::ProgramDaoImpl) bind ProgramDao::class
    singleOf(::ProgramDisciplineDaoImpl) bind ProgramDisciplineDao::class
    singleOf(::StudentDaoImpl) bind StudentDao::class
    singleOf(::UserDaoImpl) bind UserDao::class
    singleOf(::WorkDaoImpl) bind WorkDao::class
    singleOf(::WorkTypeDaoImpl) bind WorkTypeDao::class
    singleOf(::DepartmentsFacultiesDaoImpl) bind DepartmentsFacultiesDao::class
}
