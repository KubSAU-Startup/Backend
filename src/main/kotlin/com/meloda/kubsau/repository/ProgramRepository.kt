package com.meloda.kubsau.repository

import com.meloda.kubsau.database.programs.ProgramDao

interface ProgramRepository {
}

class ProgramRepositoryImpl(private val programDao: ProgramDao) : ProgramRepository {

}
