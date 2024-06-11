package com.meloda.kubsau.database.students

import com.meloda.kubsau.base.Dao
import com.meloda.kubsau.model.Student
import com.meloda.kubsau.model.StudentStatus

interface StudentsDao : Dao<Student> {

    suspend fun allStudents(offset: Int?, limit: Int?): List<Student>
    suspend fun allStudentsByIds(studentIds: List<Int>): List<Student>
    suspend fun allStudentsByGroupId(groupId: Int): List<Student>
    suspend fun allStudentsByGroupIds(groupIds: List<Int>): List<Student>
    suspend fun allStudentsByGroupIdsAsMap(groupIds: List<Int>): Map<Int, List<Student>>

    suspend fun allStudentsBySearch(
        offset: Int?,
        limit: Int?,
        groupId: Int?,
        gradeId: Int?,
        statusId: Int?,
        query: String?
    ): Map<Student, StudentStatus>

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
