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
import com.meloda.kubsau.database.faculties.FacultiesDao
import com.meloda.kubsau.database.grades.GradesDao
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
        configureKoin()
        prepopulateDB()

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

    createDummyGrades()

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

private fun Application.createDummyGrades() {
    val gradesDao by inject<GradesDao>()

    gradesDao.apply {
        runBlocking {
            if (allGrades().isEmpty()) {
                println("Crearing dummy grades...")

                val time = measureTimeMillis {
                    addNewGrade("Бакалавр")
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

private fun Application.createDummyEmployees() {
    val employeesDao by inject<EmployeesDao>()

    val names = ("Савина Валентина Ярославовна\n" +
            "Попов Фёдор Тимофеевич\n" +
            "Чернышева Ксения Михайловна\n" +
            "Фокин Артемий Львович\n" +
            "Романова Дарья Николаевна\n" +
            "Ильина Ульяна Ярославовна\n" +
            "Семенова Василиса Артёмовна\n" +
            "Лебедев Марк Платонович\n" +
            "Чернов Владимир Фёдорович\n" +
            "Румянцев Марк Маркович\n" +
            "Захарова Мария Антоновна\n" +
            "Иванов Алексей Артурович\n" +
            "Борисов Артур Владиславович\n" +
            "Лазарев Никита Ярославович\n" +
            "Семенова Кира Александровна\n" +
            "Зверева Ксения Константиновна\n" +
            "Ульянов Дмитрий Матвеевич\n" +
            "Белов Даниил Артёмович\n" +
            "Орлов Глеб Тимурович\n" +
            "Евдокимова София Кирилловна\n" +
            "Лапшин Михаил Ярославович\n" +
            "Матвеев Дмитрий Макарович\n" +
            "Куликова София Тихоновна\n" +
            "Сычев Иван Степанович\n" +
            "Матвеев Серафим Михайлович\n" +
            "Глухов Максим Егорович\n" +
            "Орехов Михаил Максимович\n" +
            "Федоров Максим Сергеевич\n" +
            "Корнеева Полина Марковна\n" +
            "Дмитриева Дарья Львовна\n" +
            "Григорьева Николь Степановна\n" +
            "Акимов Илья Евгеньевич\n" +
            "Колосова Мария Андреевна\n" +
            "Ефимов Лев Павлович\n" +
            "Петров Иван Маркович\n" +
            "Фролов Иван Максимович\n" +
            "Капустин Давид Семёнович\n" +
            "Чернова Виктория Фёдоровна\n" +
            "Тарасова Полина Ярославовна\n" +
            "Чернышев Всеволод Александрович\n" +
            "Ткачев Матвей Маркович\n" +
            "Лебедева София Тимофеевна\n" +
            "Федотова Яна Вадимовна\n" +
            "Иванов Леонид Даниэльевич\n" +
            "Ильина Анна Львовна\n" +
            "Петровская Софья Ильинична\n" +
            "Белов Роман Константинович\n" +
            "Филиппова Мадина Егоровна\n" +
            "Селезнева Милана Артёмовна\n" +
            "Филиппов Артём Максимович\n" +
            "Ефремов Марсель Фёдорович\n" +
            "Гришин Григорий Артёмович\n" +
            "Гущина Полина Максимовна\n" +
            "Воронова Таисия Константиновна\n" +
            "Никитин Артём Михайлович\n" +
            "Соловьев Михаил Андреевич\n" +
            "Киселева Малика Львовна\n" +
            "Федотова Софья Макаровна\n" +
            "Беспалов Лука Маркович\n" +
            "Карпова Юлия Кирилловна").split("\n")

    employeesDao.apply {
        runBlocking {
            if (allEmployees().isEmpty()) {
                println("Creating dummy employees...")

                val time = measureTimeMillis {
                    addNewEmployee(
                        lastName = "Николаев",
                        firstName = "Данил",
                        middleName = "Станиславович",
                        email = "lischenkodev@gmail.com",
                        type = 1
                    )
                    addNewEmployee(
                        lastName = "Кожухар",
                        firstName = "Марина",
                        middleName = "Константиновна",
                        email = "m.kozhukhar@gmail.com",
                        type = 2
                    )
                    addNewEmployee(
                        lastName = "Абросимов",
                        firstName = "Ярослав",
                        middleName = "Валерьевич",
                        email = "ya.abros@gmail.com",
                        type = 3
                    )
                    addNewEmployee(
                        lastName = "Берон",
                        firstName = "Григорий",
                        middleName = "Игоревич",
                        email = "grisha@google.com",
                        type = 1
                    )

                    names.map { it.split(" ") }
                        .forEach { (lastName, firstName, middleName) ->
                            addNewEmployee(
                                lastName = lastName,
                                firstName = firstName,
                                middleName = middleName,
                                email = "test@test.test",
                                type = 3
                            )
                        }
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

    employeesDepartmentsDao.apply {
        runBlocking {
            if (allReferences().isEmpty()) {
                println("Creating dummy employees departments references...")

                val time = measureTimeMillis {
                    addNewReference(1, 1)
                    addNewReference(2, 2)
                    addNewReference(3, 3)
                    addNewReference(1, 4)
                    addNewReference(2, 5)
                    addNewReference(3, 6)
                    addNewReference(4, 7)
                    addNewReference(4, 8)
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

    val titles = listOf(
        "Агрономии и экологии",
        "Агрохимии и защиты растений",
        "Архитектурно-строительный",
        "Ветеринарная медицина",
        "Гидромелиорации",
        "Землеустроительный",
        "Зоотехнии",
        "Институт цифровой экономики и инноваций",
        "Механизации",
        "Пищевых производств и биотехнологий",
        "Плодоовощеводство и виноградство",
        "Прикладной информатики",
        "Управления",
        "Учётно-финансовый",
        "Финансы и кредиты",
        "Экономический",
        "Энергетики",
        "Юридический",
        "Заочное",
        "Военное обучение"
    )

    facultiesDao.apply {
        runBlocking {
            if (allFaculties().isEmpty()) {
                println("Creating dummy faculties...")

                val time = measureTimeMillis {
                    titles.forEach { title -> facultiesDao.addNewFaculty(title) }
                }

                println("Dummy faculties created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyEmployeesFaculties() {
    val employeesFacultiesDao by inject<EmployeesFacultiesDao>()

    employeesFacultiesDao.apply {
        runBlocking {
            if (allReferences().isEmpty()) {
                println("Creating dummy employees faculties references...")

                val time = measureTimeMillis {
                    addNewReference(1, 12)
                    addNewReference(1, 15)
                    addNewReference(4, 11)
                    addNewReference(4, 14)
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

    headsDao.apply {
        runBlocking {
            if (allHeads().isEmpty()) {
                println("Creating dummy heads...")

                val time = measureTimeMillis {
                    heads.forEachIndexed { index, (code, title, abbreviation) ->
                        addNewHead(
                            code = code,
                            abbreviation = abbreviation,
                            title = title,
                            facultyId = index + 1
                        )
                    }
                }

                println("Dummy heads created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyDirectivities() {
    val directivitiesDao by inject<DirectivitiesDao>()

    directivitiesDao.apply {
        runBlocking {
            if (allDirectivities().isEmpty()) {
                println("Creating dummy directivities...")

                val time = measureTimeMillis {
                    addNewDirectivity(
                        title = "Экология и природопользование",
                        headId = 1,
                        gradeId = 1
                    )
                    addNewDirectivity(
                        title = "Проектирование зданий",
                        headId = 2,
                        gradeId = 1
                    )
                    addNewDirectivity(
                        title = "Промышленное и гражданское строительство",
                        headId = 2,
                        gradeId = 1
                    )
                    addNewDirectivity(
                        title = "Создание, модификация и сопровождение информационных систем, администрирование баз данных",
                        headId = 3,
                        gradeId = 1
                    )
                    addNewDirectivity(
                        title = "Менеджмент проектов в области информационных технологий, создание и поддержка информационных систем",
                        headId = 4,
                        gradeId = 1
                    )
                    addNewDirectivity(
                        title = "Электроснабжение",
                        headId = 5,
                        gradeId = 1
                    )
                    addNewDirectivity(
                        title = "Производство продуктов питания из растительного сырья",
                        headId = 6,
                        gradeId = 1
                    )
                    addNewDirectivity(
                        title = "Инженерные системы сельскохозяйственного снабжения, обводнения и водоотделения",
                        headId = 7,
                        gradeId = 1
                    )
                    addNewDirectivity(
                        title = "Мелиорация, рекультивация и охрана земель",
                        headId = 7,
                        gradeId = 1
                    )
                    addNewDirectivity(
                        title = "Землеустройство и кадастры",
                        headId = 8,
                        gradeId = 1
                    )
                    addNewDirectivity(
                        title = "Почвенно-агрохимическое обеспечение АПК",
                        headId = 9,
                        gradeId = 1
                    )
                    addNewDirectivity(
                        title = "Защита растений",
                        headId = 10,
                        gradeId = 1
                    )
                    addNewDirectivity(
                        title = "Селекция и генетика сельскохозяйственных культур",
                        headId = 10,
                        gradeId = 1
                    )
                    addNewDirectivity(
                        title = "Декоративное садоводство, плодоовощеводство, виноградство и виноделие",
                        headId = 11,
                        gradeId = 1
                    )
                    addNewDirectivity(
                        title = "Технические системы в агробизнесе",
                        headId = 12,
                        gradeId = 1
                    )
                    addNewDirectivity(
                        title = "Электрооборудование и электротехнологии",
                        headId = 12,
                        gradeId = 1
                    )
                    addNewDirectivity(
                        title = "Технология хранения и переработки сельскозяйственной продукции",
                        headId = 13,
                        gradeId = 1
                    )
                    addNewDirectivity(
                        title = "Ветеринарно-санитарная экспертиза",
                        headId = 14,
                        gradeId = 1
                    )
                    addNewDirectivity(
                        title = "Технология производства продуктов животноводства",
                        headId = 15,
                        gradeId = 1
                    )
                    addNewDirectivity(
                        title = "Бизнес-аналитика",
                        headId = 16,
                        gradeId = 1
                    )
                    addNewDirectivity(
                        title = "Бухгалтерский учёт, анализ и аудит",
                        headId = 16,
                        gradeId = 1
                    )
                    addNewDirectivity(
                        title = "Инновационный менеджмент",
                        headId = 17,
                        gradeId = 1
                    )
                    addNewDirectivity(
                        title = "Производственный менеджмент",
                        headId = 17,
                        gradeId = 1
                    )
                    addNewDirectivity(
                        title = "Государственное и муниципальное управление",
                        headId = 18,
                        gradeId = 1
                    )
                    addNewDirectivity(
                        title = "Анализ, моделирование и формирование интегрального представления стратегий и целей, бизнес-процессов и информационно-логической инфраструктуры предприятий и организаций",
                        headId = 19,
                        gradeId = 1
                    )
                    addNewDirectivity(
                        title = "Гражданско-правовой",
                        headId = 20,
                        gradeId = 1
                    )
                    addNewDirectivity(
                        title = "Уголовно-правовой",
                        headId = 20,
                        gradeId = 1
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
            "Яценко Никита Алексеевич" +
            "Крюков Егор Богданович\n" +
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
            "Гончарова Мария Лукинична\n" +
            "Лаврова Софья Владиславовна\n" +
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
                        names.shuffled().take(25).map { it.split(" ") }.forEach { (lastName, firstName, middleName) ->
                            addNewStudent(
                                firstName = firstName,
                                lastName = lastName,
                                middleName = middleName,
                                groupId = groupId,
                                statusId = statusIds.random()
                            )
                        }
                    }
                }

                println("Dummy students created. Took ${time}ms")
            }
        }
    }
}

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
                    addNewWorkType("Практика", false)
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
    val employeesDao by inject<EmployeesDao>()

    runBlocking {
        val works = worksDao.allWorks()

        if (works.isEmpty()) {
            println("Creating dummy works...")

            val time = measureTimeMillis {
                val workTypeIds = workTypesDao.allWorkTypes().map(WorkType::id)
                val disciplineIds = disciplinesDao.allDisciplines().map(Discipline::id)
                val studentIds = studentsDao.allStudents().map(Student::id)
                val employeeIds = employeesDao.allTeachers().map(Employee::id)

                repeat(100) { index ->
                    worksDao.addNewWork(
                        disciplineId = disciplineIds.random(),
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
                programIds.forEach { programId ->
                    disciplineIds.shuffled().take(9).forEach { disciplineId ->
                        programsDisciplinesDao.addNewReference(
                            programId = programId,
                            disciplineId = disciplineId,
                            workTypeId = workTypeIds.random()
                        )
                    }
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
