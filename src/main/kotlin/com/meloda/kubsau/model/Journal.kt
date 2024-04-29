package com.meloda.kubsau.model

import com.meloda.kubsau.database.journals.Journals
import org.jetbrains.exposed.sql.ResultRow

data class Journal(
    val id: Int,
    val student: Student,
    val group: Group,
    val discipline: Discipline,
    val teacher: Teacher,
    val work: Work,
    val department: Department?
) {

    companion object {

        fun mapResultRow(row: ResultRow): Journal = Journal(
            id = row[Journals.id].value,
            student = Student.mapResultRow(row),
            group = Group.mapResultRow(row),
            discipline = Discipline.mapResultRow(row),
            teacher = Teacher.mapResultRow(row),
            work = Work.mapResultRow(row),
            department = try {
                Department.mapResultRow(row)
            } catch (e: Exception) {
                null
            }
        )
    }
}
