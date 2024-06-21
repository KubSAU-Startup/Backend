package com.meloda.kubsau.repository

import com.meloda.kubsau.database.worktypes.WorkTypeDao

interface WorkTypeRepository {
}

class WorkTypeRepositoryImpl(private val workTypeDao: WorkTypeDao) : WorkTypeRepository {

}
