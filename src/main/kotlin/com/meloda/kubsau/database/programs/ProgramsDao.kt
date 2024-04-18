package com.meloda.kubsau.database.programs

import com.meloda.kubsau.database.Dao
import com.meloda.kubsau.model.Program

interface ProgramsDao : Dao<Program> {

    suspend fun allPrograms(): List<Program>
    suspend fun allProgramsByIds(programIds: List<Int>): List<Program>
    suspend fun programsBySemester(semester: Int): List<Program>
    suspend fun singleProgram(programId: Int): Program?

    suspend fun addNewProgram(
        title: String,
        semester: Int
    ): Program?

    suspend fun updateProgram(
        programId: Int,
        title: String,
        semester: Int
    ): Int

    suspend fun deleteProgram(programId: Int): Boolean
    suspend fun deletePrograms(programIds: List<Int>): Boolean
}
