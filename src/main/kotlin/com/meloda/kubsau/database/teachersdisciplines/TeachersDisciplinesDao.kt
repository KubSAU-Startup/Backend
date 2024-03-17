package com.meloda.kubsau.database.teachersdisciplines

import com.meloda.kubsau.database.RefDao
import com.meloda.kubsau.model.Discipline
import com.meloda.kubsau.model.Teacher

interface TeachersDisciplinesDao : RefDao<Teacher, Discipline> {

    suspend fun allItems(): List<Pair<Teacher, Discipline>>

    suspend fun addNewReference(teacherId: Int, disciplineId: Int): Boolean

    suspend fun deleteReference(teacherId: Int?, disciplineId: Int?): Boolean
}
