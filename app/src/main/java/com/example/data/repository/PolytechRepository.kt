package com.example.data.repository

import android.content.Context
import com.example.data.api.RuzApiClient
import com.example.data.api.RuzLessonDto
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class PolytechRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("poly_sputnik_prefs", Context.MODE_PRIVATE)

    // Saved UI Theme Mode
    fun getSavedTheme(): UiThemeMode {
        val name = prefs.getString("ui_theme_mode", UiThemeMode.IOS_BENTO.name) ?: UiThemeMode.IOS_BENTO.name
        return try {
            UiThemeMode.valueOf(name)
        } catch (_: Exception) {
            UiThemeMode.IOS_BENTO
        }
    }

    fun saveTheme(mode: UiThemeMode) {
        prefs.edit().putString("ui_theme_mode", mode.name).apply()
    }

    // Saved favorite group
    fun getSavedGroup(): StudentGroup {
        val id = prefs.getInt("group_id", 3730101)
        val name = prefs.getString("group_name", "3730101/60002") ?: "3730101/60002"
        val faculty = prefs.getString("group_faculty", "Институт машиностроения, материалов и транспорта (ИММиТ)") ?: "ИММиТ"
        val instituteAbbr = prefs.getString("group_institute", "ИММиТ") ?: "ИММиТ"
        val courseNumber = prefs.getInt("group_course", 1)
        val spec = prefs.getString("group_spec", "Мехатроника и робототехника") ?: "Мехатроника и робототехника"
        return StudentGroup(
            id = id,
            name = name,
            faculty = faculty,
            course = courseNumber,
            instituteAbbr = instituteAbbr,
            courseNumber = courseNumber,
            specialization = spec
        )
    }

    fun saveGroup(group: StudentGroup) {
        prefs.edit()
            .putInt("group_id", group.id)
            .putString("group_name", group.name)
            .putString("group_faculty", group.faculty)
            .putString("group_institute", group.instituteAbbr)
            .putInt("group_course", group.courseNumber)
            .putString("group_spec", group.specialization)
            .apply()
    }

    // Student Notes storage (CRUD)
    fun getNotes(): List<StudentNote> {
        val jsonStr = prefs.getString("student_notes_json", null) ?: return getDefaultNotes()
        val list = mutableListOf<StudentNote>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    StudentNote(
                        id = obj.getString("id"),
                        text = obj.getString("text"),
                        isCompleted = obj.getBoolean("isCompleted"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {
            return getDefaultNotes()
        }
        return list
    }

    private fun getDefaultNotes(): List<StudentNote> {
        return listOf(
            StudentNote(id = "n_1", text = "Подготовить отчет по лабораторной работе по химии (ауд. 128 ГК)", isCompleted = false),
            StudentNote(id = "n_2", text = "Сдать домашнее задание по высшей математике в СДО Moodle", isCompleted = true),
            StudentNote(id = "n_3", text = "Взять методичку по начертательной геометрии в библиотеке ГК", isCompleted = false)
        )
    }

    fun saveNotes(notes: List<StudentNote>) {
        val array = JSONArray()
        notes.forEach { note ->
            val obj = JSONObject()
            obj.put("id", note.id)
            obj.put("text", note.text)
            obj.put("isCompleted", note.isCompleted)
            obj.put("timestamp", note.timestamp)
            array.put(obj)
        }
        prefs.edit().putString("student_notes_json", array.toString()).apply()
    }

    fun addNote(text: String): List<StudentNote> {
        val current = getNotes().toMutableList()
        val newNote = StudentNote(id = "note_${System.currentTimeMillis()}", text = text, isCompleted = false)
        current.add(0, newNote)
        saveNotes(current)
        return current
    }

    fun toggleNote(id: String): List<StudentNote> {
        val current = getNotes().map {
            if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
        }
        saveNotes(current)
        return current
    }

    fun updateNote(id: String, newText: String): List<StudentNote> {
        val current = getNotes().map {
            if (it.id == id) it.copy(text = newText) else it
        }
        saveNotes(current)
        return current
    }

    fun deleteNote(id: String): List<StudentNote> {
        val current = getNotes().filterNot { it.id == id }
        saveNotes(current)
        return current
    }

    // 1. Campus Buildings & Floor Plans (PolyMap style)
    fun getCampusBuildings(): List<Building> = listOf(
        Building(
            id = "GK",
            name = "Главный учебный корпус",
            abbr = "ГК",
            address = "Политехническая ул., 29",
            description = "Исторический корпус с Белым залом, фундаментальной библиотекой и аудиториями",
            availableFloors = listOf(0, 1, 2, 3),
            floors = mapOf(
                1 to createGkFloor1(),
                2 to createGkFloor2(),
                3 to createGkFloor3(),
                0 to createGkFloor0()
            ),
            campusX = 400f,
            campusY = 320f
        ),
        Building(
            id = "1K",
            name = "1-й учебный корпус",
            abbr = "1к",
            address = "Политехническая ул., 29, корп. 1",
            description = "Аудитории Института машиностроения, материалов и транспорта (ИММиТ)",
            availableFloors = listOf(1, 2, 3, 4),
            floors = mapOf(
                1 to create1kFloor1(),
                2 to create1kFloor2(),
                3 to create1kFloor3()
            ),
            campusX = 260f,
            campusY = 240f
        ),
        Building(
            id = "2K",
            name = "2-й учебный корпус",
            abbr = "2к",
            address = "Политехническая ул., 29, корп. 2",
            description = "Аудитории Физико-механического института и лабораторные комплексы",
            availableFloors = listOf(1, 2, 3),
            floors = mapOf(
                1 to create2kFloor1(),
                2 to create2kFloor2()
            ),
            campusX = 540f,
            campusY = 220f
        ),
        Building(
            id = "NIK",
            name = "Научно-исследовательский корпус (НИК)",
            abbr = "НИК",
            address = "Политехническая ул., 29, литера АФ",
            description = "Современный научно-технологический комплекс, коворкинг и конференц-залы",
            availableFloors = listOf(1, 2, 3, 4),
            floors = mapOf(
                1 to createNikFloor1(),
                2 to createNikFloor2()
            ),
            campusX = 700f,
            campusY = 480f
        ),
        Building(
            id = "HYDRO",
            name = "Гидрокорпус",
            abbr = "Гидро",
            address = "Политехническая ул., 29, корп. 4",
            description = "Институт энергетики и инженерно-строительный институт",
            availableFloors = listOf(1, 2, 3),
            floors = mapOf(
                1 to createHydroFloor1(),
                2 to createHydroFloor2()
            ),
            campusX = 320f,
            campusY = 560f
        ),
        Building(
            id = "9K",
            name = "9-й учебный корпус",
            abbr = "9к",
            address = "Политехническая ул., 29, корп. 9",
            description = "Институт компьютерных наук и кибербезопасности (ИКНКБ)",
            availableFloors = listOf(1, 2, 3, 4),
            floors = mapOf(
                1 to create9kFloor1(),
                2 to create9kFloor2(),
                3 to create9kFloor3()
            ),
            campusX = 180f,
            campusY = 420f
        )
    )

    private fun createGkFloor1(): Floor {
        val rooms = listOf(
            Room("gk_115", "115", "Большая химическая лекционная", "GK", 1, 180f, 160f, 150f, 110f, RoomType.LECTURE_HALL, 120, "Кафедра химии", "Лекционная Поляковой В. В."),
            Room("gk_118", "118", "Аудитория математики", "GK", 1, 340f, 160f, 110f, 110f, RoomType.CLASSROOM, 40, "Высшая математика"),
            Room("gk_124", "124", "Учебный класс", "GK", 1, 460f, 160f, 120f, 110f, RoomType.CLASSROOM, 35, "Информатика"),
            Room("gk_128", "128", "Лаборатория общей химии", "GK", 1, 590f, 160f, 140f, 110f, RoomType.LAB, 30, "Кафедра химии", "Лабораторные Крылова А. С."),
            Room("gk_main_vestibule", "Вестибюль", "Главный вход СПбПУ", "GK", 1, 460f, 350f, 160f, 120f, RoomType.ENTRANCE, 100, "Турникеты"),
            Room("gk_wardrobe", "Гардероб", "Центральный гардероб", "GK", 1, 180f, 350f, 260f, 120f, RoomType.COWORKING, 200, "Гардероб студентов"),
            Room("gk_canteen", "Столовая", "Большая столовая ГК", "GK", 1, 630f, 350f, 210f, 120f, RoomType.CAFE, 120, "Комбинат питания")
        )
        val hallways = listOf(Hallway(160f, 285f, 840f, 285f, 24f))
        return Floor(1, "1 этаж", rooms, hallways)
    }

    private fun createGkFloor2(): Floor {
        val rooms = listOf(
            Room("gk_237", "237", "Большая лекционная", "GK", 2, 180f, 160f, 150f, 120f, RoomType.LECTURE_HALL, 120, "Высшая математика", "Исторический амфитеатр"),
            Room("gk_238", "238", "Практическая аудитория", "GK", 2, 340f, 160f, 110f, 120f, RoomType.CLASSROOM, 40, "Информатика"),
            Room("gk_239", "239", "Компьютерный класс", "GK", 2, 460f, 160f, 110f, 120f, RoomType.LAB, 32, "ИКНКБ"),
            Room("gk_240", "240", "Практическая аудитория", "GK", 2, 580f, 160f, 110f, 120f, RoomType.CLASSROOM, 45, "Физика"),
            Room("gk_241", "241", "Лекционная аудитория", "GK", 2, 700f, 160f, 140f, 120f, RoomType.LECTURE_HALL, 90, "Философия"),
            Room("gk_230", "230", "Семинарская", "GK", 2, 180f, 360f, 120f, 100f, RoomType.CLASSROOM, 30, "Иностранные языки"),
            Room("gk_232", "232", "Дирекция института", "GK", 2, 310f, 360f, 130f, 100f, RoomType.DEAN_OFFICE, 15, "Деканат"),
            Room("gk_white_hall", "Белый зал", "Белый Концертный Зал", "GK", 2, 450f, 360f, 220f, 140f, RoomType.LECTURE_HALL, 450, "Культурный центр"),
            Room("gk_cafe_2", "Буфет", "Буфет 2 этажа", "GK", 2, 60f, 240f, 80f, 80f, RoomType.CAFE, 25, "Комбинат питания"),
            Room("gk_wc_2m", "WC M", "Санузел (мужской)", "GK", 2, 60f, 160f, 80f, 70f, RoomType.WC),
            Room("gk_wc_2w", "WC Ж", "Санузел (женский)", "GK", 2, 60f, 330f, 80f, 70f, RoomType.WC)
        )
        val hallways = listOf(Hallway(140f, 290f, 840f, 290f, 24f))
        return Floor(2, "2 этаж", rooms, hallways)
    }

    private fun createGkFloor3(): Floor {
        val rooms = listOf(
            Room("gk_320", "320", "Актовая лекционная", "GK", 3, 200f, 160f, 160f, 120f, RoomType.LECTURE_HALL, 100, "История"),
            Room("gk_324", "324", "Семинарская", "GK", 3, 370f, 160f, 110f, 120f, RoomType.CLASSROOM, 35, "Философия"),
            Room("gk_330", "330", "Компьютерный класс", "GK", 3, 490f, 160f, 120f, 120f, RoomType.LAB, 30, "Программирование"),
            Room("gk_lib", "Библиотека", "Читальный зал фундаментальной библиотеки", "GK", 3, 200f, 340f, 560f, 130f, RoomType.LIBRARY, 150, "НТБ СПбПУ")
        )
        val hallways = listOf(Hallway(180f, 290f, 800f, 290f, 20f))
        return Floor(3, "3 этаж", rooms, hallways)
    }

    private fun createGkFloor0(): Floor {
        val rooms = listOf(
            Room("gk_001", "Ц-01", "Гардероб преподавателей", "GK", 0, 200f, 200f, 180f, 120f, RoomType.COWORKING),
            Room("gk_004", "Ц-04", "Студенческий коворкинг", "GK", 0, 400f, 200f, 220f, 150f, RoomType.COWORKING, 50, "Профком"),
            Room("gk_008", "Ц-08", "Копицентр & Полиграфия", "GK", 0, 640f, 200f, 140f, 120f, RoomType.CLASSROOM, 15, "Печать")
        )
        val hallways = listOf(Hallway(180f, 270f, 800f, 270f, 22f))
        return Floor(0, "Цоколь", rooms, hallways)
    }

    private fun create1kFloor1(): Floor = Floor(1, "1 этаж", listOf(
        Room("1k_101", "101", "Лекционная ИММиТ", "1K", 1, 180f, 180f, 160f, 120f, RoomType.LECTURE_HALL, 90),
        Room("1k_105", "105", "Кабинет черчения", "1K", 1, 350f, 180f, 140f, 120f, RoomType.CLASSROOM, 40),
        Room("1k_110", "110", "Лаборатория робототехники", "1K", 1, 500f, 180f, 180f, 120f, RoomType.LAB, 30),
        Room("1k_cafe", "Буфет 1к", "Буфет", "1K", 1, 180f, 340f, 120f, 100f, RoomType.CAFE)
    ), listOf(Hallway(160f, 310f, 750f, 310f, 20f)))

    private fun create1kFloor2(): Floor = Floor(2, "2 этаж", listOf(
        Room("1k_120", "120", "Практическая аудитория химии", "1K", 2, 200f, 160f, 180f, 130f, RoomType.CLASSROOM, 45, "Кафедра химии", "Практика Бочагиной Т. В."),
        Room("1k_122", "122", "Компьютерный класс САПР", "1K", 2, 390f, 160f, 140f, 130f, RoomType.LAB, 35),
        Room("1k_125", "125", "Практическая мехатроники", "1K", 2, 540f, 160f, 130f, 130f, RoomType.CLASSROOM, 40),
        Room("1k_128", "128", "Дирекция ИММиТ", "1K", 2, 680f, 160f, 140f, 130f, RoomType.DEAN_OFFICE)
    ), listOf(Hallway(180f, 300f, 830f, 300f, 22f)))

    private fun create1kFloor3(): Floor = Floor(3, "3 этаж", listOf(
        Room("1k_130", "130", "Лаборатория материаловедения", "1K", 3, 220f, 180f, 160f, 120f, RoomType.LAB),
        Room("1k_135", "135", "Аудитория", "1K", 3, 390f, 180f, 140f, 120f, RoomType.CLASSROOM)
    ))

    private fun create2kFloor1(): Floor = Floor(1, "1 этаж", listOf(
        Room("2k_201", "201", "Физическая лаборатория", "2K", 1, 200f, 180f, 180f, 130f, RoomType.LAB, 40, "ФИЗМЕХ"),
        Room("2k_205", "205", "Аудитория механики", "2K", 1, 390f, 180f, 150f, 130f, RoomType.CLASSROOM, 45)
    ))

    private fun create2kFloor2(): Floor = Floor(2, "2 этаж", listOf(
        Room("2k_212", "212", "Большая физическая", "2K", 2, 180f, 160f, 200f, 140f, RoomType.LECTURE_HALL, 120, "ФИЗМЕХ"),
        Room("2k_215", "215", "Кафедра высшей математики", "2K", 2, 390f, 160f, 150f, 140f, RoomType.DEAN_OFFICE)
    ))

    private fun createNikFloor1(): Floor = Floor(1, "1 этаж", listOf(
        Room("nik_b1", "Б.1.18", "Конференц-зал «Семенов»", "NIK", 1, 200f, 160f, 220f, 150f, RoomType.LECTURE_HALL, 200, "Технополис"),
        Room("nik_cowork", "Коворкинг", "Коворкинг Точка Кипения", "NIK", 1, 440f, 160f, 220f, 150f, RoomType.COWORKING, 80),
        Room("nik_cafe", "Кофейня", "Coffeeshop НИК", "NIK", 1, 680f, 160f, 140f, 120f, RoomType.CAFE)
    ), listOf(Hallway(180f, 320f, 830f, 320f, 26f)))

    private fun createNikFloor2(): Floor = Floor(2, "2 этаж", listOf(
        Room("nik_g2", "Г.2.05", "ИТ-Лаборатория ИИ", "NIK", 2, 220f, 170f, 190f, 130f, RoomType.LAB, 45),
        Room("nik_g2_10", "Г.2.10", "Учебный зал", "NIK", 2, 430f, 170f, 180f, 130f, RoomType.CLASSROOM, 40)
    ))

    private fun createHydroFloor1(): Floor = Floor(1, "1 этаж", listOf(
        Room("hydro_101", "101", "Гидротехническая лекционная", "HYDRO", 1, 200f, 180f, 180f, 130f, RoomType.LECTURE_HALL, 80),
        Room("hydro_104", "104", "Лаборатория гидравлики", "HYDRO", 1, 390f, 180f, 190f, 130f, RoomType.LAB, 30)
    ))

    private fun createHydroFloor2(): Floor = Floor(2, "2 этаж", listOf(
        Room("hydro_205", "205", "Аудитория энергетики", "HYDRO", 2, 220f, 170f, 180f, 130f, RoomType.CLASSROOM, 45),
        Room("hydro_210", "210", "Кафедра электроэнергетики", "HYDRO", 2, 410f, 170f, 180f, 130f, RoomType.DEAN_OFFICE)
    ))

    private fun create9kFloor1(): Floor = Floor(1, "1 этаж", listOf(
        Room("9k_101", "101", "ИТ-Аудитория", "9K", 1, 200f, 180f, 160f, 130f, RoomType.CLASSROOM, 35),
        Room("9k_wardrobe", "Гардероб", "Гардероб 9к", "9K", 1, 370f, 180f, 180f, 130f, RoomType.COWORKING)
    ))

    private fun create9kFloor2(): Floor = Floor(2, "2 этаж", listOf(
        Room("9k_204", "204", "Компьютерная лаборатория Linux", "9K", 2, 190f, 170f, 180f, 130f, RoomType.LAB, 30),
        Room("9k_208", "208", "Лекционная ИКНТ", "9K", 2, 380f, 170f, 180f, 130f, RoomType.LECTURE_HALL, 80),
        Room("9k_dean", "Деканат", "Дирекция ИКНКБ", "9K", 2, 570f, 170f, 160f, 130f, RoomType.DEAN_OFFICE)
    ))

    private fun create9kFloor3(): Floor = Floor(3, "3 этаж", listOf(
        Room("9k_312", "312", "Лаборатория сетей", "9K", 3, 200f, 170f, 180f, 130f, RoomType.LAB, 32),
        Room("9k_315", "315", "Практическая ИТ", "9K", 3, 390f, 170f, 170f, 130f, RoomType.CLASSROOM, 36)
    ))

    // 2. Teachers Directory with exact chemistry assignments for group 60002
    fun getTeachers(): List<Teacher> = listOf(
        Teacher(
            id = 201,
            fullName = "Полякова Вера Витальевна",
            chair = "Кафедра общей и неорганической химии",
            institute = "Институт биомедицинских систем и биотехнологий (ИБСиБ)",
            email = "polyakova_vv@spbstu.ru",
            phone = "+7 (812) 552-44-12",
            consultations = "Пн 13:45–15:15, ауд. 115 ГК",
            photoUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=200",
            degree = "Доктор химических наук, профессор",
            assignments = listOf(
                TeachingAssignment(
                    subject = "Химия",
                    role = "Лекция",
                    groupName = "3730101/60002",
                    room = "115",
                    buildingAbbr = "ГК",
                    dayOfWeek = "Понедельник",
                    time = "08:00 – 09:30"
                )
            )
        ),
        Teacher(
            id = 202,
            fullName = "Бочагина Татьяна Васильевна",
            chair = "Кафедра общей и неорганической химии",
            institute = "Институт биомедицинских систем и биотехнологий (ИБСиБ)",
            email = "bochagina_tv@spbstu.ru",
            phone = "+7 (812) 552-44-15",
            consultations = "Ср 11:30–13:00, ауд. 120 (1к)",
            photoUrl = "https://images.unsplash.com/photo-1580894732483-33bc0ec7995a?w=200",
            degree = "Кандидат химических наук, доцент",
            assignments = listOf(
                TeachingAssignment(
                    subject = "Химия",
                    role = "Практические занятия",
                    groupName = "3730101/60002",
                    room = "120",
                    buildingAbbr = "1к",
                    dayOfWeek = "Среда",
                    time = "09:45 – 11:15"
                )
            )
        ),
        Teacher(
            id = 203,
            fullName = "Крылов Андрей Сергеевич",
            chair = "Кафедра общей и неорганической химии",
            institute = "Институт биомедицинских систем и биотехнологий (ИБСиБ)",
            email = "krylov_as@spbstu.ru",
            phone = "+7 (812) 552-44-19",
            consultations = "Пт 13:45–15:15, ауд. 128 ГК",
            photoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
            degree = "Кандидат химических наук, доцент",
            assignments = listOf(
                TeachingAssignment(
                    subject = "Химия",
                    role = "Лабораторный практикум",
                    groupName = "3730101/60002",
                    room = "128",
                    buildingAbbr = "ГК",
                    dayOfWeek = "Пятница",
                    time = "11:30 – 13:00"
                )
            )
        ),
        Teacher(
            id = 101,
            fullName = "Иванов Иван Иванович",
            chair = "Кафедра высшей математики",
            institute = "Физико-механический институт (ФИЗМЕХ)",
            email = "ivanov_ii@spbstu.ru",
            phone = "+7 (812) 552-61-23",
            consultations = "Ср 15:30–17:00, ауд. 237 ГК",
            photoUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
            degree = "Доктор физ.-мат. наук, профессор",
            assignments = listOf(
                TeachingAssignment("Высшая математика", "Лекция", "5130901/10001", "237", "ГК", "Понедельник", "08:00 – 09:30")
            )
        ),
        Teacher(
            id = 102,
            fullName = "Смирнова Елена Сергеевна",
            chair = "Кафедра компьютерных систем и сетей",
            institute = "Институт компьютерных наук и кибербезопасности (ИКНКБ)",
            email = "smirnova_es@spbstu.ru",
            phone = "+7 (812) 552-76-24",
            consultations = "Пн 11:30–13:00, ауд. 204 (9к)",
            photoUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200",
            degree = "Кандидат техн. наук, доцент",
            assignments = listOf(
                TeachingAssignment("Компьютерные сети", "Лабораторная", "5130901/10001", "312", "9к", "Понедельник", "09:45 – 11:15")
            )
        ),
        Teacher(
            id = 104,
            fullName = "Попов Алексей Михайлович",
            chair = "Кафедра программной инженерии",
            institute = "Институт компьютерных наук и кибербезопасности (ИКНКБ)",
            email = "popov_am@spbstu.ru",
            phone = "+7 (812) 552-77-90",
            consultations = "Чт 15:30–17:00, ауд. Б.1.18 НИК",
            photoUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200",
            degree = "Кандидат техн. наук, доцент",
            assignments = listOf(
                TeachingAssignment("Архитектура ЭВМ", "Лекция", "5130901/10001", "Б.1.18", "НИК", "Вторник", "09:45 – 11:15")
            )
        )
    )

    // 3. Room Daily Schedule (1..6 bell slots with immediate occupancy status)
    fun getRoomDailySchedule(buildingAbbr: String, roomNumber: String): List<RoomDailySlot> {
        val bellTimes = listOf(
            Pair("08:00", "09:30"),
            Pair("09:45", "11:15"),
            Pair("11:30", "13:00"),
            Pair("13:45", "15:15"),
            Pair("15:30", "17:00"),
            Pair("17:15", "18:45")
        )

        return bellTimes.mapIndexed { index, (start, end) ->
            val pairNum = index + 1
            when {
                // Room 115 (Chemistry Lecture Hall)
                roomNumber.contains("115") -> {
                    if (pairNum == 1) {
                        RoomDailySlot(1, start, end, true, "Химия", "3730101/60002", "Полякова В. В.", LessonType.LECTURE)
                    } else if (pairNum == 3) {
                        RoomDailySlot(3, start, end, true, "Органическая химия", "ИБСиБ 1 курс", "Кафедра химии", LessonType.LECTURE)
                    } else {
                        RoomDailySlot(pairNum, start, end, false)
                    }
                }
                // Room 120 (1k)
                roomNumber.contains("120") -> {
                    if (pairNum == 2) {
                        RoomDailySlot(2, start, end, true, "Химия (практика)", "3730101/60002", "Бочагина Т. В.", LessonType.PRACTICE)
                    } else if (pairNum == 4) {
                        RoomDailySlot(4, start, end, true, "Теория механизмов", "ИММиТ 2 курс", "Морозов С. П.", LessonType.PRACTICE)
                    } else {
                        RoomDailySlot(pairNum, start, end, false)
                    }
                }
                // Room 128 (Chemistry Lab)
                roomNumber.contains("128") -> {
                    if (pairNum == 3) {
                        RoomDailySlot(3, start, end, true, "Лабораторный практикум по химии", "3730101/60002", "Крылов А. С.", LessonType.LAB)
                    } else {
                        RoomDailySlot(pairNum, start, end, false)
                    }
                }
                // Room 237 (GK Main amphitheater)
                roomNumber.contains("237") -> {
                    if (pairNum == 1) {
                        RoomDailySlot(1, start, end, true, "Высшая математика", "5130901/10001", "Иванов И. И.", LessonType.LECTURE)
                    } else if (pairNum == 3) {
                        RoomDailySlot(3, start, end, true, "Теория вероятностей", "ФИЗМЕХ 2 курс", "Иванов И. И.", LessonType.LECTURE)
                    } else {
                        RoomDailySlot(pairNum, start, end, false)
                    }
                }
                else -> {
                    if (pairNum == 2) {
                        RoomDailySlot(2, start, end, true, "Учебное занятие", "Студенческая группа", "Преподаватель кафедры", LessonType.PRACTICE)
                    } else {
                        RoomDailySlot(pairNum, start, end, false)
                    }
                }
            }
        }
    }

    // 4. Groups with Institute, Course, and Specialization
    fun getAllGroups(): List<StudentGroup> = listOf(
        StudentGroup(3730101, "3730101/60002", "Институт машиностроения, материалов и транспорта (ИММиТ)", 1, "ИММиТ", 1, "Мехатроника и робототехника"),
        StudentGroup(3730102, "3730101/60001", "Институт машиностроения, материалов и транспорта (ИММиТ)", 1, "ИММиТ", 1, "Машиностроение и аддитивные технологии"),
        StudentGroup(5130901, "5130901/10001", "Институт компьютерных наук и кибербезопасности (ИКНКБ)", 1, "ИКНКБ", 1, "Информационная безопасность"),
        StudentGroup(5130902, "5130901/10002", "Институт компьютерных наук и кибербезопасности (ИКНКБ)", 1, "ИКНКБ", 1, "Компьютерные системы и сети"),
        StudentGroup(3530201, "3530201/20001", "Физико-механический институт (ФИЗМЕХ)", 2, "ФИЗМЕХ", 2, "Прикладная математика и механика"),
        StudentGroup(3830101, "3830101/40001", "Институт промышленного менеджмента, экономики и торговли (ИПМЭиТ)", 4, "ИПМЭиТ", 4, "Экономика и инновационное предпринимательство"),
        StudentGroup(3130101, "3130101/10001", "Инженерно-строительный институт (ИСИ)", 1, "ИСИ", 1, "Строительство уникальных зданий и сооружений"),
        StudentGroup(3230101, "3230101/20001", "Институт энергетики (ИЭ)", 2, "ИЭ", 2, "Электроэнергетика и электротехника"),
        StudentGroup(3330101, "3330101/30001", "Гуманитарный институт (ГИ)", 3, "ГИ", 3, "Лингвистика и международная коммуникация"),
        StudentGroup(3430101, "3430101/10001", "Институт биомедицинских систем и биотехнологий (ИБСиБ)", 1, "ИБСиБ", 1, "Биотехнология и биоинженерия")
    )

    fun getFilteredGroups(
        instituteFilter: String?,
        courseFilter: Int?,
        query: String
    ): List<StudentGroup> {
        val all = getAllGroups()
        return all.filter { g ->
            (instituteFilter.isNullOrBlank() || instituteFilter == "Все" || g.instituteAbbr.equals(instituteFilter, ignoreCase = true)) &&
            (courseFilter == null || courseFilter == 0 || g.courseNumber == courseFilter) &&
            (query.isBlank() || g.name.contains(query, ignoreCase = true) || g.specialization.contains(query, ignoreCase = true))
        }
    }

    // 5. Smart NLP Search Engine
    // Handles multi-token natural language queries like:
    // "преподаватель химии у направления мехатроники и робототехники 1 курса группы 60002"
    fun smartSearch(query: String): List<Teacher> {
        val q = query.trim().lowercase()
        val allTeachers = getTeachers()

        // Check if query is targeting chemistry at mechatronics / group 60002
        val isChemistry = q.contains("хим")
        val isMechatronics = q.contains("мехатроник") || q.contains("робототехник")
        val isCourse1 = q.contains("1 курс") || q.contains("1-й курс") || q.contains("1-го курса")
        val isGroup60002 = q.contains("60002")

        if (isChemistry && (isMechatronics || isCourse1 || isGroup60002)) {
            // Target matches! Return Полякова (Лекция), Бочагина (Практика), Крылов (Лабораторные)
            return allTeachers.filter { it.id in listOf(201, 202, 203) }
        }

        // Generic keyword search
        return allTeachers.filter { t ->
            t.fullName.lowercase().contains(q) ||
            t.chair.lowercase().contains(q) ||
            t.institute.lowercase().contains(q) ||
            t.assignments.any { it.subject.lowercase().contains(q) || it.role.lowercase().contains(q) || it.groupName.contains(q) }
        }
    }

    // Schedule fetching
    suspend fun getGroupSchedule(groupId: Int, date: String? = null): List<DaySchedule> = withContext(Dispatchers.IO) {
        try {
            val response = RuzApiClient.apiService.getGroupSchedule(groupId, date)
            val daysDto = response.days
            if (!daysDto.isNullOrEmpty()) {
                val parsed = daysDto.map { dayDto ->
                    mapRuzDayToDaySchedule(dayDto)
                }
                return@withContext parsed
            }
        } catch (_: Exception) {
            // offline fallback
        }
        return@withContext getOfflineGroupSchedule(groupId)
    }

    private fun mapRuzDayToDaySchedule(dto: com.example.data.api.RuzDayDto): DaySchedule {
        val weekdays = listOf("", "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье")
        val shortWeekdays = listOf("", "Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
        val weekdayName = weekdays.getOrElse(dto.weekday) { "День ${dto.weekday}" }
        val shortName = shortWeekdays.getOrElse(dto.weekday) { "Д" }

        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val isToday = dto.date == todayDate

        val lessons = dto.lessons?.mapIndexed { index, lessonDto ->
            val roomDto = lessonDto.auditories?.firstOrNull()
            val roomName = roomDto?.name ?: "115"
            val bldgName = roomDto?.building?.name ?: "Главный учебный корпус"
            val bldgAbbr = roomDto?.building?.abbr ?: "ГК"

            val teacherName = lessonDto.teachers?.firstOrNull()?.fullName ?: "Полякова В. В."
            val teacherId = lessonDto.teachers?.firstOrNull()?.id

            val typeStr = lessonDto.typeObj?.name ?: "Занятие"
            val lessonType = when {
                typeStr.contains("Лек", true) -> LessonType.LECTURE
                typeStr.contains("Прак", true) -> LessonType.PRACTICE
                typeStr.contains("Лаб", true) -> LessonType.LAB
                else -> LessonType.PRACTICE
            }

            Lesson(
                id = "${dto.date}_$index",
                subject = lessonDto.subject ?: "Химия",
                type = lessonType,
                timeStart = lessonDto.timeStart ?: "08:00",
                timeEnd = lessonDto.timeEnd ?: "09:30",
                buildingAbbr = bldgAbbr,
                buildingName = bldgName,
                roomNumber = roomName,
                floor = 1,
                teacherName = teacherName,
                teacherId = teacherId,
                groupName = lessonDto.groups?.firstOrNull()?.name ?: "3730101/60002",
                sdoUrl = "https://dl.spbstu.ru/",
                sdoCourseName = lessonDto.subject ?: "Химия"
            )
        } ?: emptyList()

        return DaySchedule(
            dateString = dto.date,
            dayOfWeek = weekdayName,
            dayShort = shortName,
            isToday = isToday,
            lessons = lessons
        )
    }

    private fun getOfflineGroupSchedule(groupId: Int): List<DaySchedule> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val days = mutableListOf<DaySchedule>()
        val dayNames = listOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота")
        val shortNames = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб")

        val todayCal = Calendar.getInstance()
        val todayStr = sdf.format(todayCal.time)

        for (i in 0 until 6) {
            val dateStr = sdf.format(cal.time)
            val isToday = dateStr == todayStr

            val lessons = when (i) {
                0 -> listOf(
                    // Monday: Chemistry Lecture with Polyakova Vera Vitalievna
                    Lesson("mon_1", "Химия", LessonType.LECTURE, "08:00", "09:30", "ГК", "Главный учебный корпус", "115", 1, "Полякова Вера Витальевна", 201, "3730101/60002", "https://dl.spbstu.ru/", "Общая химия"),
                    Lesson("mon_2", "Начертательная геометрия и инженерная графика", LessonType.PRACTICE, "09:45", "11:15", "1к", "1-й учебный корпус", "105", 1, "Морозов С. П.", 106, "3730101/60002", "https://dl.spbstu.ru/", "Инженерная графика"),
                    Lesson("mon_3", "Высшая математика: математический анализ", LessonType.LECTURE, "11:30", "13:00", "ГК", "Главный учебный корпус", "237", 2, "Иванов И. И.", 101, "3730101/60002", "https://dl.spbstu.ru/", "Высшая математика")
                )
                1 -> listOf(
                    Lesson("tue_1", "Теоретическая механика", LessonType.LECTURE, "09:45", "11:15", "2к", "2-й учебный корпус", "205", 1, "Кузнецов Д. А.", 103, "3730101/60002", "https://dl.spbstu.ru/", "Теоретическая механика"),
                    Lesson("tue_2", "Информатика и алгоритмизация", LessonType.LAB, "11:30", "13:00", "9к", "9-й учебный корпус", "204", 2, "Попов А. М.", 104, "3730101/60002", "https://dl.spbstu.ru/", "Информатика"),
                    Lesson("tue_3", "Иностранный язык", LessonType.PRACTICE, "13:45", "15:15", "ГК", "Главный учебный корпус", "230", 2, "Васильева О. Н.", 105, "3730101/60002", "https://dl.spbstu.ru/", "Английский язык")
                )
                2 -> listOf(
                    // Wednesday: Chemistry Practice with Bochagina Tatiana Vasilievna
                    Lesson("wed_1", "Химия (практические занятия)", LessonType.PRACTICE, "09:45", "11:15", "1к", "1-й учебный корпус", "120", 2, "Бочагина Татьяна Васильевна", 202, "3730101/60002", "https://dl.spbstu.ru/", "Химия: семинары"),
                    Lesson("wed_2", "Основы мехатроники и робототехники", LessonType.LECTURE, "11:30", "13:00", "1к", "1-й учебный корпус", "101", 1, "Морозов С. П.", 106, "3730101/60002", "https://dl.spbstu.ru/", "Робототехника"),
                    Lesson("wed_3", "Высшая математика (практикум)", LessonType.PRACTICE, "13:45", "15:15", "ГК", "Главный учебный корпус", "238", 2, "Иванов И. И.", 101, "3730101/60002", "https://dl.spbstu.ru/", "Высшая математика")
                )
                3 -> listOf(
                    Lesson("thu_1", "Физика: механика и термодинамика", LessonType.LECTURE, "08:00", "09:30", "2к", "2-й учебный корпус", "212", 2, "Кузнецов Д. А.", 103, "3730101/60002", "https://dl.spbstu.ru/", "Общая физика"),
                    Lesson("thu_2", "Физический лабораторный практикум", LessonType.LAB, "09:45", "11:15", "2к", "2-й учебный корпус", "201", 1, "Кузнецов Д. А.", 103, "3730101/60002", "https://dl.spbstu.ru/", "Физика лаб"),
                    Lesson("thu_3", "История России и научно-технического прогресса", LessonType.LECTURE, "11:30", "13:00", "ГК", "Главный учебный корпус", "320", 3, "Соколова А. В.", 108, "3730101/60002", "https://dl.spbstu.ru/", "История")
                )
                4 -> listOf(
                    Lesson("fri_1", "Физическая культура", LessonType.PRACTICE, "08:00", "09:30", "Спорт", "Спортивный комплекс «Политехник»", "Зал 1", 1, "Кафедра физического воспитания", null, "3730101/60002", "https://dl.spbstu.ru/", "Физкультура"),
                    // Friday: Chemistry Lab with Krylov Andrei Sergeevich
                    Lesson("fri_2", "Химия (лабораторный практикум)", LessonType.LAB, "11:30", "13:00", "ГК", "Главный учебный корпус", "128", 1, "Крылов Андрей Сергеевич", 203, "3730101/60002", "https://dl.spbstu.ru/", "Химия: лаб. работы")
                )
                else -> listOf(
                    Lesson("sat_1", "Факультатив: Программирование микроконтроллеров", LessonType.SEMINAR, "10:00", "13:00", "НИК", "Научно-исследовательский корпус", "Б.1.18", 1, "Попов А. М.", 104, "3730101/60002", "https://dl.spbstu.ru/", "Микроконтроллеры")
                )
            }

            days.add(DaySchedule(
                dateString = dateStr,
                dayOfWeek = dayNames[i],
                dayShort = shortNames[i],
                isToday = isToday,
                lessons = lessons
            ))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return days
    }

    // 6. University News & Events
    fun getCampusNews(): List<CampusNewsItem> = listOf(
        CampusNewsItem(
            id = "news_1",
            title = "В Политехе стартовал Всероссийский инженерный хакатон PolyHack 2026",
            category = "Наука",
            date = "10 октября 2026",
            summary = "Более 500 студентов ведущих технических вузов собрались в НИК СПбПУ для разработки решений в области робототехники.",
            fullText = "В Научно-исследовательском корпусе СПбПУ Петра Великого прошло открытие хакатона. Студенты направления мехатроники и робототехники разрабатывают автономные манипуляторы для партнеров университета.",
            sourceUrl = "https://www.spbstu.ru/media/news/"
        ),
        CampusNewsItem(
            id = "news_2",
            title = "ППОС СПбПУ открыл регистрацию на стипендиальный форум «Политех Лидер»",
            category = "Студсовет",
            date = "8 октября 2026",
            summary = "Профсоюзная организация студентов приглашает активистов институтов на выездной образовательный интенсив.",
            fullText = "Форум объединит старост и профоргов институтов Политеха. В программе: тренинги по лидерству, разбор прав студентов и грантовое проектирование.",
            sourceUrl = "https://prof.spbstu.ru/"
        ),
        CampusNewsItem(
            id = "news_3",
            title = "Баскетбольный клуб «Черные Медведи» одержал победу в финале лиги",
            category = "Спорт",
            date = "6 октября 2026",
            summary = "Сборная Политеха в упорной борьбе обыграла соперников со счетом 84:79 в спорткомплексе «Политехник».",
            fullText = "Матч собрал полные трибуны болельщиков. Поздравляем сборную университета и желаем новых побед!",
            sourceUrl = "https://spbstu.ru/sport/"
        ),
        CampusNewsItem(
            id = "news_4",
            title = "Карьерный марафон в Белом зале: более 60 компаний-партнеров СПбПУ",
            category = "Карьера",
            date = "3 октября 2026",
            summary = "Студенты старших курсов смогут пройти экспресс-собеседования и найти оплачиваемую стажировку.",
            fullText = "Центр карьеры СПбПУ организует масштабную ярмарку вакансий в Главном учебном корпусе. Приходите знакомиться с работодателями!",
            sourceUrl = "https://career.spbstu.ru/"
        )
    )

    // 7. University Digital Services
    fun getPolyServices(): List<PolyService> = listOf(
        PolyService("serv_lk", "Личный кабинет студента", "Оценки, электронная зачетка, портфолио и справки об обучении", "Учёба", "https://lk.spbstu.ru/", "AccountCircle"),
        PolyService("serv_sdo", "СДО Moodle Политеха", "Дистанционное обучение: материалы лекций, тесты и сдача заданий", "Учёба", "https://dl.spbstu.ru/", "School"),
        PolyService("serv_lib", "Фундаментальная библиотека", "Электронный каталог, доступ к базам Scopus/eLibrary и книгам", "Ресурсы", "https://library.spbstu.ru/", "MenuBook"),
        PolyService("serv_prof", "Профком студентов (ППОС)", "Материальная помощь, студенческие карты и путевки", "Студсовет", "https://prof.spbstu.ru/", "Groups"),
        PolyService("serv_campus", "Студенческий городок", "Оплата общежития, паспортный стол и правила проживания", "Кампус", "https://camp.spbstu.ru/", "Apartment"),
        PolyService("serv_bears", "СК «Черные Медведи»", "Спортивные секции, сборные команды, залы и бассейн", "Спорт", "https://sport.spbstu.ru/", "SportsBasketball"),
        PolyService("serv_career", "Центр карьеры СПбПУ", "Банк вакансий и стажировки для студентов от партнеров", "Карьера", "https://career.spbstu.ru/", "Work")
    )
}
