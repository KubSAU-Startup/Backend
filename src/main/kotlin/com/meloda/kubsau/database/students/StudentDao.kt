package com.meloda.kubsau.database.students

import com.meloda.kubsau.base.Dao
import com.meloda.kubsau.model.Student

interface StudentDao : Dao<Student> {

    suspend fun allStudents(facultyId: Int?, offset: Int?, limit: Int?): List<Student>
    suspend fun allStudentsByIds(studentIds: List<Int>): List<Student>
    suspend fun allStudentsByGroupId(groupId: Int): List<Student>
    suspend fun allStudentsByGroupIds(groupIds: List<Int>): List<Student>
    suspend fun allStudentsByGroupIdsAsMap(groupIds: List<Int>, learnersOnly: Boolean?): Map<Int, List<Student>>

    suspend fun allStudentsBySearch(
        facultyId: Int?,
        offset: Int?,
        limit: Int?,
        groupId: Int?,
        gradeId: Int?,
        status: Int?,
        query: String?,
        studentIds: List<Int>?
    ): List<Student>

    suspend fun singleStudent(studentId: Int): Student?

    suspend fun addNewStudent(
        firstName: String,
        lastName: String,
        middleName: String?,
        groupId: Int,
        status: Int
    ): Student?

    suspend fun updateStudent(
        studentId: Int,
        firstName: String,
        lastName: String,
        middleName: String?,
        groupId: Int,
        status: Int
    ): Boolean

    suspend fun deleteStudent(studentId: Int): Boolean
    suspend fun deleteStudents(studentIds: List<Int>): Boolean
}
