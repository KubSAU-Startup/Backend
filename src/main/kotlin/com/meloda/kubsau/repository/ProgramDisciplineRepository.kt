package com.meloda.kubsau.repository

import com.meloda.kubsau.database.programsdisciplines.ProgramDisciplineDao

interface ProgramDisciplineRepository {
}

class ProgramDisciplineRepositoryImpl(private val dao: ProgramDisciplineDao) : ProgramDisciplineRepository {

}
