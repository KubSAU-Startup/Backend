package com.meloda.kubsau.route.journal

import com.meloda.kubsau.base.respondSuccess
import com.meloda.kubsau.model.Discipline
import com.meloda.kubsau.model.Group
import com.meloda.kubsau.model.Teacher
import com.meloda.kubsau.model.WorkType
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlin.random.Random

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

private val workTypesString = listOf(
    "Курсовая", "Практика"
)

private val workTypes = List(workTypesString.size) { index ->
    WorkType(id = index, title = workTypesString[index])
}

private val disciplinesString = listOf(
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

private val disciplines = List(10) { index ->
    Discipline(
        id = index,
        title = disciplinesString.random()
    )
}

private val teachers = List(10) { index ->
    Teacher(
        id = index,
        fullName = names.random()
    )
}

private val groupsString = listOf("ИТ", "ПИ", "БИ")

private val groups = List(10) { index ->
    Group(
        id = index,
        title = "${groupsString.random()}${Random.nextInt(from = 2001, until = 2006)}"
    )
}

private val students = List(10) {
    JournalStudent(
        fullName = names.random(),
        status = if (Random.nextBoolean()) 1 else 0
    )
}

private val journal = List(10) { index ->
    JournalItem(
        student = students.random(),
        group = groups.random(),
        discipline = disciplines.random(),
        teacher = teachers.random(),
        work = JournalWork(
            id = Random.nextInt(from = 0, until = 101),
            type = workTypes.random(),
            registrationDate = getRandomUnixTime(1609459200L, 1708775961L),
            title = if (Random.nextBoolean()) null else "Work title #${index + 1}"
        )
    )
}

fun Route.journal() {
    get("/journals/worktypes") {
        respondSuccess { workTypes }
    }

    get("/journals") {
        val params = call.request.queryParameters
        val workTypeId = params["workTypeId"]?.toInt()
        val disciplineId = params["disciplineId"]?.toInt()
        val teacherId = params["teacherId"]?.toInt()
        val departmentId = params["departmentId"]?.toInt()
        val groupId = params["groupId"]?.toInt()

        val filteredJournal = journal.filter { item ->
            (item.work.id == workTypeId || workTypeId == null) &&
                    (item.discipline.id == disciplineId || disciplineId == null) &&
                    (item.teacher.id == teacherId || teacherId == null) &&
                    (item.group.id == groupId || groupId == null)
        }

        respondSuccess { filteredJournal }
    }

    filters()
}

private fun Route.filters() {
    get("/journals/filters") {
        respondSuccess {
            JournalFilters(
                workTypes = workTypes,
                disciplines = disciplines,
                teachers = teachers,
                groups = groups
            )
        }
    }
}

private data class JournalFilters(
    val workTypes: List<WorkType>,
    val disciplines: List<Discipline>,
    val teachers: List<Teacher>,
    val groups: List<Group>
)

private data class JournalItem(
    val student: JournalStudent,
    val group: Group,
    val discipline: Discipline,
    val teacher: Teacher,
    val work: JournalWork
)

private data class JournalStudent(
    val fullName: String,
    val status: Int
)

private data class JournalWork(
    val id: Int,
    val type: WorkType,
    val registrationDate: Long,
    val title: String?
)


fun getRandomUnixTime(startTime: Long, endTime: Long): Long {
    require(startTime < endTime) { "Start time must be before end time" }
    val randomUnixTime = Random.nextLong(startTime, endTime)
    return randomUnixTime
}


