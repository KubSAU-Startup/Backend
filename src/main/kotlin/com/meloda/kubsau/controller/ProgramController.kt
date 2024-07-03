package com.meloda.kubsau.controller

import com.meloda.kubsau.DATA_FOLDER
import com.meloda.kubsau.base.BaseController
import com.meloda.kubsau.common.*
import com.meloda.kubsau.database.departments.DepartmentDao
import com.meloda.kubsau.database.groups.GroupDao
import com.meloda.kubsau.database.programs.ProgramDao
import com.meloda.kubsau.database.programsdisciplines.ProgramDisciplineDao
import com.meloda.kubsau.database.students.StudentDao
import com.meloda.kubsau.model.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import org.koin.ktor.ext.inject
import qrcode.QRCode
import qrcode.color.Colors
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ProgramController : BaseController {

    override fun Route.routes() {
        authenticate {
            route("/programs") {
                getPrograms()
                getProgramById()
                getDisciplines()
                generateQRCodes()
                searchPrograms()
                addProgram()
                addDisciplinesToProgram()
                editProgram()
                editProgramDisciplines()
                deleteProgramById()
                deleteProgramsByIds()
            }
        }
    }

    private fun Route.getPrograms() {
        val programDao by inject<ProgramDao>()
        val programDisciplineDao by inject<ProgramDisciplineDao>()

        get {
            val principal = call.userPrincipal()
            val parameters = call.request.queryParameters

            val programIds = parameters.getIntList(
                key = "programIds",
                maxSize = MAX_PROGRAMS
            )

            val offset = parameters.getInt("offset")
            val limit = parameters.getInt(key = "limit", range = ProgramRange)

            val entries = programDao.allProgramsBySearch(
                facultyId = principal.facultyId,
                programIds = programIds,
                offset = offset,
                limit = limit ?: MAX_PROGRAMS,
                semester = null,
                directivityId = null,
                query = null
            )

            val disciplines = programDisciplineDao.allSearchDisciplinesByProgramIds(entries.map { it.program.id })

            respondSuccess {
                SearchResponse(
                    count = entries.size,
                    offset = offset ?: 0,
                    entries = entries.map { entry ->
                        entry.copy(
                            disciplines = disciplines.filter { it.programId == entry.program.id }.map { it.discipline }
                        )
                    }
                )
            }
        }
    }

    private fun Route.getProgramById() {
        val programDao by inject<ProgramDao>()
        val programDisciplineDao by inject<ProgramDisciplineDao>()

        get("{id}") {
            val programId = call.parameters.getIntOrThrow("id")
            val program = programDao.singleProgram(programId) ?: throw ContentNotFoundException

            val disciplines = programDisciplineDao.allDisciplinesByProgramId(programId)
                .map(Discipline::id)

            respondSuccess {
                ProgramWithDisciplineIds(
                    program = program,
                    disciplineIds = disciplines
                )
            }
        }
    }


    private fun Route.getDisciplines() {
        val programDisciplineDao by inject<ProgramDisciplineDao>()
        val departmentDao by inject<DepartmentDao>()

        get("{programId}/disciplines") {
            val programId = call.parameters.getIntOrThrow("programId")
            val extended = call.request.queryParameters.getBoolean("extended", false)

            val disciplines = programDisciplineDao.allDisciplinesByProgramId(programId)

            if (!extended) {
                respondSuccess {
                    DisciplinesResponse(disciplines = disciplines)
                }
            } else {
                val departmentIds = disciplines.map(Discipline::departmentId)
                val departments = departmentDao.allDepartmentsByIds(departmentIds)

                respondSuccess {
                    FullDisciplinesResponse(
                        disciplines = disciplines,
                        departments = departments
                    )
                }
            }
        }

        get("/disciplines") {
            val parameters = call.request.queryParameters

            val programIds = parameters.getIntListOrThrow(
                key = "programIds",
                requiredNotEmpty = true
            )

            val extended = parameters.getBoolean("extended", false)

            val disciplines = programDisciplineDao.allDisciplinesByProgramIds(programIds)

            if (!extended) {
                respondSuccess { DisciplinesResponse(disciplines = disciplines) }
            } else {
                val departmentIds = disciplines.map(Discipline::departmentId)
                val departments = departmentDao.allDepartmentsByIds(departmentIds)

                respondSuccess {
                    FullDisciplinesResponse(
                        disciplines = disciplines,
                        departments = departments
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun Route.generateQRCodes() {
        val programDao by inject<ProgramDao>()
        val groupDao by inject<GroupDao>()
        val studentDao by inject<StudentDao>()
        val programDisciplineDao by inject<ProgramDisciplineDao>()

        get("{id}/qr") {
            val programId = call.parameters.getIntOrThrow("id")
            programDao.singleProgram(programId) ?: throw ContentNotFoundException

            val groupIds = call.request.queryParameters.getIntListOrThrow(
                key = "groupIds",
                requiredNotEmpty = true
            )

            val groups = groupDao.allGroupsByIds(groupIds)
            if (groups.isEmpty()) {
                throw ContentNotFoundException
            }

            // TODO: 02/07/2024, Danil Nikolaev: remove after presentation
            if (groupIds.singleOrNull() == 1) {
                delay(1500)
                call.respondFile(File("$DATA_FOLDER/IT2001.zip"))
                return@get
            }

            val students = studentDao.allStudentsByGroupIdsAsMap(groupIds, true)
            val disciplines = programDisciplineDao.allDisciplineIdsByProgramIdAsMap(programId)

            val jobList = mutableListOf<Job>()

            val limitedDispatcher = Dispatchers.IO.limitedParallelism(2)

            for (group in groups) {
                val groupFolder = File("$DATA_FOLDER/QRs/${group.title}")

                val job = async(limitedDispatcher) {
                    for (student in students[group.id].orEmpty()) {
                        val studentFolder = File("${groupFolder.path}/${student.fullName}").also { it.mkdirs() }

                        val innerJobs = disciplines.map { discipline ->
                            async(limitedDispatcher) {
                                generateQRCode(
                                    dispatcher = limitedDispatcher,
                                    qrValue = "%d,%d,%d,%d".format(
                                        discipline.departmentId,
                                        discipline.disciplineId,
                                        student.id,
                                        discipline.workTypeId
                                    ),
                                    rootPath = studentFolder.path,
                                    fileName = discipline.title
                                )
                            }
                        }
                        innerJobs.awaitAll()
                    }
                }
                jobList.add(job)
            }

            jobList.joinAll()

            val qrFolder = File("$DATA_FOLDER/QRs")
            val fileToExtract = File("$DATA_FOLDER/temp.zip")

            zipDirectory(qrFolder, fileToExtract)

            call.respondFile(fileToExtract)

            qrFolder.deleteRecursively()
            fileToExtract.delete()
        }
    }

    private suspend fun generateQRCode(
        dispatcher: CoroutineDispatcher,
        qrValue: String,
        rootPath: String,
        fileName: String
    ): File = withContext(dispatcher) {
        val qrCodeBuilder = QRCode
            .ofRoundedSquares()
            .withSize(10)
            .withRadius(5)
            .withBackgroundColor(Colors.WHITE)
            .withColor(Colors.BLACK)
            .build(qrValue)

        val qrCode = qrCodeBuilder.render()
        val file = File("$rootPath/$fileName.png")

        qrCode.writeImage(file.outputStream())
        file
    }

    private fun zipDirectory(directory: File, zipFile: File) {
        ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
            zipDirectory(directory, directory, zipOut)
        }
    }

    private fun zipDirectory(baseDirectory: File, directoryToZip: File, zipOut: ZipOutputStream) {
        val files = directoryToZip.listFiles() ?: return

        for (file in files) {
            if (file.isDirectory) {
                zipDirectory(baseDirectory, file, zipOut)
            } else {
                val relativePath = baseDirectory.toURI().relativize(file.toURI()).path
                val entry = ZipEntry(relativePath)
                zipOut.putNextEntry(entry)

                FileInputStream(file).use { inputStream ->
                    inputStream.copyTo(zipOut)
                }
                zipOut.closeEntry()
            }
        }
    }


    private fun Route.searchPrograms() {
        val programDao by inject<ProgramDao>()
        val programDisciplineDao by inject<ProgramDisciplineDao>()

        get("/search") {
            val principal = call.userPrincipal()
            val parameters = call.request.queryParameters

            val offset = parameters.getInt("offset")
            val limit = parameters.getInt(key = "limit", range = ProgramRange)
            val semester = parameters.getInt("semester")
            val directivityId = parameters.getInt("directivityId")
            val query = parameters.getString(key = "query", trim = true)?.lowercase()

            val entries = programDao.allProgramsBySearch(
                facultyId = principal.facultyId,
                programIds = null,
                offset = offset,
                limit = limit ?: MAX_PROGRAMS,
                semester = semester,
                directivityId = directivityId,
                query = query
            )

            val programIds = entries.map { it.program.id }
            val disciplines = programDisciplineDao.allSearchDisciplinesByProgramIds(programIds)

            respondSuccess {
                SearchResponse(
                    count = entries.size,
                    offset = offset ?: 0,
                    entries = entries.map { entry ->
                        entry.copy(
                            disciplines = disciplines.filter { it.programId == entry.program.id }.map { it.discipline }
                        )
                    }
                )
            }
        }
    }

    private fun Route.addProgram() {
        val programDao by inject<ProgramDao>()

        post {
            val parameters = call.receiveParameters()

            val semester = parameters.getIntOrThrow("semester")
            val directivityId = parameters.getIntOrThrow("directivityId")

            val created = programDao.addNewProgram(
                semester = semester,
                directivityId = directivityId
            )

            if (created != null) {
                respondSuccess { created }
            } else {
                throw UnknownException
            }
        }
    }

    private fun Route.addDisciplinesToProgram() {
        val programDao by inject<ProgramDao>()
        val programDisciplineDao by inject<ProgramDisciplineDao>()

        post("{id}/disciplines") {
            val programId = call.parameters.getIntOrThrow("id")
            programDao.singleProgram(programId) ?: throw ContentNotFoundException

            val parameters = call.receiveParameters()

            val disciplineIds = parameters.getIntListOrThrow(
                key = "disciplineIds",
                requiredNotEmpty = true
            )

            val workTypeIds = parameters.getIntListOrThrow(
                key = "workTypeIds",
                requiredNotEmpty = true
            )

            if (disciplineIds.size != workTypeIds.size) {
                throw ValidationException.InvalidException(
                    message = "different count of disciplines (${disciplineIds.size}) and work types (${workTypeIds.size}"
                )
            }

            disciplineIds.forEachIndexed { index, disciplineId ->
                if (!programDisciplineDao.addNewReference(
                        programId = programId,
                        disciplineId = disciplineId,
                        workTypeId = workTypeIds[index]
                    )
                ) {
                    throw UnknownException
                }
            }

            respondSuccess { 1 }
        }
    }

    private fun Route.editProgram() {
        val programDao by inject<ProgramDao>()

        patch("{id}") {
            val programId = call.parameters.getIntOrThrow("id")
            val currentProgram = programDao.singleProgram(programId) ?: throw ContentNotFoundException

            val parameters = call.receiveParameters()

            val semester = parameters.getInt("semester")
            val directivityId = parameters.getInt("directivityId")

            programDao.updateProgram(
                programId = programId,
                semester = semester ?: currentProgram.semester,
                directivityId = directivityId ?: currentProgram.directivityId
            ).let { success ->
                if (success) {
                    respondSuccess { 1 }
                } else {
                    throw UnknownException
                }
            }
        }
    }

    private fun Route.editProgramDisciplines() {
        val programDao by inject<ProgramDao>()
        val programDisciplineDao by inject<ProgramDisciplineDao>()

        patch("{id}/disciplines") {
            val programId = call.parameters.getIntOrThrow("id")
            val program = programDao.singleProgram(programId) ?: throw ContentNotFoundException

            val parameters = call.receiveParameters()

            val disciplineIds = parameters.getIntListOrThrow(
                key = "disciplineIds",
                requiredNotEmpty = true
            )

            val workTypeIds = parameters.getIntListOrThrow(
                key = "workTypeIds",
                requiredNotEmpty = true
            )

            if (disciplineIds.size != workTypeIds.size) {
                throw ValidationException.InvalidException(
                    message = "disciplines size (${disciplineIds.size}) is different from work types size (${workTypeIds.size})"
                )
            }

            programDisciplineDao.deleteReferencesByProgramId(program.id)

            disciplineIds.forEachIndexed { index, disciplineId ->
                programDisciplineDao.addNewReference(programId, disciplineId, workTypeIds[index])
            }

            respondSuccess { 1 }
        }
    }

    private fun Route.deleteProgramById() {
        val programDao by inject<ProgramDao>()

        delete("{id}") {
            val programId = call.parameters.getIntOrThrow("id")
            programDao.singleProgram(programId) ?: throw ContentNotFoundException

            if (programDao.deleteProgram(programId)) {
                respondSuccess { 1 }
            } else {
                throw UnknownException
            }
        }
    }

    private fun Route.deleteProgramsByIds() {
        val programDao by inject<ProgramDao>()

        delete {
            val programIds = call.request.queryParameters.getIntListOrThrow(
                key = "programIds",
                requiredNotEmpty = true
            )

            val currentPrograms = programDao.allProgramsByIds(programIds)
            if (currentPrograms.isEmpty()) {
                throw ContentNotFoundException
            }

            if (programDao.deletePrograms(programIds)) {
                respondSuccess { 1 }
            } else {
                throw UnknownException
            }
        }
    }
}


private data class SearchResponse(
    val count: Int,
    val offset: Int,
    val entries: List<SearchEntry>
)

data class SearchEntry(
    val program: SearchProgram,
    val directivity: IdTitle,
    val grade: IdTitle,
    val disciplines: List<SearchDiscipline>
)

data class SearchProgram(
    val id: Int,
    val semester: Int
)

data class SearchDiscipline(
    val id: Int,
    val title: String,
    val workTypeId: Int,
    val departmentId: Int
)

data class SearchDisciplineWithProgramId(
    val programId: Int,
    val discipline: SearchDiscipline
)

data class FullDisciplineIds(
    val disciplineId: Int,
    val programId: Int,
    val workTypeId: Int,
    val departmentId: Int,
    val title: String
)

private data class ProgramWithDisciplineIds(
    val program: Program,
    val disciplineIds: List<Int>
)

private data class DisciplinesResponse(
    val disciplines: List<Discipline>
)

private data class FullDisciplinesResponse(
    val disciplines: List<Discipline>,
    val departments: List<Department>
)
