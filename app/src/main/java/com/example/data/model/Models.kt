package com.example.data.model

enum class UiThemeMode(val title: String, val subtitle: String, val badge: String) {
    IOS_BENTO(
        title = "Вариант А: iOS Clean Bento",
        subtitle = "Белоснежный премиальный iOS-стиль с Bento-карточками, скруглениями 20dp и плавающим Glassbar",
        badge = "По умолчанию"
    ),
    DARK_GLASS(
        title = "Вариант Б: Dark Obsidian Glass",
        subtitle = "Глубокий обсидиан с неоновым изумрудным свечением, полупрозрачным стеклом и плавающим круглым Dock",
        badge = "Неоновый обсидиан"
    ),
    MINIMAL_EDITORIAL(
        title = "Вариант В: Minimalist Editorial",
        subtitle = "Строгий монохромный академический стиль, крупные контрастные шрифты и компактная навигация",
        badge = "Шведский монохром"
    )
}

data class StudentGroup(
    val id: Int,
    val name: String,
    val faculty: String = "",
    val course: Int = 1,
    val instituteAbbr: String = "ИКНКБ", // "ИКНКБ", "ИММиТ", "ФИЗМЕХ", "ИПМЭиТ", "ИСИ", "ИЭ", "ГИ", "ИБСиБ"
    val courseNumber: Int = 1,
    val specialization: String = "Информационная безопасность"
)

data class TeachingAssignment(
    val subject: String,
    val role: String, // "Лекция", "Практические занятия", "Лабораторный практикум"
    val groupName: String,
    val room: String,
    val buildingAbbr: String,
    val dayOfWeek: String,
    val time: String
)

data class Teacher(
    val id: Int,
    val fullName: String,
    val chair: String,
    val institute: String,
    val email: String,
    val phone: String = "+7 (812) 552-76-23",
    val consultations: String = "Ср 15:30–17:00, ауд. 237 ГК",
    val photoUrl: String = "",
    val degree: String = "Кандидат наук, доцент",
    val assignments: List<TeachingAssignment> = emptyList()
)

data class RoomDailySlot(
    val pairIndex: Int, // 1..6
    val timeStart: String,
    val timeEnd: String,
    val isOccupied: Boolean,
    val subject: String? = null,
    val groupName: String? = null,
    val teacherName: String? = null,
    val lessonType: LessonType? = null
)

enum class LessonType(val displayName: String) {
    LECTURE("Лекция"),
    PRACTICE("Практика"),
    LAB("Лабораторная"),
    SEMINAR("Семинар"),
    EXAM("Экзамен")
}

data class Lesson(
    val id: String,
    val subject: String,
    val type: LessonType,
    val timeStart: String,
    val timeEnd: String,
    val buildingAbbr: String,
    val buildingName: String,
    val roomNumber: String,
    val floor: Int,
    val teacherName: String,
    val teacherId: Int? = null,
    val groupName: String,
    val sdoUrl: String = "https://dl.spbstu.ru/",
    val sdoCourseName: String = ""
)

data class DaySchedule(
    val dateString: String,
    val dayOfWeek: String,
    val dayShort: String,
    val isToday: Boolean,
    val lessons: List<Lesson>
)

sealed class CurrentPairStatus {
    object NoClasses : CurrentPairStatus()
    data class InProgress(
        val lesson: Lesson,
        val minutesRemaining: Int,
        val progress: Float
    ) : CurrentPairStatus()
    data class Upcoming(
        val lesson: Lesson,
        val minutesUntilStart: Int
    ) : CurrentPairStatus()
    object FinishedForToday : CurrentPairStatus()
}

data class StudentNote(
    val id: String,
    val text: String,
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class CampusNewsItem(
    val id: String,
    val title: String,
    val category: String,
    val date: String,
    val summary: String,
    val fullText: String,
    val sourceUrl: String = "https://www.spbstu.ru/media/news/"
)

data class PolyService(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val url: String,
    val iconName: String
)
