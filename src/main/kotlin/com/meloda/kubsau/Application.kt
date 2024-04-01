package com.meloda.kubsau

import com.meloda.kubsau.common.Constants
import com.meloda.kubsau.common.isInDocker
import com.meloda.kubsau.database.DatabaseController
import com.meloda.kubsau.database.departments.DepartmentsDao
import com.meloda.kubsau.database.disciplines.DisciplinesDao
import com.meloda.kubsau.database.groups.GroupsDao
import com.meloda.kubsau.database.journals.JournalsDao
import com.meloda.kubsau.database.majors.MajorsDao
import com.meloda.kubsau.database.programs.ProgramsDao
import com.meloda.kubsau.database.specializations.SpecializationsDao
import com.meloda.kubsau.database.students.StudentsDao
import com.meloda.kubsau.database.teachers.TeachersDao
import com.meloda.kubsau.database.teachersdisciplines.TeachersDisciplinesDao
import com.meloda.kubsau.database.users.UsersDao
import com.meloda.kubsau.database.works.WorksDao
import com.meloda.kubsau.database.worktypes.WorkTypesDao
import com.meloda.kubsau.model.*
import com.meloda.kubsau.plugins.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.cors.routing.*
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.inject
import org.slf4j.event.Level
import kotlin.random.Random
import kotlin.system.measureTimeMillis

const val PORT = 8080

var startTime = 0L

fun main() {
    startTime = System.currentTimeMillis()

    DatabaseController.init()
    configureServer()
}

private fun configureServer() {
    val server = embeddedServer(
        factory = Netty,
        port = PORT,
        watchPaths = listOf("classes")
    ) {
        configureKoin()
        prepopulateDB()

        install(CallLogging) {
            level = Level.DEBUG
        }

        install(AutoHeadResponse)
        install(CORS) {
            anyHost()

            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Delete)

            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.Accept)
            allowHeader(HttpHeaders.Authorization)
        }

        configureAuthentication()
        configureExceptions()
        configureContentNegotiation()

        routing()
    }

    println("Server's version: ${Constants.BACKEND_VERSION}")
    println("Is inside Docker: $isInDocker")
    println("Port: $PORT")

    server.start(wait = true)
}

private fun Application.prepopulateDB() {
    createDummyWorkTypes()
    createDummyDepartments()
    createDummyUsers()
    createDummyMajors()
    createDummyPrograms()
    createDummySpecializations()
    createDummyGroups()
    createDummyStudents()
    createDummyTeachers()
    createDummyDisciplines()
    createDummyTeachersDisciplines()
    createDummyJournalWorks()
    createDummyJournalEntries()
}

private fun Application.createDummyWorkTypes() {
    val workTypesDao by inject<WorkTypesDao>()

    workTypesDao.apply {
        runBlocking {
            if (allWorkTypes().size < 2) {
                println("Creating dummy work types...")

                val time = measureTimeMillis {
                    addNewWorkType("Курсовая", false)
                    addNewWorkType("Практика", true)
                }

                println("Dummy work types created. Took ${time}ms")
            }
        }
    }
}


private fun Application.createDummyDepartments() {
    val departmentsDao by inject<DepartmentsDao>()

    departmentsDao.apply {
        runBlocking {
            if (allDepartments().size < 3) {
                println("Creating dummy departments...")

                val time = measureTimeMillis {
                    addNewDepartment("Прикладная информатика", "+7 (800) 555-35-35")
                    addNewDepartment("Бизнес-информатика", "+7 (800) 555-35-36")
                    addNewDepartment("Иностранный язык", "+7 (800) 555-35-37")
                }

                println("Dummy departments created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyUsers() {
    val usersDao by inject<UsersDao>()

    usersDao.apply {
        runBlocking {
            if (allUsers().size < 3) {
                println("Creating dummy users...")

                val time = measureTimeMillis {
                    addNewUser(login = "lischenkodev@gmail.com", password = "123456", type = 1, departmentId = 1)
                    addNewUser(login = "m.kozhukhar@gmail.com", password = "789012", type = 1, departmentId = 2)
                    addNewUser(login = "ya.abros@gmail.com", password = "345678", type = 1, departmentId = 3)
                }

                println("Dummy users created. Took ${time}ms")
            }
        }
    }
}

private val names = listOf(
    "Иванов Иван Иванович",
    "Смирнова Мария Александровна",
    "Кузнецов Александр Сергеевич",
    "Попова Екатерина Дмитриевна",
    "Васильев Дмитрий Петрович",
    "Петрова Анна Алексеевна",
    "Соколов Алексей Владимирович",
    "Михайлова София Николаевна",
    "Новиков Андрей Игоревич",
    "Фёдорова Алиса Анатольевна",
    "Морозов Сергей Александрович",
    "Волкова Анастасия Денисовна",
    "Алексеев Михаил Владимирович",
    "Лебедева Елена Аркадьевна",
    "Семёнов Артём Максимович",
    "Егорова Виктория Васильевна",
    "Павлов Илья Викторович",
    "Козлова Дарья Владимировна",
    "Степанов Максим Александрович",
    "Иванова Полина Владимировна",
    "Федоров Тимофей Алексеевич",
    "Александрова Вероника Ивановна",
    "Морозов Кирилл Алексеевич",
    "Васильева Елизавета Алексеевна",
    "Петров Антон Викторович",
    "Степанова Валерия Сергеевна",
    "Николаев Григорий Петрович",
    "Максимова Юлия Владимировна",
    "Ильин Даниил Александрович",
    "Зайцева Анастасия Сергеевна",
    "Соловьёв Павел Дмитриевич",
    "Смирнова Влада Игоревна",
    "Кузнецов Роман Павлович",
    "Попова Арина Михайловна",
    "Фёдоров Егор Дмитриевич",
    "Михайлова Алёна Владимировна",
    "Новиков Игорь Анатольевич",
    "Фёдорова Варвара Андреевна",
    "Александров Фёдор Алексеевич",
    "Петрова Светлана Андреевна"
)

private fun Application.createDummyStudents() {
    val groupsDao by inject<GroupsDao>()
    val studentsDao by inject<StudentsDao>()

    studentsDao.apply {
        runBlocking {
            if (allStudents().size < 10) {
                println("Creating dummy students...")

                val time = measureTimeMillis {
                    val groupIds = groupsDao.allGroups().map(Group::id)

                    repeat(10) {
                        val nameSplit = names.random().split(" ")

                        addNewStudent(
                            firstName = nameSplit[0],
                            lastName = nameSplit[1],
                            middleName = nameSplit[2],
                            groupId = groupIds.random(),
                            status = if (Random.nextBoolean()) 1 else 0
                        )
                    }
                }

                println("Dummy students created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyTeachers() {
    val departmentsDao by inject<DepartmentsDao>()
    val teachersDao by inject<TeachersDao>()

    teachersDao.apply {
        runBlocking {
            if (allTeachers().size < 10) {
                println("Creating dummy teachers...")

                val time = measureTimeMillis {
                    val departmentIds = departmentsDao.allDepartments().map(Department::id)

                    repeat(10) {
                        val nameSplit = names.random().split(" ")

                        addNewTeacher(
                            firstName = nameSplit[0],
                            lastName = nameSplit[1],
                            middleName = nameSplit[2],
                            departmentId = departmentIds.random()
                        )
                    }
                }

                println("Dummy teachers created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyDisciplines() {
    val disciplinesString = listOf(
        "Математический анализ",
        "Физика",
        "Программирование",
        "Экономика",
        "История",
        "Философия",
        "Иностранный язык",
        "Химия",
        "Биология",
        "Механика",
        "Политология",
        "Лингвистика",
        "География",
        "Социология",
        "Психология",
        "Финансы",
        "Маркетинг",
        "Архитектура",
        "Медицина",
        "Литература",
        "Культурология",
        "Религиоведение",
        "Право",
        "Театральное искусство",
        "Информационные технологии",
        "Экология",
        "Искусство",
        "Физическая культура",
        "Графический дизайн",
        "Музыка",
        "Журналистика",
        "Педагогика",
        "Астрономия",
        "Антропология",
        "Кибербезопасность",
        "Анатомия",
        "Логика",
        "Агрономия",
        "Генетика"
    )

    val disciplinesDao by inject<DisciplinesDao>()

    disciplinesDao.apply {
        runBlocking {
            if (allDisciplines().size < disciplinesString.size) {
                println("Creating dummy disciplines...")

                val time = measureTimeMillis {
                    disciplinesString.forEach { title -> addNewDiscipline(title) }
                }

                println("Dummy disciplines created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyTeachersDisciplines() {
    val teachersDisciplinesDao by inject<TeachersDisciplinesDao>()
    val teachersDao by inject<TeachersDao>()
    val disciplinesDao by inject<DisciplinesDao>()

    runBlocking {
        if (teachersDisciplinesDao.allItems().size < 10) {
            println("Creating dummy teachers-disciplines references...")

            val time = measureTimeMillis {
                val teacherIds = teachersDao.allTeachers().map(Teacher::id)
                val disciplineIds = disciplinesDao.allDisciplines().map(Discipline::id)

                repeat(10) {
                    teachersDisciplinesDao.addNewReference(
                        teacherId = teacherIds.random(),
                        disciplineId = disciplineIds.random()
                    )
                }
            }

            println("Dummy teachers-disciplines references created. Took ${time}ms")
        }
    }
}

private fun Application.createDummyMajors() {
    val majors = listOf(
        Triple("01.03.04", "Прикладная математика", "ПМ"),
        Triple("09.03.01", "Информатика и вычислительная техника", "ИИВТ"),
        Triple("09.03.02", "Информационные системы и технологии", "ИСИТ"),
        Triple("09.03.03", "Прикладная информатика", "ПИ"),
        Triple("09.03.04", "Программная инженерия", "ПИЖ"),
    )

    val majorsDao by inject<MajorsDao>()

    majorsDao.apply {
        runBlocking {
            if (allMajors().size < majors.size) {
                println("Creating dummy majors...")

                val time = measureTimeMillis {
                    majors.forEach { major ->
                        addNewMajor(
                            code = major.first,
                            title = major.second,
                            abbreviation = major.third
                        )
                    }
                }

                println("Dummy majors created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyPrograms() {
    val programsDao by inject<ProgramsDao>()

    programsDao.apply {
        runBlocking {
            if (allPrograms().size < 40) {
                println("Creating dummy programs...")

                val time = measureTimeMillis {
                    repeat(40) { index ->
                        addNewProgram("Program #${index + 1}", Random.nextInt(from = 0, until = 13))
                    }
                }

                println("Dummy programs created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummySpecializations() {
    val specializationsDao by inject<SpecializationsDao>()

    specializationsDao.apply {
        runBlocking {
            if (allSpecializations().size < 20) {
                println("Creating dummy specializations...")

                val time = measureTimeMillis {
                    repeat(20) { index ->
                        addNewSpecialization("Specialization #${index + 1}")
                    }
                }

                println("Dummy specializations created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyGroups() {
    val groupsString = listOf("ИТ", "ПИ", "БИ")

    val groupsDao by inject<GroupsDao>()
    val majorsDao by inject<MajorsDao>()

    groupsDao.apply {
        runBlocking {
            if (allGroups().size < 10) {
                println("Creating dummy groups...")

                val time = measureTimeMillis {
                    val majorIds = majorsDao.allMajors().map(Major::id)

                    repeat(10) {
                        addNewGroup(
                            title = "${groupsString.random()}${Random.nextInt(from = 2001, until = 2006)}",
                            majorId = majorIds.random()
                        )
                    }
                }

                println("Dummy groups created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyJournalWorks() {
    val workTypesDao by inject<WorkTypesDao>()
    val worksDao by inject<WorksDao>()
    val disciplinesDao by inject<DisciplinesDao>()
    val studentsDao by inject<StudentsDao>()

    runBlocking {
        val works = worksDao.allWorks()

        if (works.size < 10) {
            println("Creating dummy works...")

            val time = measureTimeMillis {
                val workTypeIds = workTypesDao.allWorkTypes().map(WorkType::id)
                val disciplineIds = disciplinesDao.allDisciplines().map(Discipline::id)
                val studentIds = studentsDao.allStudents().map(Student::id)

                repeat(10) { index ->
                    worksDao.addNewWork(
                        typeId = workTypeIds.random(),
                        disciplineId = disciplineIds.random(),
                        studentId = studentIds.random(),
                        registrationDate = getRandomUnixTime(1609459200L, 1708775961L),
                        title = "Work #${index + 1}"
                    )
                }
            }

            println("Dummy works created. Took ${time}ms")
        }
    }
}

private fun Application.createDummyJournalEntries() {
    val studentsDao by inject<StudentsDao>()
    val groupsDao by inject<GroupsDao>()
    val disciplinesDao by inject<DisciplinesDao>()
    val teachersDao by inject<TeachersDao>()
    val worksDao by inject<WorksDao>()

    val journalsDao by inject<JournalsDao>()

    journalsDao.apply {
        runBlocking {
            val journals = journalsDao.allJournals()

            if (journals.size < 10) {
                println("Creating dummy journal entries...")

                val time = measureTimeMillis {
                    val studentIds = studentsDao.allStudents().map(Student::id)
                    val groupIds = groupsDao.allGroups().map(Group::id)
                    val disciplineIds = disciplinesDao.allDisciplines().map(Discipline::id)
                    val teacherIds = teachersDao.allTeachers().map(Teacher::id)
                    val workIds = worksDao.allWorks().map(Work::id)

                    repeat(10) {
                        addNewJournal(
                            studentId = studentIds.random(),
                            groupId = groupIds.random(),
                            disciplineId = disciplineIds.random(),
                            teacherId = teacherIds.random(),
                            workId = workIds.random()
                        )
                    }
                }

                println("Dummy journal entries created. Took ${time}ms")
            }
        }
    }
}


private fun getRandomUnixTime(startTime: Long, endTime: Long): Long {
    require(startTime < endTime) { "Start time must be before end time" }
    val randomUnixTime = Random.nextLong(startTime, endTime)
    return randomUnixTime
}
