package com.meloda.kubsau

import com.meloda.kubsau.common.Constants
import com.meloda.kubsau.common.isInDocker
import com.meloda.kubsau.database.DatabaseController
import com.meloda.kubsau.database.departments.DepartmentsDao
import com.meloda.kubsau.database.disciplines.DisciplinesDao
import com.meloda.kubsau.database.groups.GroupsDao
import com.meloda.kubsau.database.majors.MajorsDao
import com.meloda.kubsau.database.students.StudentsDao
import com.meloda.kubsau.database.teachers.TeachersDao
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
import io.ktor.server.plugins.cors.routing.*
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.inject
import kotlin.random.Random

const val PORT = 8080

fun main() {
    DatabaseController.init()
    configureServer()
}


private fun configureServer() {
    val server = embeddedServer(Netty, PORT) {
        prepopulateDB()

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
        configureKoin()

        routing()
    }

    println("Server's version: ${Constants.BACKEND_VERSION}")
    println("Is docker: $isInDocker")
    println("Server is working on port: $PORT")

    server.start(wait = true)
}

private fun Application.prepopulateDB() {
    createDummyWorkTypes()
    createDummyDepartments()
    createDummyUsers()
    createDummyStudents()
    createDummyTeachers()
    createDummyDisciplines()
    createDummyMajors()
    createDummyGroups()
    createDummyJournalWorks()
    createDummyJournalEntries()
}

private fun Application.createDummyWorkTypes() {
    val workTypesDao by inject<WorkTypesDao>()

    workTypesDao.apply {
        runBlocking {
            if (allWorkTypes().size < 2) {
                addNewWorkType("Курсовая", false)
                addNewWorkType("Практика", true)
            }
        }
    }
}


private fun Application.createDummyDepartments() {
    val departmentsDao by inject<DepartmentsDao>()

    departmentsDao.apply {
        runBlocking {
            if (allDepartments().size < 3) {
                addNewDepartment("Прикладная информатика", "+7 (800) 555-35-35")
                addNewDepartment("Бизнес-информатика", "+7 (800) 555-35-36")
                addNewDepartment("Иностранный язык", "+7 (800) 555-35-37")
            }
        }
    }
}

private fun Application.createDummyUsers() {
    val usersDao by inject<UsersDao>()

    usersDao.apply {
        runBlocking {
            if (allUsers().size < 3) {
                addNewUser(login = "lischenkodev@gmail.com", password = "123456", type = 1, departmentId = 1)
                addNewUser(login = "m.kozhukhar@gmail.com", password = "789012", type = 1, departmentId = 2)
                addNewUser(login = "ya.abros@gmail.com", password = "345678", type = 1, departmentId = 3)
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
        }
    }
}

private fun Application.createDummyTeachers() {
    val departmentsDao by inject<DepartmentsDao>()
    val teachersDao by inject<TeachersDao>()

    teachersDao.apply {
        runBlocking {
            if (allTeachers().size < 10) {
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
                disciplinesString.forEach { title -> addNewDiscipline(title) }
            }
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
                majors.forEach { major ->
                    addNewMajor(
                        code = major.first,
                        title = major.second,
                        abbreviation = major.third
                    )
                }
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
                val majorIds = majorsDao.allMajors().map(Major::id)

                repeat(10) {
                    addNewGroup(
                        title = "${groupsString.random()}${Random.nextInt(from = 2001, until = 2006)}",
                        majorId = majorIds.random()
                    )
                }
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

        if (works.size < 3) {
            val workTypeIds = workTypesDao.allWorkTypes().map(WorkType::id)
            val disciplineIds = disciplinesDao.allDisciplines().map(Discipline::id)
            val studentIds = studentsDao.allStudents().map(Student::id)

            repeat(3) { index ->
                worksDao.addNewWork(
                    typeId = workTypeIds.random(),
                    disciplineId = disciplineIds.random(),
                    studentId = studentIds.random(),
                    registrationDate = getRandomUnixTime(1609459200L, 1708775961L),
                    title = "Work #${index + 1}"
                )
            }
        }
    }
}

private fun Application.createDummyJournalEntries() {
    // TODO: 16/03/2024, Danil Nikolaev: fill out
}


private fun getRandomUnixTime(startTime: Long, endTime: Long): Long {
    require(startTime < endTime) { "Start time must be before end time" }
    val randomUnixTime = Random.nextLong(startTime, endTime)
    return randomUnixTime
}
