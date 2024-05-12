package com.meloda.kubsau.database.students

import com.meloda.kubsau.database.Dao
import com.meloda.kubsau.model.Student

interface StudentsDao : Dao<Student> {

    suspend fun allStudents(offset: Int?, limit: Int?): List<Student>
    suspend fun allStudentsByIds(studentIds: List<Int>): List<Student>
    suspend fun allStudentsByGroupId(groupId: Int): List<Student>
    suspend fun allStudentsByGroupIds(groupIds: List<Int>): List<Student>

    suspend fun allStudentsByFilters(
        offset: Int?,
        limit: Int?,
        groupId: Int?,
        gradeId: Int?,
        statusId: Int?
    ): List<Student>

    suspend fun allStudentsByQuery(offset: Int?, limit: Int?, query: String): List<Student>
    suspend fun singleStudent(studentId: Int): Student?

    suspend fun addNewStudent(
        firstName: String,
        lastName: String,
        middleName: String?,
        groupId: Int,
        statusId: Int
    ): Student?

    suspend fun updateStudent(
        studentId: Int,
        firstName: String,
        lastName: String,
        middleName: String?,
        groupId: Int,
        statusId: Int
    ): Boolean

    suspend fun deleteStudent(studentId: Int): Boolean
    suspend fun deleteStudents(studentIds: List<Int>): Boolean
}
