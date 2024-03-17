package com.meloda.kubsau.database.programs

import com.meloda.kubsau.database.Dao
import com.meloda.kubsau.model.Program

interface ProgramsDao : Dao<Program> {

    suspend fun allPrograms(): List<Program>
    suspend fun programsBySemester(semester: Int): List<Program>
    suspend fun addNewProgram(
        title: String,
        semester: Int
    ): Program?

    suspend fun deleteProgram(programId: Int): Boolean
}
