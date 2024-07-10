package com.meloda.kubsau.repository

import com.meloda.kubsau.database.students.StudentDao

interface StudentRepository {
}

class StudentRepositoryImpl(private val studentDao: StudentDao) : StudentRepository {

}
