package com.meloda.kubsau

import com.meloda.kubsau.common.*
import com.meloda.kubsau.config.ConfigController
import com.meloda.kubsau.config.DatabaseController
import com.meloda.kubsau.config.SecretsController
import com.meloda.kubsau.database.departments.DepartmentDao
import com.meloda.kubsau.database.directivities.DirectivityDao
import com.meloda.kubsau.database.disciplines.DisciplineDao
import com.meloda.kubsau.database.employees.EmployeeDao
import com.meloda.kubsau.database.employeesdepartments.EmployeeDepartmentDao
import com.meloda.kubsau.database.employeesfaculties.EmployeeFacultyDao
import com.meloda.kubsau.database.faculties.FacultyDao
import com.meloda.kubsau.database.grades.GradeDao
import com.meloda.kubsau.database.groups.GroupDao
import com.meloda.kubsau.database.heads.HeadDao
import com.meloda.kubsau.database.programs.ProgramDao
import com.meloda.kubsau.database.programsdisciplines.ProgramDisciplineDao
import com.meloda.kubsau.database.students.StudentDao
import com.meloda.kubsau.database.studentstatuses.StudentStatusDao
import com.meloda.kubsau.database.users.UserDao
import com.meloda.kubsau.database.works.WorkDao
import com.meloda.kubsau.database.worktypes.WorkTypeDao
import com.meloda.kubsau.model.Employee
import com.meloda.kubsau.model.Student
import com.meloda.kubsau.model.WorkType
import com.meloda.kubsau.plugins.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.inject
import org.slf4j.event.Level
import kotlin.random.Random
import kotlin.system.measureTimeMillis

val PROJECT_FOLDER: String = if (IS_IN_DOCKER) "" else System.getProperty("user.dir")
val DATA_FOLDER: String = "$PROJECT_FOLDER/data"
val CONFIG_FOLDER: String = "$PROJECT_FOLDER/config"
val PORT: Int = getEnvOrNull("PORT")?.toIntOrNull() ?: 8080

fun main() {
    val startTime = System.currentTimeMillis()

    ConfigController.init()
    SecretsController.init()
    DatabaseController.init()

    configureServer(startTime).start(wait = true)
}

private fun configureServer(startTime: Long): NettyApplicationEngine = embeddedServer(
    factory = Netty,
    port = PORT,
    watchPaths = listOf("classes")
) {
    println("Server's version: ${Constants.BACKEND_VERSION}")
    println("Is inside Docker: $IS_IN_DOCKER")
    println("Port: $PORT")

    configureKoin()
    prepopulateDB()

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
        format { call ->
            val requestLog = call.request.toLogString()
            val responseLog = call.response.toLogString()
            "$requestLog -> $responseLog"
        }
    }

    install(AutoHeadResponse)
    install(CORS) {
        anyHost()

        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)

        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)
        allowHeader(HttpHeaders.Authorization)
    }

    configureAuthentication()
    configureExceptions()
    configureContentNegotiation()

    routing()

    println("Server is ready in ${System.currentTimeMillis().minus(startTime)}ms")
}

private fun Application.prepopulateDB() {
    // TODO: 10/04/2024, Danil Nikolaev: import data from json

    val startTime = System.currentTimeMillis()

    println("Pre-populating db...")

    createDummyEmployees()

    createDummyUsers()

    createDummyGrades()

    createDummyDepartments()
    createDummyEmployeesDepartments()

    createDummyDisciplines()

    createDummyFaculties()
    createDummyEmployeesFaculties()

    createDummyHeads()

    createDummyDirectivities()

    createDummyGroups()

    createDummyStudentStatuses()
    createDummyStudents()

    createDummyWorkTypes()
    createDummyWorks()

    createDummyPrograms()
    createDummyProgramsDisciplines()

    println("Db pre-populated in ${System.currentTimeMillis().minus(startTime)}ms")
}

private fun Application.createDummyEmployees() {
    val employeeDao by inject<EmployeeDao>()

    val teacherNames =
        ("Калитко Светлана Алексеевна\n" +
                "Павлов Дмитрий Алексеевич\n" +
                "Колесникова Татьяна Петровна\n" +
                "Жабчик Светлана Викторовна\n" +
                "Еникеев Анатолий Анатольевич\n" +
                "Погребняк Наталья Владимировна\n" +
                "Шаповалов Анатолий Вячеславович\n" +
                "Тюнин Евгений Борисович\n" +
                "Овсянникова Ольга Владимировна\n" +
                "Рыбальченко Ольга Владимировна\n" +
                "Петунина Ирина Александровна\n" +
                "Кацко Игорь Александрович\n" +
                "Чемарина Анна Валерьевна\n" +
                "Аршинов Георгий Александрович\n" +
                "Иванова Елена Александровна\n" +
                "Сергеев Александр Эдуардович\n" +
                "Фешина Елена Васильевна\n" +
                "Крепышев Дмитрий Александрович\n" +
                "Нилова Надежда Михайловна\n" +
                "Ильин Владимир Викторович\n" +
                "Алашеев Вадим Викторович\n" +
                "Франциско Ольга Юрьевна\n" +
                "Яхонтова Ирина Михайловна\n" +
                "Барановская Татьяна Петровна\n" +
                "Вострокнутов Александр Евгеньевич\n" +
                "Лукьяненко Татьяна Викторовна\n" +
                "Мурлин Алексей Георгиевич\n" +
                "Креймер Алексей Семёнович\n" +
                "Ефанова Наталья Владимировна")
            .split("\n")



    employeeDao.apply {
        runBlocking {
            if (allEmployees(null, null, null).isEmpty()) {
                println("Creating dummy employees...")

                val time = measureTimeMillis {
                    addNewEmployee(
                        lastName = "Василенко",
                        firstName = "Игорь",
                        middleName = "Иванович",
                        email = "vasilenko.i@kubsau.ru",
                        type = 1
                    )
                    addNewEmployee(
                        lastName = "Василенко",
                        firstName = "Игорь",
                        middleName = "Иванович",
                        email = "vasilenko.i@kubsau.ru",
                        type = 2
                    )
                    addNewEmployee(
                        lastName = "Параскевов",
                        firstName = "Александр",
                        middleName = "Владимирович",
                        email = "paraskevov.a@kubsau.ru",
                        type = 2
                    )
                    addNewEmployee(
                        lastName = "Ивлев",
                        firstName = "Евгений",
                        middleName = "Владимирович",
                        email = "ivlev.e@kubsau.ru",
                        type = 3
                    )

                    teacherNames.map { it.split(" ") }.forEach { (lastName, firstName, middleName) ->
                        addNewEmployee(
                            lastName = lastName,
                            firstName = firstName,
                            middleName = middleName,
                            email = "test@kubsau.ru",
                            type = 2
                        )
                    }
                }

                println("Dummy employees created. Took ${time}ms")
            }
        }
    }
}


private fun Application.createDummyUsers() {
    val userDao by inject<UserDao>()

    userDao.apply {
        runBlocking {
            if (allUsers().isEmpty()) {
                println("Creating dummy users...")

                val time = measureTimeMillis {
                    val hashedPassword = hashPassword("123456")
                    val hashedPassword2 = hashPassword("789012")

                    addNewUser(
                        login = "vasilenko.i@kubsau.ru",
                        password = hashedPassword2,
                        employeeId = 1
                    )
                    addNewUser(
                        login = "vasilenko.i@kubsau.ru",
                        password = hashedPassword,
                        employeeId = 2
                    )
                    addNewUser(
                        login = "paraskevov.a@kubsau.ru",
                        password = hashedPassword,
                        employeeId = 3
                    )
                    addNewUser(
                        login = "ivlev.e@kubsau.ru",
                        password = hashedPassword,
                        employeeId = 4
                    )
                }

                println("Dummy users created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyGrades() {
    val gradeDao by inject<GradeDao>()

    gradeDao.apply {
        runBlocking {
            if (allGrades(null, null).isEmpty()) {
                println("Creating dummy grades...")

                val time = measureTimeMillis {
                    addNewGrade("Бакалавриат")
                    addNewGrade("Магистратура")
                    addNewGrade("Специалитет")
                    addNewGrade("Аспирантура")
                    addNewGrade("Интернатура")
                }

                println("Dummy grades created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyEmployeesDepartments() {
    val employeeDepartmentDao by inject<EmployeeDepartmentDao>()

    val departments =
        ("5\n" +
                "3\n" +
                "8\n" +
                "9\n" +
                "10\n" +
                "11\n" +
                "12\n" +
                "2\n" +
                "14\n" +
                "15\n" +
                "6\n" +
                "16\n" +
                "1\n" +
                "1\n" +
                "3\n" +
                "1\n" +
                "1\n" +
                "1\n" +
                "3\n" +
                "7\n" +
                "1\n" +
                "4\n" +
                "3\n" +
                "3\n" +
                "3\n" +
                "1\n" +
                "3\n" +
                "1\n" +
                "3\n" +
                "3")
            .split("\n")
            .map { it.toInt() }

    employeeDepartmentDao.apply {
        runBlocking {
            if (allReferences().isEmpty()) {
                println("Creating dummy employees departments references...")

                val time = measureTimeMillis {
                    addNewReference(1, 1)
                    addNewReference(2, 1)
                    addNewReference(3, 1)
                    addNewReference(3, 2)
                    addNewReference(4, 1)

                    (5..30).forEachIndexed { index, employeeId ->
                        val departmentId = departments[index]
                        addNewReference(employeeId, departmentId)
                    }
                }

                println("Dummy employees departments references created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyDepartments() {
    val departmentDao by inject<DepartmentDao>()

    val titles = listOf(
        "Компьютерных технологий и систем",
        "Информационных систем",
        "Системного анализа и обработки информации",
        "Экономической кибернетики",
        "Менеджмента",
        "Высшей математики",
        "Физвоспитания",
        "Физики",
        "Истории и политологии",
        "Философии",
        "Иностранного языка",
        "Теории и истории государства и права",
        "Статистики и прикладной математики",
        "Механизации животноводства и БЖД",
        "Русского языка и речевой коммуникации",
        "Статистики и прикладной математики"
    )

    departmentDao.apply {
        runBlocking {
            if (allDepartments(null).isEmpty()) {
                println("Creating dummy departments...")

                val time = measureTimeMillis {
                    titles.forEach { title ->
                        addNewDepartment(title = title, phone = "+7 (800) 555-35-35")
                    }
                }

                println("Dummy departments created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyDisciplines() {
    val disciplinesDepartments = listOf(
        "Самоменеджмент" to 5,
        "Математический анализ" to 6,
        "Физика" to 8,
        "История" to 9,
        "Философия" to 10,
        "Иностранный язык" to 11,
        "Основы правовых знаний" to 12,
        "Информационные технологии" to 2,
        "Безопасность жизнедеятельности" to 14,
        "Русский язык и культура речи" to 15,
        "Линейная алгебра и аналитическая геометрия" to 6,
        "Теория вероятностей и математическая статистика" to 13,
        "Информатика" to 1,
        "Алгоритмизация и программирование" to 3,
        "Основы математической логики и теории алгоритмов" to 1,
        "Алгоритмы и структуры данных" to 3,
        "Компьютерные системы" to 1,
        "Базы данных" to 1,
        "Методы и средства проектирования информационных систем" to 2,
        "Инфокоммуникационные системы и сети" to 1,
        "Стандартизация, сертификация и управление качеством информационных систем" to 3,
        "Физическая культура и спорт" to 7,
        "Информационная безопасность" to 1,
        "Дискретная математика" to 6,
        "Управление данными" to 1,
        "Микроэлектроника и схемотехника" to 1,
        "Моделирование процессов и систем" to 4,
        "Системы и системный анализ" to 3,
        "Микропроцессоры" to 1,
        "Информационный менеджмент" to 3,
        "Корпоративные информационные системы" to 3,
        "Основы теории управления" to 1,
        "Языки программирования" to 3,
        "Основы WEB-инжиниринга" to 1,
        "Кроссплатформенные приложения" to 3,
        "Управление ИТ-проектами" to 3
    )

    val disciplineDao by inject<DisciplineDao>()

    disciplineDao.apply {
        runBlocking {
            if (allDisciplines().isEmpty()) {
                println("Creating dummy disciplines...")

                val time = measureTimeMillis {
                    disciplinesDepartments.forEach { (title, departmentId) ->
                        addNewDiscipline(
                            title = title,
                            departmentId = departmentId
                        )
                    }
                }

                println("Dummy disciplines created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyFaculties() {
    val facultyDao by inject<FacultyDao>()

    val titles = listOf("Прикладной информатики")

    facultyDao.apply {
        runBlocking {
            if (allFaculties(null, null).isEmpty()) {
                println("Creating dummy faculties...")

                val time = measureTimeMillis {
                    titles.forEach { title -> facultyDao.addNewFaculty(title) }
                }

                println("Dummy faculties created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyEmployeesFaculties() {
    val employeeFacultyDao by inject<EmployeeFacultyDao>()

    employeeFacultyDao.apply {
        runBlocking {
            if (allReferences().isEmpty()) {
                println("Creating dummy employees faculties references...")

                val time = measureTimeMillis {
                    addNewReference(1, 1)
                }

                println("Dummy employees faculties references created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyHeads() {
    val heads = listOf(
        Triple("09.03.04", "Информационные системы и технологии", "ИТ"),
        Triple("09.03.03", "Прикладная информатика", "ПИ"),
        Triple("38.03.05", "Бизнес-информатика", "БИ")
    )

    val headDao by inject<HeadDao>()

    headDao.apply {
        runBlocking {
            if (allHeads(null, null, null).isEmpty()) {
                println("Creating dummy heads...")

                val time = measureTimeMillis {
                    heads.forEachIndexed { _, (code, title, abbreviation) ->
                        addNewHead(
                            code = code,
                            abbreviation = abbreviation,
                            title = title,
                            facultyId = 1
                        )
                    }
                }

                println("Dummy heads created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyDirectivities() {
    val directivityDao by inject<DirectivityDao>()

    directivityDao.apply {
        runBlocking {
            if (allDirectivities(null, null, null).isEmpty()) {
                println("Creating dummy directivities...")

                val time = measureTimeMillis {
                    addNewDirectivity(
                        title = "Создание, модификация и сопровождение информационных систем, администрирование баз данных",
                        headId = 1,
                        gradeId = 1
                    )
                    addNewDirectivity(
                        title = "Менеджмент проектов в области информационных технологий, создание и поддержка информационных систем",
                        headId = 2,
                        gradeId = 1
                    )
                    addNewDirectivity(
                        title = "Анализ, моделирование и формирование интегрального представления стратегий и целей, бизнес-процессов и информационно-логической инфраструктуры предприятий и организаций",
                        headId = 3,
                        gradeId = 1
                    )
                }

                println("Dummy directivities created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyGroups() {
    val groupDao by inject<GroupDao>()
    val headDao by inject<HeadDao>()
    val directivityDao by inject<DirectivityDao>()

    groupDao.apply {
        runBlocking {
            val headTuples = headDao.allHeads(null, null, null).map {
                it.id to it.abbreviation
            }

            if (allGroups(null).isEmpty()) {
                println("Creating dummy groups...")

                val directivities = directivityDao.allDirectivities(null, null, null)

                val time = measureTimeMillis {
                    headTuples.forEach { (headId, abbreviation) ->
                        (1..if (headId == 3) 1 else 3).forEach { value ->
                            addNewGroup(
                                title = "${abbreviation}200$value",
                                directivityId = directivities.first { it.headId == headId }.id
                            )
                        }
                    }
                }

                println("Dummy groups created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyStudentStatuses() {
    val studentStatusDao by inject<StudentStatusDao>()

    studentStatusDao.apply {
        runBlocking {
            if (allStatuses().isEmpty()) {
                println("Creating dummy students statuses...")

                val time = measureTimeMillis {
                    addNewStatus("Учится")
                    addNewStatus("Отчислен")
                    addNewStatus("Академ")
                }

                println("Dummy students statuses created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyStudents() {
    val names1 = ("Абросимов Ярослав Валерьевич\n" +
            "Абу Раид Хумам\n" +
            "Бекчентаева Виктория Рустамовна\n" +
            "Берон Григорий Игорьевич\n" +
            "Бесхлебный Владислав Алексеевич\n" +
            "Бобылева Елизавета Евгеньевна\n" +
            "Бордюжа Дмитрий Алексеевич\n" +
            "Бутенко Виктория Руслановна\n" +
            "Варавва Дмитрий Олегович\n" +
            "Величко Артём Сергеевич\n" +
            "Дьяченко Никита Юрьевич\n" +
            "Кожухар Марина Константиновна\n" +
            "Котовенко Александр Юрьевич\n" +
            "Курдаев Олег Иванович\n" +
            "Лебедев Святослав Витальевич\n" +
            "Леонов Илья Евгеньевич\n" +
            "Лобанов Николай Алексеевич\n" +
            "Манохин Антон Юрьевич\n" +
            "Мингазова Алина Илдусова\n" +
            "Мурзин Вячеслав Михайлович\n" +
            "Николаев Данил Станиславович\n" +
            "Павелко Константин Алексеевич\n" +
            "Пиданов Марк Витальевич\n" +
            "Погуляйло Вадим Андреевич\n" +
            "Рябов Алексей Алексеевич\n" +
            "Шепетило Константин Валерьевич\n" +
            "Яценко Никита Алексеевич")
        .split("\n")

    val names2 = ("Крюков Егор Богданович\n" +
            "Филиппов Марк Фёдорович\n" +
            "Михайлов Андрей Андреевич\n" +
            "Плотников Артём Давидович\n" +
            "Егоров Роман Даниэльевич\n" +
            "Овчинников Михаил Никитич\n" +
            "Митрофанов Александр Сергеевич\n" +
            "Лапина Виктория Артёмовна\n" +
            "Бурова Анастасия Александровна\n" +
            "Молчанова Кира Марковна\n" +
            "Дементьев Матвей Иванович\n" +
            "Попова Таисия Мироновна\n" +
            "Герасимов Фёдор Антонович\n" +
            "Рыбаков Дмитрий Павлович\n" +
            "Колосов Денис Романович\n" +
            "Виноградова Сафия Фёдоровна\n" +
            "Титова Анастасия Сергеевна\n" +
            "Михайлова Анна Данииловна\n" +
            "Токарев Всеволод Егорович\n" +
            "Гончарова Мария Лукинична")
        .split("\n")

    val names3 = ("Лаврова Софья Владиславовна\n" +
            "Комиссарова Арина Михайловна\n" +
            "Скворцова Ника Тимофеевна\n" +
            "Иванов Михаил Владиславович\n" +
            "Майорова Анна Сергеевна\n" +
            "Сизов Лев Михайлович\n" +
            "Александров Евгений Платонович\n" +
            "Кузнецова Ольга Артёмовна\n" +
            "Дегтярев Семён Маркович\n" +
            "Третьяков Макар Артёмович\n" +
            "Клюева Полина Дмитриевна\n" +
            "Калинина Полина Марковна\n" +
            "Медведева Наталья Лукинична\n" +
            "Касьянова Евгения Макаровна\n" +
            "Маслов Савва Тимофеевич\n" +
            "Елисеев Марк Данилович\n" +
            "Захарова Алиса Сергеевна\n" +
            "Журавлева Стефания Матвеевна\n" +
            "Рыбаков Даниил Тихонович\n" +
            "Матвеева Анна Вячеславовна")
        .split("\n")

    val studentDao by inject<StudentDao>()

    studentDao.apply {
        runBlocking {
            if (allStudents(null, null).isEmpty()) {
                println("Creating dummy students...")

                val time = measureTimeMillis {
                    names1.map { it.split(" ") }.forEach { (lastName, firstName, middleName) ->
                        addNewStudent(
                            firstName = firstName,
                            lastName = lastName,
                            middleName = middleName,
                            groupId = 1,
                            statusId = when (lastName) {
                                "Бекчентаева" -> 3
                                "Лебедев" -> 2
                                else -> 1
                            }
                        )
                    }

                    names2.map { it.split(" ") }.forEach { (lastName, firstName, middleName) ->
                        addNewStudent(
                            firstName = firstName,
                            lastName = lastName,
                            middleName = middleName,
                            groupId = 4,
                            statusId = 1
                        )
                    }

                    names3.map { it.split(" ") }.forEach { (lastName, firstName, middleName) ->
                        addNewStudent(
                            firstName = firstName,
                            lastName = lastName,
                            middleName = middleName,
                            groupId = 7,
                            statusId = 1
                        )
                    }
                }

                println("Dummy students created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyWorkTypes() {
    val workTypeDao by inject<WorkTypeDao>()

    workTypeDao.apply {
        runBlocking {
            if (allWorkTypes().isEmpty()) {
                println("Creating dummy work types...")

                val time = measureTimeMillis {
                    addNewWorkType("Курсовая", true)
                    addNewWorkType("Лабораторная", false)
                    addNewWorkType("Рассчётно-графическая", true)
                    addNewWorkType("Практика", false)
                }

                println("Dummy work types created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyWorks() {
    return
    val workDao by inject<WorkDao>()
    val workTypeDao by inject<WorkTypeDao>()
    val disciplineDao by inject<DisciplineDao>()
    val studentDao by inject<StudentDao>()
    val employeeDao by inject<EmployeeDao>()

    runBlocking {
        val works = workDao.allWorks(null, null, null)

        if (works.isEmpty()) {
            println("Creating dummy works...")

            val time = measureTimeMillis {
                val workTypeIds = workTypeDao.allWorkTypes().map(WorkType::id)
                val disciplines = disciplineDao.allDisciplines()
                val studentIds = studentDao.allStudents(null, null).map(Student::id)
                val employeeIds = employeeDao.allTeachers(null, null, null).map(Employee::id)

                repeat(100) { index ->
                    val discipline = disciplines.random()
                    workDao.addNewWork(
                        disciplineId = discipline.id,
                        studentId = studentIds.random(),
                        registrationDate = getRandomUnixTime(),
                        title = "Work #${index + 1}",
                        workTypeId = workTypeIds.random(),
                        employeeId = employeeIds.random()
                    )
                }
            }

            println("Dummy works created. Took ${time}ms")
        }
    }
}

private fun Application.createDummyPrograms() {
    val directivityDao by inject<DirectivityDao>()
    val programDao by inject<ProgramDao>()

    programDao.apply {
        runBlocking {
            val directivities = directivityDao.allDirectivities(null, null, null)

            if (allPrograms(null, null).isEmpty()) {
                println("Creating dummy programs...")

                val time = measureTimeMillis {
                    directivities.forEach { directivity ->
                        (1..8).forEach { semester ->
                            addNewProgram(
                                semester = semester,
                                directivityId = directivity.id
                            )
                        }
                    }
                }

                println("Dummy programs created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyProgramsDisciplines() {
    val programDisciplineDao by inject<ProgramDisciplineDao>()

    programDisciplineDao.apply {
        runBlocking {
            if (programDisciplineDao.allReferences(null, null).isEmpty()) {
                println("Creating dummy programs-disciplines references...")

                val time = measureTimeMillis {
                    addNewReference(
                        programId = 1,
                        disciplineId = 13,
                        workTypeId = 1
                    )
                    addNewReference(
                        programId = 1,
                        disciplineId = 13,
                        workTypeId = 2
                    )
                    addNewReference(
                        programId = 1,
                        disciplineId = 13,
                        workTypeId = 2
                    )
                    addNewReference(
                        programId = 1,
                        disciplineId = 24,
                        workTypeId = 2
                    )
                    addNewReference(
                        programId = 1,
                        disciplineId = 13,
                        workTypeId = 2
                    )
                    addNewReference(
                        programId = 1,
                        disciplineId = 6,
                        workTypeId = 4
                    )
                    addNewReference(
                        programId = 1,
                        disciplineId = 4,
                        workTypeId = 4
                    )
                    addNewReference(
                        programId = 1,
                        disciplineId = 11,
                        workTypeId = 2
                    )
                    addNewReference(
                        programId = 1,
                        disciplineId = 7,
                        workTypeId = 4
                    )
                    addNewReference(
                        programId = 1,
                        disciplineId = 10,
                        workTypeId = 4
                    )
                    addNewReference(
                        programId = 1,
                        disciplineId = 22,
                        workTypeId = 4
                    )

                    addNewReference(
                        programId = 1,
                        disciplineId = 10,
                        workTypeId = 4
                    )


                    addNewReference(
                        programId = 2,
                        disciplineId = 9,
                        workTypeId = 4
                    )
                    addNewReference(
                        programId = 2,
                        disciplineId = 6,
                        workTypeId = 4
                    )
                    addNewReference(
                        programId = 2,
                        disciplineId = 2,
                        workTypeId = 2
                    )
                    addNewReference(
                        programId = 2,
                        disciplineId = 15,
                        workTypeId = 2
                    )
                    addNewReference(
                        programId = 2,
                        disciplineId = 1,
                        workTypeId = 4
                    )
                    addNewReference(
                        programId = 2,
                        disciplineId = 12,
                        workTypeId = 2
                    )
                    addNewReference(
                        programId = 2,
                        disciplineId = 3,
                        workTypeId = 3
                    )
                    addNewReference(
                        programId = 2,
                        disciplineId = 2,
                        workTypeId = 4
                    )

                    addNewReference(
                        programId = 3,
                        disciplineId = 16,
                        workTypeId = 2
                    )
                    addNewReference(
                        programId = 3,
                        disciplineId = 16,
                        workTypeId = 1
                    )
                    addNewReference(
                        programId = 3,
                        disciplineId = 16,
                        workTypeId = 2
                    )
                    addNewReference(
                        programId = 3,
                        disciplineId = 8,
                        workTypeId = 1
                    )
                    addNewReference(
                        programId = 3,
                        disciplineId = 8,
                        workTypeId = 2
                    )
                    addNewReference(
                        programId = 3,
                        disciplineId = 18,
                        workTypeId = 2
                    )
                    addNewReference(
                        programId = 3,
                        disciplineId = 6,
                        workTypeId = 4
                    )
                    addNewReference(
                        programId = 3,
                        disciplineId = 3,
                        workTypeId = 2
                    )
                    addNewReference(
                        programId = 3,
                        disciplineId = 5,
                        workTypeId = 4
                    )
                    addNewReference(
                        programId = 3,
                        disciplineId = 16,
                        workTypeId = 4
                    )

                    addNewReference(
                        programId = 4,
                        disciplineId = 17,
                        workTypeId = 1
                    )
                    addNewReference(
                        programId = 4,
                        disciplineId = 17,
                        workTypeId = 4
                    )
                    addNewReference(
                        programId = 4,
                        disciplineId = 25,
                        workTypeId = 1
                    )
                    addNewReference(
                        programId = 4,
                        disciplineId = 25,
                        workTypeId = 2
                    )
                    addNewReference(
                        programId = 4,
                        disciplineId = 6,
                        workTypeId = 4
                    )
                    addNewReference(
                        programId = 4,
                        disciplineId = 17,
                        workTypeId = 2
                    )
                    addNewReference(
                        programId = 4,
                        disciplineId = 26,
                        workTypeId = 2
                    )
                    addNewReference(
                        programId = 4,
                        disciplineId = 22,
                        workTypeId = 4
                    )
                    addNewReference(
                        programId = 4,
                        disciplineId = 33,
                        workTypeId = 2
                    )

                    addNewReference(
                        programId = 5,
                        disciplineId = 19,
                        workTypeId = 1
                    )
                    addNewReference(
                        programId = 5,
                        disciplineId = 19,
                        workTypeId = 2
                    )
                    addNewReference(
                        programId = 5,
                        disciplineId = 6,
                        workTypeId = 4
                    )
                    addNewReference(
                        programId = 5,
                        disciplineId = 20,
                        workTypeId = 2
                    )
                    addNewReference(
                        programId = 5,
                        disciplineId = 29,
                        workTypeId = 2
                    )
                    addNewReference(
                        programId = 5,
                        disciplineId = 22,
                        workTypeId = 4
                    )

                    addNewReference(
                        programId = 6,
                        disciplineId = 6,
                        workTypeId = 4
                    )
                    addNewReference(
                        programId = 6,
                        disciplineId = 29,
                        workTypeId = 2
                    )
                    addNewReference(
                        programId = 6,
                        disciplineId = 28,
                        workTypeId = 2
                    )
                    addNewReference(
                        programId = 6,
                        disciplineId = 22,
                        workTypeId = 4
                    )


                    addNewReference(
                        programId = 7,
                        disciplineId = 31,
                        workTypeId = 1
                    )
                    addNewReference(
                        programId = 7,
                        disciplineId = 30,
                        workTypeId = 1
                    )
                    addNewReference(
                        programId = 7,
                        disciplineId = 30,
                        workTypeId = 2
                    )
                    addNewReference(
                        programId = 7,
                        disciplineId = 31,
                        workTypeId = 2
                    )
                    addNewReference(
                        programId = 7,
                        disciplineId = 34,
                        workTypeId = 2
                    )

                    addNewReference(
                        programId = 8,
                        disciplineId = 32,
                        workTypeId = 3
                    )
                    addNewReference(
                        programId = 8,
                        disciplineId = 21,
                        workTypeId = 2
                    )
                    addNewReference(
                        programId = 8,
                        disciplineId = 17,
                        workTypeId = 4
                    )
                    addNewReference(
                        programId = 8,
                        disciplineId = 35,
                        workTypeId = 2
                    )
                    addNewReference(
                        programId = 8,
                        disciplineId = 36,
                        workTypeId = 2
                    )
                }

                println("Dummy programs-disciplines references created. Took ${time}ms")
            }
        }
    }
}


private fun getRandomUnixTime(): Long {
    val (startTime, endTime) = 1609459200L to 1708775961L
    require(startTime < endTime) { "Start time must be before end time" }
    return Random.nextLong(startTime, endTime) * 1000
}
