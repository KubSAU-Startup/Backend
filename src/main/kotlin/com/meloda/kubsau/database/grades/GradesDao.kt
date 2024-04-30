package com.meloda.kubsau.database.grades

import com.meloda.kubsau.database.Dao
import com.meloda.kubsau.model.Grade

interface GradesDao : Dao<Grade> {

    suspend fun allGrades(): List<Grade>

    suspend fun addNewGrade(title: String): Grade?
}
