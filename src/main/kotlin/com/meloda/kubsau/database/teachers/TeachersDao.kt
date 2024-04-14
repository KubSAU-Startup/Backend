package com.meloda.kubsau.database.teachers

import com.meloda.kubsau.database.Dao
import com.meloda.kubsau.model.Teacher

interface TeachersDao : Dao<Teacher> {

    suspend fun allTeachers(): List<Teacher>
    suspend fun singleTeacher(teacherId: Int): Teacher?
    suspend fun addNewTeacher(
        firstName: String,
        lastName: String,
        middleName: String,
        departmentId: Int
    ): Teacher?

    suspend fun deleteTeacher(teacherId: Int): Boolean
}
