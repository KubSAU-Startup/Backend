package com.meloda.kubsau.route.qr

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.database.groups.GroupsDao
import com.meloda.kubsau.database.programs.ProgramsDao
import com.meloda.kubsau.database.programsdisciplines.ProgramsDisciplinesDao
import com.meloda.kubsau.model.Discipline
import com.meloda.kubsau.model.Group
import com.meloda.kubsau.model.Program
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.qr() {
    authenticate {
        route("/qr") {
            getData()
        }
    }
}

private fun Route.getData() {
    val programsDisciplinesDao by inject<ProgramsDisciplinesDao>()
    val programsDao by inject<ProgramsDao>()
    val groupsDao by inject<GroupsDao>()

    get {
        val programs = programsDao.allPrograms().map { program ->
            val disciplines = programsDisciplinesDao.allDisciplinesByProgramId(program.id)
            program.mapWithDisciplines(disciplines)
        }
        val groups = groupsDao.allGroups()

        respondSuccess { GetDataResponse(programs = programs, groups = groups) }
    }
}

private fun Program.mapWithDisciplines(disciplines: List<Discipline>): ProgramWithDisciplines =
    ProgramWithDisciplines(
        id = id,
        title = title,
        semester = semester,
        disciplines = disciplines
    )

private data class ProgramWithDisciplines(
    val id: Int,
    val title: String,
    val semester: Int,
    val disciplines: List<Discipline>
)

private data class GetDataResponse(
    val programs: List<ProgramWithDisciplines>,
    val groups: List<Group>
)
