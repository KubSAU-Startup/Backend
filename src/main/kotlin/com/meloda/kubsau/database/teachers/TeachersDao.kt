package com.meloda.kubsau.database.teachers

import com.meloda.kubsau.database.Dao
import com.meloda.kubsau.model.Teacher

interface TeachersDao : Dao<Teacher> {

    suspend fun allTeachers(): List<Teacher>
    suspend fun allTeachersByIds(teacherIds: List<Int>): List<Teacher>
    suspend fun singleTeacher(teacherId: Int): Teacher?

    suspend fun addNewTeacher(
        firstName: String,
        lastName: String,
        middleName: String,
        departmentId: Int
    ): Teacher?

    suspend fun updateTeacher(
        teacherId: Int,
        firstName: String,
        lastName: String,
        middleName: String,
        departmentId: Int
    ): Int

    suspend fun deleteTeacher(teacherId: Int): Boolean
    suspend fun deleteTeachers(teacherIds: List<Int>): Boolean
}
