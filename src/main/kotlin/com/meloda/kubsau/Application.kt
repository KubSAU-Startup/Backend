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
import com.meloda.kubsau.database.programsdisciplines.ProgramsDisciplinesDao
import com.meloda.kubsau.database.specializations.SpecializationsDao
import com.meloda.kubsau.database.specializationsdisciplines.SpecializationsDisciplinesDao
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
    // TODO: 10/04/2024, Danil Nikolaev: import data from json

    createDummyUsers()

    createDummyMajors()
    createDummyGroups()
    createDummyStudents()
    createDummyWorkTypes()
    createDummySpecializations()
    createDummyPrograms()
    createDummyDepartments()
    createDummyTeachers()


    createDummyDisciplines()
    createDummyProgramsDisciplines()
    createDummySpecializationsDisciplines()
    createDummyTeachersDisciplines()
    createDummyJournalWorks()
    createDummyJournalEntries()
}

private fun Application.createDummyWorkTypes() {
    val workTypesDao by inject<WorkTypesDao>()

    workTypesDao.apply {
        runBlocking {
            if (allWorkTypes().size < 3) {
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
            if (allDepartments().size < titles.size) {
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
                    addNewUser(login = "email@domain.com", password = "123456", type = 2, departmentId = 4)
                }

                println("Dummy users created. Took ${time}ms")
            }
        }
    }
}

private val studentNames =
    ("Абросимов Ярослав Валерьевич\n" +
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

private fun Application.createDummyStudents() {
    val groupsDao by inject<GroupsDao>()
    val studentsDao by inject<StudentsDao>()

    studentsDao.apply {
        runBlocking {
            val groups = groupsDao.allGroups().map(Group::id)

            if (allStudents().size < groups.size * 25) {
                println("Creating dummy students...")

                val time = measureTimeMillis {
                    groups.forEach { groupId ->
                        repeat(25) {
                            val nameSplit = studentNames.random().split(" ")

                            addNewStudent(
                                firstName = nameSplit[1],
                                lastName = nameSplit[0],
                                middleName = nameSplit[2],
                                groupId = groupId,
                                status = 1
                            )
                        }
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
                        val nameSplit = studentNames.random().split(" ")

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
    val workTypesDao by inject<WorkTypesDao>()

    disciplinesDao.apply {
        runBlocking {
            if (allDisciplines().size < disciplinesString.size) {
                println("Creating dummy disciplines...")

                val time = measureTimeMillis {
                    val workTypes = workTypesDao.allWorkTypes().map(WorkType::id)

                    disciplinesString.forEach { title ->
                        addNewDiscipline(
                            title = title,
                            workTypeId = workTypes.random()
                        )
                    }
                }

                println("Dummy disciplines created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyProgramsDisciplines() {
    val programsDisciplinesDao by inject<ProgramsDisciplinesDao>()
    val programsDao by inject<ProgramsDao>()
    val disciplinesDao by inject<DisciplinesDao>()

    runBlocking {
        if (programsDisciplinesDao.allItems().size < 100) {
            println("Creating dummy programs-disciplines references...")

            val time = measureTimeMillis {
                val programIds = programsDao.allPrograms().map(Program::id)
                val disciplineIds = disciplinesDao.allDisciplines().map(Discipline::id)

                repeat(100) {
                    programsDisciplinesDao.addNewReference(
                        programId = programIds.random(),
                        disciplineId = disciplineIds.random()
                    )
                }
            }

            println("Dummy programs-disciplines references created. Took ${time}ms")
        }
    }
}

private fun Application.createDummySpecializationsDisciplines() {
    val specializationsDisciplinesDao by inject<SpecializationsDisciplinesDao>()
    val specializationsDao by inject<SpecializationsDao>()
    val disciplinesDao by inject<DisciplinesDao>()

    runBlocking {
        if (specializationsDisciplinesDao.allItems().size < 100) {
            println("Creating dummy specializations-disciplines references...")

            val time = measureTimeMillis {
                val specializationIds = specializationsDao.allSpecializations().map(Specialization::id)
                val disciplineIds = disciplinesDao.allDisciplines().map(Discipline::id)

                repeat(100) {
                    specializationsDisciplinesDao.addNewReference(
                        specializationId = specializationIds.random(),
                        disciplineId = disciplineIds.random()
                    )
                }
            }

            println("Dummy specializations-disciplines references created. Took ${time}ms")
        }
    }
}

private fun Application.createDummyTeachersDisciplines() {
    val teachersDisciplinesDao by inject<TeachersDisciplinesDao>()
    val teachersDao by inject<TeachersDao>()
    val disciplinesDao by inject<DisciplinesDao>()

    runBlocking {
        if (teachersDisciplinesDao.allItems().size < 100) {
            println("Creating dummy teachers-disciplines references...")

            val time = measureTimeMillis {
                val teacherIds = teachersDao.allTeachers().map(Teacher::id)
                val disciplineIds = disciplinesDao.allDisciplines().map(Discipline::id)

                repeat(100) {
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

private fun Application.createDummySpecializations() {
    val specializationsDao by inject<SpecializationsDao>()

    specializationsDao.apply {
        runBlocking {
            if (allSpecializations().size < 20) {
                println("Creating dummy specializations...")

                val time = measureTimeMillis {
                    addNewSpecialization("Экология и природопользование")
                    addNewSpecialization("Проектирование зданий")
                    addNewSpecialization("Промышленное и гражданское строительство")
                    addNewSpecialization("Создание, модификация и сопровождение информационных систем, администрирование баз данных")
                    addNewSpecialization("Менеджмент проектов в области информационных технологий, создание и поддержка информационных систем")
                    addNewSpecialization("Электроснабжение")
                    addNewSpecialization("Производство продуктов питания из растительного сырья")
                    addNewSpecialization("Инженерные системы сельскохозяйственного снабжения, обводнения и водоотделения")
                    addNewSpecialization("Землеустройство и кадастры")
                    addNewSpecialization("Почвенно-агрохимическое обеспечение АПК")
                    addNewSpecialization("Защита растений")
                    addNewSpecialization("Декоративное садоводство, плодоовощеводство, виноградство и виноделие")
                    addNewSpecialization("Технические системы в агробизнесе")
                    addNewSpecialization("Ветеринарно-санитарная экспертиза")
                    addNewSpecialization("Технология производства продуктов животноводства")
                    addNewSpecialization("Бизнес-аналитика")
                    addNewSpecialization("Инновационный менеджмент")
                    addNewSpecialization("Государственное и муниципальное управление")
                    addNewSpecialization("Анализ, моделирование и формирование интегрального представления стратегий и целей, бизнес-процессов и информационно-логической инфраструктуры предприятий и организаций")
                    addNewSpecialization("Гражданско-правовой")
                }

                println("Dummy specializations created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyPrograms() {
    val specializationsDao by inject<SpecializationsDao>()
    val programsDao by inject<ProgramsDao>()

    programsDao.apply {
        runBlocking {
            val specializations = specializationsDao.allSpecializations()

            if (allPrograms().size < specializations.size * 12) {
                println("Creating dummy programs...")

                val time = measureTimeMillis {

                    specializations.forEach { specialization ->
                        val semester = Random.nextInt(from = 1, until = 13)

                        repeat(12) {
                            addNewProgram(
                                title = "$semester ${specialization.title}",
                                semester = semester
                            )
                        }
                    }
                }

                println("Dummy programs created. Took ${time}ms")
            }
        }
    }
}

private fun Application.createDummyGroups() {
    val groupsDao by inject<GroupsDao>()
    val majorsDao by inject<MajorsDao>()

    groupsDao.apply {
        runBlocking {
            val majorTuples = majorsDao.allMajors().map {
                it.id to it.abbreviation
            }

            if (allGroups().size < majorTuples.size * 3) {
                println("Creating dummy groups...")

                val time = measureTimeMillis {
                    majorTuples.forEach { tuple ->
                        repeat(3) {
                            addNewGroup(
                                title = "${tuple.second}${Random.nextInt(from = 2001, until = 2004)}",
                                majorId = tuple.first
                            )
                        }
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
