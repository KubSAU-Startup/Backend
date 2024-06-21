package com.meloda.kubsau.database.grades

import com.meloda.kubsau.base.Dao
import com.meloda.kubsau.model.Grade

interface GradeDao : Dao<Grade> {

    suspend fun allGrades(offset: Int?, limit: Int?): List<Grade>
    suspend fun allGradesByIds(gradeIds: List<Int>): List<Grade>
    suspend fun singleGradeById(gradeId: Int): Grade?
    suspend fun addNewGrade(title: String): Grade?
    suspend fun updateGrade(gradeId: Int, title: String): Boolean
    suspend fun deleteGrade(gradeId: Int): Boolean
    suspend fun deleteGrades(gradeIds: List<Int>): Boolean
}
