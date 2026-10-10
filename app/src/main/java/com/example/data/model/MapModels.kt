package com.example.data.model

enum class RoomType {
    LECTURE_HALL,
    CLASSROOM,
    LAB,
    DEAN_OFFICE,
    CAFE,
    WC,
    STAIRS,
    ENTRANCE,
    LIBRARY,
    COWORKING
}

data class Room(
    val id: String,
    val number: String,
    val title: String,
    val buildingId: String,
    val floor: Int,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val type: RoomType = RoomType.CLASSROOM,
    val capacity: Int = 30,
    val department: String = "СПбПУ",
    val description: String = ""
)

data class Hallway(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val strokeWidth: Float = 16f
)

data class Floor(
    val floorNumber: Int,
    val title: String,
    val rooms: List<Room>,
    val hallways: List<Hallway> = emptyList()
)

data class Building(
    val id: String,
    val name: String,
    val abbr: String,
    val address: String,
    val description: String,
    val availableFloors: List<Int>,
    val floors: Map<Int, Floor> = emptyMap(),
    val campusX: Float = 0f,
    val campusY: Float = 0f
)
