package com.meloda.kubsau

import com.meloda.kubsau.common.Constants
import com.meloda.kubsau.common.getEnvOrNull
import com.meloda.kubsau.common.isInDocker
import com.meloda.kubsau.common.toLogString
import com.meloda.kubsau.database.DatabaseController
import com.meloda.kubsau.database.departments.DepartmentsDao
import com.meloda.kubsau.database.directivities.DirectivitiesDao
import com.meloda.kubsau.database.disciplines.DisciplinesDao
import com.meloda.kubsau.database.employees.EmployeesDao
import com.meloda.kubsau.database.employeesdepartments.EmployeesDepartmentsDao
import com.meloda.kubsau.database.employeesfaculties.EmployeesFacultiesDao
import com.meloda.kubsau.database.employeetypes.EmployeeTypesDao
import com.meloda.kubsau.database.faculties.FacultiesDao
import com.meloda.kubsau.database.groups.GroupsDao
import com.meloda.kubsau.database.heads.HeadsDao
import com.meloda.kubsau.database.programs.ProgramsDao
import com.meloda.kubsau.database.programsdisciplines.ProgramsDisciplinesDao
import com.meloda.kubsau.database.students.StudentsDao
import com.meloda.kubsau.database.studentstatuses.StudentStatusesDao
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
import io.ktor.server.request.*
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.inject
import org.slf4j.event.Level
import kotlin.random.Random
import kotlin.system.measureTimeMillis

val PORT = getEnvOrNull("PORT")?.toIntOrNull() ?: 8080

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
        install(CallLogging) {
            level = Level.INFO
            filter { call ->
                call.request.path().startsWith("/")
            }
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

        configureKoin()
        prepopulateDB()

        routing()
    }

    println("Server's version: ${Constants.BACKEND_VERSION}")
    println("Is inside Docker: $isInDocker")
    println("Port: $PORT")

    server.start(wait = true)
}

private fun Application.prepopulateDB() {
    // TODO: 10/04/2024, Danil Nikolaev: import data from json

    createDummyUsers()

    createDummyEmployeeTypes()
    createDummyEmployees()

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
}

private fun Application.createDummyUsers() {
    val usersDao by inject<UsersDao>()

    usersDao.apply {
        runBlocking {
            if (allUsers().isEmpty()) {
                println("Creating dummy users...")

                val time = measureTimeMillis {
                    addNewUser(login = "lischenkodev@gmail.com", password = "123456", type = 1, employeeId = 1)
                    addNewUser(login = "m.kozhukhar@gmail.com", password = "789012", type = 1, employeeId = 2)
                    addNewUser(login = "ya.abros@gmail.com", password = "345678", type = 1, employeeId = 3)
                    addNewUser(login = "email@domain.com", password = "123456", type = 2, employeeId = 4)
                }

                println("Dummy users created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyEmployeeTypes() {
    val employeeTypesDao by inject<EmployeeTypesDao>()

    employeeTypesDao.apply {
        runBlocking {
            if (allTypes().isEmpty()) {
                println("Creating dummy employee types...")

                val time = measureTimeMillis {
                    addNewType(title = "Администратор")
                    addNewType(title = "Преподаватель")
                    addNewType(title = "Кафедра")
                }

                println("Dummy employee types created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyEmployees() {
    val employeesDao by inject<EmployeesDao>()
    val employeeTypesDao by inject<EmployeeTypesDao>()

    employeesDao.apply {
        runBlocking {
            if (allEmployees().isEmpty()) {
                println("Creating dummy employees...")

                val employeeTypeIds = employeeTypesDao.allTypes().map(EmployeeType::id)

                val time = measureTimeMillis {
                    addNewEmployee(
                        lastName = "Николаев",
                        firstName = "Данил",
                        middleName = "Станиславович",
                        email = "lischenkodev@gmail.com",
                        employeeTypeId = employeeTypeIds.first()
                    )
                    addNewEmployee(
                        lastName = "Кожухар",
                        firstName = "Марина",
                        middleName = "Константиновна",
                        email = "m.kozhukhar@gmail.com",
                        employeeTypeId = employeeTypeIds.last()
                    )
                    addNewEmployee(
                        lastName = "Абросимов",
                        firstName = "Ярослав",
                        middleName = "Валерьевич",
                        email = "ya.abros@gmail.com",
                        employeeTypeId = employeeTypeIds[1]
                    )
                }

                println("Dummy employees created. Took ${time}ms")
            }
        }
    }
}


private fun Application.createDummyDepartments() {
    val departmentsDao by inject<DepartmentsDao>()

    val strings = listOf(
        "АгрохимииsepАдминистративного и финансового праваsepАнатомии, ветеринарного акушерства и хирургииsepАрхитектурыsepАудитаsepБиотехнологии, биохимии и биофизикиsepБотаники и общей экологииsepБухгалтерского учетаsepВиноградарстваsepВысшей математикиsepГенетики, селекции и семеноводстваsepГеодезииsepГидравлики и с.х. водоснабженияsepГосударственного и международного праваsepГосударственного и муниципального управленияsepГражданского праваsepГражданского процессаsepДенежного обращения и кредитаsepЗемельного, трудового и экологического праваsepЗемлеустройства и земельного кадастра",
        "Иностранных языковsepИнституциональной экономики и инвестиционного менеджментаsepИнформационных системsepИстории и политологииsepКомплексных систем водоснабженияsepКомпьютерных технологий и системsepКриминалистикиsepМеждународного частного и предпринимательского праваsepМенеджментаsepМеханизации животноводства и БЖДsepМикробиологии, эпизоотологии и вирусологииsepОбщего и орошаемого земледелияsepОвощеводстваsepОрганизации производства и инновационной деятельностиsepОснований и фундаментовsepПаразитологии, ветсанэкспертизы и зоогигиеныsepПедагогики и психологииsepПлодоводстваsepПочвоведенияsepПрикладной экологии",
        "Процессы и машины в агробизнесеsepРазведения с.х. животных и зоотехнологийsepРастениеводстваsepРусского языка и речевой коммуникацииsepСистемного анализа и обработки информацииsepСопротивления материаловsepСоциологии и культурологииsepСтатистики и прикладной математикиsepСтроительного производстваsepСтроительных материалов и конструкцийsepСтроительства и эксплуатации ВХОsepТеории бухгалтерского учетаsepТеории и истории государства и праваsepТерапии и фармакологииsepТехнологии хранения и переработки животноводческой продукцииsepТехнологии хранения и переработки растениеводческой продукцииsepТракторов, автомобилей и технической механикиsepУголовного праваsepУголовного процессаsepУправления и маркетинга",
        "ФизвоспитанияsepФизикиsepФизиологии и биохимии растенийsepФизиологии и кормления с.х. животныхsepФилософииsepФинансовsepФитопатологии, энтомологии и защиты растенийsepХимииsepЧастной зоотехнии и свиноводстваsepЭкономики и внешнеэкономической деятельностиsepЭкономического анализаsepЭкономической кибернетикиsepЭкономической теорииsepЭксплуатации и технического сервисаsepЭлектрических машин и электроприводаsepЭлектроснабженияsepЭлектротехники, теплотехники и возобновляемых источников энергии"
    )

    val titles = mutableListOf<String>()

    strings.forEach { string ->
        string.split("sep").forEach { title ->
            titles += title
        }
    }

    departmentsDao.apply {
        runBlocking {
            if (allDepartments().isEmpty()) {
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

private fun Application.createDummyEmployeesDepartments() {
    val employeesDepartmentsDao by inject<EmployeesDepartmentsDao>()
    val employeesDao by inject<EmployeesDao>()
    val departmentsDao by inject<DepartmentsDao>()

    employeesDepartmentsDao.apply {
        runBlocking {
            if (allReferences().isEmpty()) {
                println("Creating dummy employees departments references...")

                val employeeIds = employeesDao.allEmployees().map(Employee::id)
                val departmentIds = departmentsDao.allDepartments().map(Department::id)

                val time = measureTimeMillis {
                    List(20) {
                        employeeIds.random() to departmentIds.random()
                    }.distinct().forEach { (employeeId, departmentId) ->
                        addNewReference(
                            employeeId = employeeId,
                            departmentId = departmentId
                        )
                    }
                }

                println("Dummy employees departments references created. Took ${time}ms")
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
    val departmentsDao by inject<DepartmentsDao>()

    disciplinesDao.apply {
        runBlocking {
            if (allDisciplines().isEmpty()) {
                println("Creating dummy disciplines...")

                val departmentIds = departmentsDao.allDepartments().map(Department::id)

                val time = measureTimeMillis {
                    disciplinesString.forEach { title ->
                        addNewDiscipline(
                            title = title,
                            departmentId = departmentIds.random()
                        )
                    }
                }

                println("Dummy disciplines created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyFaculties() {
    val facultiesDao by inject<FacultiesDao>()

    facultiesDao.apply {
        runBlocking {
            if (allFaculties().isEmpty()) {
                println("Creating dummy faculties...")

                val time = measureTimeMillis {
                    repeat(20) { index ->
                        addNewFaculty("Faculty #${index + 1}")
                    }
                }

                println("Dummy faculties created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyEmployeesFaculties() {
    val employeesFacultiesDao by inject<EmployeesFacultiesDao>()
    val employeesDao by inject<EmployeesDao>()
    val facultiesDao by inject<FacultiesDao>()

    employeesFacultiesDao.apply {
        runBlocking {
            if (allReferences().isEmpty()) {
                println("Creating dummy employees faculties references...")

                val employeeIds = employeesDao.allEmployees().map(Employee::id)
                val facultyIds = facultiesDao.allFaculties().map(Faculty::id)

                val time = measureTimeMillis {
                    List(20) {
                        employeeIds.random() to facultyIds.random()
                    }.distinct().forEach { (employeeId, facultyId) ->
                        addNewReference(
                            employeeId = employeeId,
                            facultyId = facultyId
                        )
                    }
                }

                println("Dummy employees faculties references created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyHeads() {
    val heads = listOf(
        Triple("05.03.06", "Экология и природоведение", "ЭиП"),
        Triple("08.03.01", "Строительство", "СТР"),
        Triple("09.03.04", "Информационные системы и технологии", "ИСИТ"),
        Triple("09.03.03", "Прикладная информатика", "ПИ"),
        Triple("13.03.02", "Электроэнергетика и электротехника", "ЭЛЭЛ"),
        Triple("19.03.02", "Продукты питания из растительного сырья", "ППРС"),
        Triple("20.03.02", "Природоустройство и водопользование", "ПиВ"),
        Triple("21.03.02", "Землеустройство и кадастры", "ЗиК"),
        Triple("35.03.03", "Агрохимия и агропочвоведение", "АиА"),
        Triple("35.03.04", "Агрономия", "АГР"),
        Triple("35.03.05", "Садоводство", "САД"),
        Triple("35.03.06", "Агроинженерия", "АГРИНЖ"),
        Triple("35.03.07", "Технология производства и переработки сельскохозяйственной продукции", "ТПИС"),
        Triple("36.03.01", "Ветеринарно-санитарная экспертиза", "ВСЭ"),
        Triple("36.03.02", "Зоотехния", "ЗТ"),
        Triple("38.03.01", "Экономика", "ЭК"),
        Triple("38.03.02", "Менеджмент", "МЕН"),
        Triple("38.03.04", "Государственное и муниципальное управление", "ГИМУ"),
        Triple("38.03.05", "Бизнес-информатика", "БИ"),
        Triple("40.03.01", "Юриспруденция", "ЮР"),
    )

    val headsDao by inject<HeadsDao>()
    val facultiesDao by inject<FacultiesDao>()

    headsDao.apply {
        runBlocking {
            if (allHeads().isEmpty()) {
                println("Creating dummy heads...")

                val facultyIds = facultiesDao.allFaculties().map(Faculty::id)

                val time = measureTimeMillis {
                    heads.forEach { (code, title, abbreviation) ->
                        addNewHead(
                            code = code,
                            abbreviation = abbreviation,
                            title = title,
                            facultyId = facultyIds.random()
                        )
                    }
                }

                println("Dummy heads created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyDirectivities() {
    val headsDao by inject<HeadsDao>()
    val directivitiesDao by inject<DirectivitiesDao>()

    directivitiesDao.apply {
        runBlocking {
            if (allDirectivities().isEmpty()) {
                println("Creating dummy directivities...")

                val headIds = headsDao.allHeads().map(Head::id)

                val time = measureTimeMillis {
                    addNewDirectivity(
                        title = "Экология и природопользование",
                        headId = headIds.random()
                    )
                    addNewDirectivity(
                        title = "Проектирование зданий",
                        headId = headIds.random()
                    )
                    addNewDirectivity(
                        title = "Промышленное и гражданское строительство",
                        headId = headIds.random()
                    )
                    addNewDirectivity(
                        title = "Создание, модификация и сопровождение информационных систем, администрирование баз данных",
                        headId = headIds.random()
                    )
                    addNewDirectivity(
                        title = "Менеджмент проектов в области информационных технологий, создание и поддержка информационных систем",
                        headId = headIds.random()
                    )
                    addNewDirectivity(
                        title = "Электроснабжение",
                        headId = headIds.random()
                    )
                    addNewDirectivity(
                        title = "Производство продуктов питания из растительного сырья",
                        headId = headIds.random()
                    )
                    addNewDirectivity(
                        title = "Инженерные системы сельскохозяйственного снабжения, обводнения и водоотделения",
                        headId = headIds.random()
                    )
                    addNewDirectivity(
                        title = "Землеустройство и кадастры",
                        headId = headIds.random()
                    )
                    addNewDirectivity(
                        title = "Почвенно-агрохимическое обеспечение АПК",
                        headId = headIds.random()
                    )
                    addNewDirectivity(
                        title = "Защита растений",
                        headId = headIds.random()
                    )
                    addNewDirectivity(
                        title = "Декоративное садоводство, плодоовощеводство, виноградство и виноделие",
                        headId = headIds.random()
                    )
                    addNewDirectivity(
                        title = "Технические системы в агробизнесе",
                        headId = headIds.random()
                    )
                    addNewDirectivity(
                        title = "Ветеринарно-санитарная экспертиза",
                        headId = headIds.random()
                    )
                    addNewDirectivity(
                        title = "Технология производства продуктов животноводства",
                        headId = headIds.random()
                    )
                    addNewDirectivity(
                        title = "Бизнес-аналитика",
                        headId = headIds.random()
                    )
                    addNewDirectivity(
                        title = "Инновационный менеджмент",
                        headId = headIds.random()
                    )
                    addNewDirectivity(
                        title = "Государственное и муниципальное управление",
                        headId = headIds.random()
                    )
                    addNewDirectivity(
                        title = "Анализ, моделирование и формирование интегрального представления стратегий и целей, бизнес-процессов и информационно-логической инфраструктуры предприятий и организаций",
                        headId = headIds.random()
                    )
                    addNewDirectivity(
                        title = "Гражданско-правовой",
                        headId = headIds.random()
                    )
                }

                println("Dummy directivities created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyGroups() {
    val groupsDao by inject<GroupsDao>()
    val headsDao by inject<HeadsDao>()
    val directivitiesDao by inject<DirectivitiesDao>()

    groupsDao.apply {
        runBlocking {
            val headTuples = headsDao.allHeads().map {
                it.id to it.abbreviation
            }

            if (allGroups().isEmpty()) {
                println("Creating dummy groups...")

                val directivities = directivitiesDao.allDirectivities()

                val time = measureTimeMillis {
                    headTuples.forEach { (headId, abbreviation) ->
                        repeat(3) {
                            addNewGroup(
                                title = "$abbreviation${Random.nextInt(from = 2001, until = 2004)}",
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
    val studentStatusesDao by inject<StudentStatusesDao>()

    studentStatusesDao.apply {
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
    val names = ("Абросимов Ярослав Валерьевич\n" +
            "Абу Раид Хумам\n" +
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
            "Яценко Никита Алексеевич").split("\n")

    val groupsDao by inject<GroupsDao>()
    val studentsDao by inject<StudentsDao>()
    val studentStatusesDao by inject<StudentStatusesDao>()

    studentsDao.apply {
        runBlocking {
            val groups = groupsDao.allGroups().map(Group::id)

            if (allStudents().isEmpty()) {
                println("Creating dummy students...")

                val statusIds = studentStatusesDao.allStatuses().map(StudentStatus::id)

                val time = measureTimeMillis {
                    groups.forEach { groupId ->
                        List(25) {
                            names.random() to statusIds.random()
                        }.distinct().forEach { (fullName, statusId) ->
                            val (lastName, firstName, middleName) = fullName.split(" ")

                            addNewStudent(
                                firstName = firstName,
                                lastName = lastName,
                                middleName = middleName,
                                groupId = groupId,
                                statusId = statusId
                            )
                        }
                    }
                }

                println("Dummy students created. Took ${time}ms")
            }
        }
    }
}

//private fun Application.createDummyTeachers() {
//    val departmentsDao by inject<DepartmentsDao>()
//    val teachersDao by inject<TeachersDao>()
//
//    teachersDao.apply {
//        runBlocking {
//            if (allTeachers().size < 10) {
//                println("Creating dummy teachers...")
//
//                val time = measureTimeMillis {
//                    val departmentIds = departmentsDao.allDepartments().map(Department::id)
//
//                    repeat(10) {
//                        val nameSplit = studentNames.random().split(" ")
//
//                        addNewTeacher(
//                            firstName = nameSplit[0],
//                            lastName = nameSplit[1],
//                            middleName = nameSplit[2],
//                            departmentId = departmentIds.random()
//                        )
//                    }
//                }
//
//                println("Dummy teachers created. Took ${time}ms")
//            }
//        }
//    }
//}

//private fun Application.createDummySpecializationsDisciplines() {
//    val specializationsDisciplinesDao by inject<SpecializationsDisciplinesDao>()
//    val specializationsDao by inject<SpecializationsDao>()
//    val disciplinesDao by inject<DisciplinesDao>()
//
//    runBlocking {
//        if (specializationsDisciplinesDao.allItems().size < 100) {
//            println("Creating dummy specializations-disciplines references...")
//
//            val time = measureTimeMillis {
//                val specializationIds = specializationsDao.allSpecializations().map(Specialization::id)
//                val disciplineIds = disciplinesDao.allDisciplines().map(Discipline::id)
//
//                repeat(100) {
//                    specializationsDisciplinesDao.addNewReference(
//                        specializationId = specializationIds.random(),
//                        disciplineId = disciplineIds.random()
//                    )
//                }
//            }
//
//            println("Dummy specializations-disciplines references created. Took ${time}ms")
//        }
//    }
//}

//private fun Application.createDummyTeachersDisciplines() {
//    val teachersDisciplinesDao by inject<TeachersDisciplinesDao>()
//    val teachersDao by inject<TeachersDao>()
//    val disciplinesDao by inject<DisciplinesDao>()
//
//    runBlocking {
//        if (teachersDisciplinesDao.allItems().size < 100) {
//            println("Creating dummy teachers-disciplines references...")
//
//            val time = measureTimeMillis {
//                val teacherIds = teachersDao.allTeachers().map(Teacher::id)
//                val disciplineIds = disciplinesDao.allDisciplines().map(Discipline::id)
//
//                repeat(100) {
//                    teachersDisciplinesDao.addNewReference(
//                        teacherId = teacherIds.random(),
//                        disciplineId = disciplineIds.random()
//                    )
//                }
//            }
//
//            println("Dummy teachers-disciplines references created. Took ${time}ms")
//        }
//    }
//}

private fun Application.createDummyWorkTypes() {
    val workTypesDao by inject<WorkTypesDao>()

    workTypesDao.apply {
        runBlocking {
            if (allWorkTypes().isEmpty()) {
                println("Creating dummy work types...")

                val time = measureTimeMillis {
                    addNewWorkType("Курсовая", true)
                    addNewWorkType("Лабораторная", false)
                    addNewWorkType("Рассчётно-графическая", true)
                }

                println("Dummy work types created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyWorks() {
    val worksDao by inject<WorksDao>()
    val workTypesDao by inject<WorkTypesDao>()
    val disciplinesDao by inject<DisciplinesDao>()
    val studentsDao by inject<StudentsDao>()

    runBlocking {
        val works = worksDao.allWorks()

        if (works.isEmpty()) {
            println("Creating dummy works...")

            val time = measureTimeMillis {
                val workTypeIds = workTypesDao.allWorkTypes().map(WorkType::id)
                val disciplineIds = disciplinesDao.allDisciplines().map(Discipline::id)
                val studentIds = studentsDao.allStudents().map(Student::id)

                repeat(10) { index ->
                    worksDao.addNewWork(
                        disciplineId = disciplineIds.random(),
                        studentId = studentIds.random(),
                        registrationDate = getRandomUnixTime(),
                        title = "Work #${index + 1}",
                        workTypeId = workTypeIds.random()
                    )
                }
            }

            println("Dummy works created. Took ${time}ms")
        }
    }
}

private fun Application.createDummyPrograms() {
    val directivitiesDao by inject<DirectivitiesDao>()
    val programsDao by inject<ProgramsDao>()

    programsDao.apply {
        runBlocking {
            val directivities = directivitiesDao.allDirectivities()

            if (allPrograms().isEmpty()) {
                println("Creating dummy programs...")

                val time = measureTimeMillis {
                    directivities.forEach { directivity ->
                        val semester = Random.nextInt(from = 1, until = 13)

                        repeat(12) {
                            addNewProgram(
                                title = "$semester ${directivity.title}",
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
    val programsDisciplinesDao by inject<ProgramsDisciplinesDao>()
    val programsDao by inject<ProgramsDao>()
    val disciplinesDao by inject<DisciplinesDao>()
    val workTypesDao by inject<WorkTypesDao>()

    runBlocking {
        if (programsDisciplinesDao.allReferences().isEmpty()) {
            println("Creating dummy programs-disciplines references...")

            val programIds = programsDao.allPrograms().map(Program::id)
            val disciplineIds = disciplinesDao.allDisciplines().map(Discipline::id)
            val workTypeIds = workTypesDao.allWorkTypes().map(WorkType::id)

            val time = measureTimeMillis {
                repeat(100) {
                    programsDisciplinesDao.addNewReference(
                        programId = programIds.random(),
                        disciplineId = disciplineIds.random(),
                        workTypeId = workTypeIds.random()
                    )
                }
            }

            println("Dummy programs-disciplines references created. Took ${time}ms")
        }
    }
}


private fun getRandomUnixTime(): Long {
    val (startTime, endTime) = 1609459200L to 1708775961L
    require(startTime < endTime) { "Start time must be before end time" }
    return Random.nextLong(startTime, endTime)
}
