package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.PolytechRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.*

enum class AppTab {
    HOME,
    POLYMAP,
    SCHEDULE,
    SEARCH,
    SERVICES
}

enum class SearchCategory(val title: String) {
    ALL("Все"),
    TEACHERS("Преподаватели"),
    ROOMS("Аудитории"),
    GROUPS("Группы"),
    BUILDINGS("Корпуса"),
    SERVICES("Сервисы")
}

enum class ScheduleViewMode(val title: String) {
    BY_GROUP("Группа"),
    BY_ROOM("Аудитория"),
    BY_TEACHER("Преподаватель")
}

data class SearchResultItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: SearchCategory,
    val iconType: String,
    val payload: Any? = null,
    val roleBadge: String? = null,
    val teacherPhotoUrl: String? = null
)

data class MainUiState(
    val activeTab: AppTab = AppTab.HOME,
    val currentGroup: StudentGroup,
    val weekSchedule: List<DaySchedule> = emptyList(),
    val selectedDayIndex: Int = 0,
    val currentPairStatus: CurrentPairStatus = CurrentPairStatus.NoClasses,
    val isLoadingSchedule: Boolean = false,
    val isWeekOdd: Boolean = false,

    // Student Notes
    val studentNotes: List<StudentNote> = emptyList(),
    val isEditingNoteDialog: Boolean = false,
    val editingNoteId: String? = null,
    val editingNoteText: String = "",

    // PolyMap state
    val buildings: List<Building> = emptyList(),
    val selectedBuilding: Building,
    val selectedFloorNumber: Int = 1,
    val highlightedRoomId: String? = null,
    val highlightedRoomNumber: String? = null,
    val inspectedRoom: Room? = null,
    val inspectedRoomSlots: List<RoomDailySlot> = emptyList(),

    // Schedule screen
    val scheduleViewMode: ScheduleViewMode = ScheduleViewMode.BY_GROUP,
    val activeFilterTeacher: Teacher? = null,
    val activeFilterRoomNumber: String? = null,

    // Search state
    val searchQuery: String = "",
    val searchCategory: SearchCategory = SearchCategory.ALL,
    val searchResults: List<SearchResultItem> = emptyList(),

    // Services & News state
    val polyServices: List<PolyService> = emptyList(),
    val newsList: List<CampusNewsItem> = emptyList(),
    val selectedNewsCategory: String = "Все",
    val readNewsItem: CampusNewsItem? = null,

    // Advanced Group Picker Dialog with Filters
    val isGroupPickerOpen: Boolean = false,
    val groupPickerQuery: String = "",
    val groupFilterInstitute: String = "Все",
    val groupFilterCourse: Int = 0,
    val filteredGroups: List<StudentGroup> = emptyList(),

    // UI Theme Studio State
    val uiThemeMode: UiThemeMode = UiThemeMode.IOS_BENTO,
    val isUiStudioOpen: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository = PolytechRepository(application.applicationContext)

    private val _uiState: MutableStateFlow<MainUiState>
    val uiState: StateFlow<MainUiState>

    init {
        val savedGroup = repository.getSavedGroup()
        val savedTheme = repository.getSavedTheme()
        val buildings = repository.getCampusBuildings()
        val defaultBuilding = buildings.first()
        val notes = repository.getNotes()

        _uiState = MutableStateFlow(
            MainUiState(
                currentGroup = savedGroup,
                uiThemeMode = savedTheme,
                buildings = buildings,
                selectedBuilding = defaultBuilding,
                selectedFloorNumber = 1,
                studentNotes = notes,
                polyServices = repository.getPolyServices(),
                newsList = repository.getCampusNews(),
                filteredGroups = repository.getAllGroups()
            )
        )
        uiState = _uiState.asStateFlow()

        loadScheduleForGroup(savedGroup.id)
        startPairTimerLoop()
        executeSearch("", SearchCategory.ALL)
    }

    fun selectTab(tab: AppTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun selectDay(index: Int) {
        if (index in 0 until _uiState.value.weekSchedule.size) {
            _uiState.update { it.copy(selectedDayIndex = index) }
        }
    }

    fun loadScheduleForGroup(groupId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSchedule = true) }
            val schedule = repository.getGroupSchedule(groupId)
            val todayIndex = schedule.indexOfFirst { it.isToday }.let { if (it >= 0) it else 0 }
            _uiState.update {
                it.copy(
                    weekSchedule = schedule,
                    selectedDayIndex = todayIndex,
                    isLoadingSchedule = false
                )
            }
            updateCurrentPairStatus()
        }
    }

    private fun startPairTimerLoop() {
        viewModelScope.launch {
            while (isActive) {
                updateCurrentPairStatus()
                delay(15_000)
            }
        }
    }

    private fun updateCurrentPairStatus() {
        val state = _uiState.value
        val schedule = state.weekSchedule
        if (schedule.isEmpty()) return

        val todayIndex = schedule.indexOfFirst { it.isToday }.let { if (it >= 0) it else state.selectedDayIndex }
        val todayLessons = schedule.getOrNull(todayIndex)?.lessons ?: emptyList()

        if (todayLessons.isEmpty()) {
            _uiState.update { it.copy(currentPairStatus = CurrentPairStatus.NoClasses) }
            return
        }

        val now = Calendar.getInstance()
        val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        var status: CurrentPairStatus = CurrentPairStatus.FinishedForToday

        for (lesson in todayLessons) {
            val startMin = parseTimeToMinutes(lesson.timeStart)
            val endMin = parseTimeToMinutes(lesson.timeEnd)

            if (nowMinutes in startMin..endMin) {
                val totalDuration = (endMin - startMin).toFloat().coerceAtLeast(1f)
                val elapsed = (nowMinutes - startMin).toFloat()
                val progress = (elapsed / totalDuration).coerceIn(0f, 1f)
                val remaining = endMin - nowMinutes
                status = CurrentPairStatus.InProgress(lesson, remaining, progress)
                break
            } else if (nowMinutes < startMin) {
                val untilStart = startMin - nowMinutes
                status = CurrentPairStatus.Upcoming(lesson, untilStart)
                break
            }
        }

        _uiState.update { it.copy(currentPairStatus = status) }
    }

    private fun parseTimeToMinutes(timeStr: String): Int {
        val parts = timeStr.split(":")
        if (parts.size == 2) {
            val h = parts[0].toIntOrNull() ?: 0
            val m = parts[1].toIntOrNull() ?: 0
            return h * 60 + m
        }
        return 0
    }

    // Student Notes Actions
    fun addNote(text: String) {
        if (text.isNotBlank()) {
            val updated = repository.addNote(text.trim())
            _uiState.update { it.copy(studentNotes = updated, isEditingNoteDialog = false, editingNoteText = "") }
        }
    }

    fun toggleNote(id: String) {
        val updated = repository.toggleNote(id)
        _uiState.update { it.copy(studentNotes = updated) }
    }

    fun deleteNote(id: String) {
        val updated = repository.deleteNote(id)
        _uiState.update { it.copy(studentNotes = updated) }
    }

    fun openAddNoteDialog() {
        _uiState.update { it.copy(isEditingNoteDialog = true, editingNoteId = null, editingNoteText = "") }
    }

    fun openEditNoteDialog(note: StudentNote) {
        _uiState.update { it.copy(isEditingNoteDialog = true, editingNoteId = note.id, editingNoteText = note.text) }
    }

    fun closeNoteDialog() {
        _uiState.update { it.copy(isEditingNoteDialog = false, editingNoteId = null, editingNoteText = "") }
    }

    fun onNoteTextChanged(text: String) {
        _uiState.update { it.copy(editingNoteText = text) }
    }

    fun saveCurrentNoteDialog() {
        val state = _uiState.value
        val text = state.editingNoteText.trim()
        if (text.isNotBlank()) {
            val id = state.editingNoteId
            val updated = if (id != null) {
                repository.updateNote(id, text)
            } else {
                repository.addNote(text)
            }
            _uiState.update { it.copy(studentNotes = updated, isEditingNoteDialog = false, editingNoteText = "", editingNoteId = null) }
        }
    }

    // PolyMap Controls
    fun selectBuilding(building: Building) {
        val defaultFloor = building.availableFloors.firstOrNull() ?: 1
        _uiState.update {
            it.copy(
                selectedBuilding = building,
                selectedFloorNumber = defaultFloor,
                highlightedRoomId = null,
                highlightedRoomNumber = null,
                inspectedRoom = null,
                inspectedRoomSlots = emptyList()
            )
        }
    }

    fun selectFloor(floorNumber: Int) {
        _uiState.update {
            it.copy(
                selectedFloorNumber = floorNumber,
                inspectedRoom = null,
                inspectedRoomSlots = emptyList()
            )
        }
    }

    fun inspectRoom(room: Room) {
        val slots = repository.getRoomDailySchedule(room.buildingId, room.number)
        _uiState.update {
            it.copy(
                inspectedRoom = room,
                inspectedRoomSlots = slots,
                highlightedRoomNumber = room.number
            )
        }
    }

    fun clearInspectedRoom() {
        _uiState.update { it.copy(inspectedRoom = null, inspectedRoomSlots = emptyList()) }
    }

    fun navigateToRoomOnMap(buildingAbbr: String, roomNumber: String, floorNumber: Int) {
        val buildings = _uiState.value.buildings
        val targetBuilding = buildings.firstOrNull {
            it.abbr.equals(buildingAbbr, ignoreCase = true) ||
            it.name.contains(buildingAbbr, ignoreCase = true)
        } ?: buildings.first()

        val validFloor = if (targetBuilding.availableFloors.contains(floorNumber)) {
            floorNumber
        } else {
            targetBuilding.availableFloors.firstOrNull() ?: 1
        }

        _uiState.update {
            it.copy(
                activeTab = AppTab.POLYMAP,
                selectedBuilding = targetBuilding,
                selectedFloorNumber = validFloor,
                highlightedRoomNumber = roomNumber
            )
        }

        val floor = targetBuilding.floors[validFloor]
        val room = floor?.rooms?.firstOrNull { it.number.contains(roomNumber, ignoreCase = true) }
        if (room != null) {
            inspectRoom(room)
        }
    }

    // Search Controls
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        executeSearch(query, _uiState.value.searchCategory)
    }

    fun onSearchCategorySelected(category: SearchCategory) {
        _uiState.update { it.copy(searchCategory = category) }
        executeSearch(_uiState.value.searchQuery, category)
    }

    private fun executeSearch(query: String, category: SearchCategory) {
        val q = query.trim().lowercase()
        val results = mutableListOf<SearchResultItem>()

        // 1. Smart NLP search for Teachers
        if (category == SearchCategory.ALL || category == SearchCategory.TEACHERS) {
            val matchedTeachers = if (q.isNotEmpty()) repository.smartSearch(query) else repository.getTeachers()
            matchedTeachers.forEach { t ->
                val primaryAssignment = t.assignments.firstOrNull()
                val roleBadge = primaryAssignment?.let { "${it.subject} • ${it.role}" } ?: t.degree
                val subText = if (primaryAssignment != null) {
                    "${t.chair} • Гр. ${primaryAssignment.groupName} (${primaryAssignment.room} ${primaryAssignment.buildingAbbr})"
                } else {
                    "${t.chair} • ${t.email}"
                }

                results.add(
                    SearchResultItem(
                        id = "teacher_${t.id}",
                        title = t.fullName,
                        subtitle = subText,
                        category = SearchCategory.TEACHERS,
                        iconType = "teacher",
                        payload = t,
                        roleBadge = roleBadge,
                        teacherPhotoUrl = t.photoUrl
                    )
                )
            }
        }

        // 2. Rooms with immediate schedule status
        if (category == SearchCategory.ALL || category == SearchCategory.ROOMS) {
            _uiState.value.buildings.forEach { bldg ->
                bldg.floors.values.forEach { floor ->
                    floor.rooms.filter {
                        q.isEmpty() || it.number.lowercase().contains(q) ||
                        it.title.lowercase().contains(q) || it.description.lowercase().contains(q)
                    }.forEach { room ->
                        val slots = repository.getRoomDailySchedule(bldg.abbr, room.number)
                        val occupiedSlot = slots.firstOrNull { it.isOccupied }
                        val statusText = if (occupiedSlot != null) {
                            "Занято: ${occupiedSlot.subject} (${occupiedSlot.timeStart} – ${occupiedSlot.timeEnd})"
                        } else {
                            "Сейчас свободно для самоподготовки"
                        }

                        results.add(
                            SearchResultItem(
                                id = "room_${room.id}",
                                title = "Ауд. ${room.number}: ${room.title}",
                                subtitle = "${bldg.name}, ${room.floor} этаж • $statusText",
                                category = SearchCategory.ROOMS,
                                iconType = "room",
                                payload = Pair(bldg, room),
                                roleBadge = if (occupiedSlot != null) "Занято" else "Свободно"
                            )
                        )
                    }
                }
            }
        }

        // 3. Buildings
        if (category == SearchCategory.ALL || category == SearchCategory.BUILDINGS) {
            _uiState.value.buildings.filter {
                q.isEmpty() || it.name.lowercase().contains(q) ||
                it.abbr.lowercase().contains(q) || it.description.lowercase().contains(q)
            }.forEach { b ->
                results.add(
                    SearchResultItem(
                        id = "building_${b.id}",
                        title = "${b.name} (${b.abbr})",
                        subtitle = "${b.address} • Этажи: ${b.availableFloors.joinToString(", ")}",
                        category = SearchCategory.BUILDINGS,
                        iconType = "building",
                        payload = b
                    )
                )
            }
        }

        // 4. Groups
        if (category == SearchCategory.ALL || category == SearchCategory.GROUPS) {
            repository.getAllGroups().filter {
                q.isEmpty() || it.name.lowercase().contains(q) ||
                it.specialization.lowercase().contains(q) || it.faculty.lowercase().contains(q)
            }.forEach { g ->
                results.add(
                    SearchResultItem(
                        id = "group_${g.id}",
                        title = "Группа ${g.name}",
                        subtitle = "${g.instituteAbbr} • ${g.courseNumber} курс • ${g.specialization}",
                        category = SearchCategory.GROUPS,
                        iconType = "group",
                        payload = g,
                        roleBadge = "${g.courseNumber} курс"
                    )
                )
            }
        }

        // 5. Services
        if (category == SearchCategory.ALL || category == SearchCategory.SERVICES) {
            _uiState.value.polyServices.filter {
                q.isEmpty() || it.title.lowercase().contains(q) ||
                it.description.lowercase().contains(q)
            }.forEach { s ->
                results.add(
                    SearchResultItem(
                        id = "service_${s.id}",
                        title = s.title,
                        subtitle = s.description,
                        category = SearchCategory.SERVICES,
                        iconType = "service",
                        payload = s
                    )
                )
            }
        }

        _uiState.update { it.copy(searchResults = results) }
    }

    // Schedule Controls
    fun setScheduleViewMode(mode: ScheduleViewMode) {
        _uiState.update { it.copy(scheduleViewMode = mode) }
    }

    fun filterScheduleByTeacher(teacher: Teacher?) {
        _uiState.update {
            it.copy(
                scheduleViewMode = ScheduleViewMode.BY_TEACHER,
                activeFilterTeacher = teacher,
                activeTab = AppTab.SCHEDULE
            )
        }
    }

    fun filterScheduleByRoom(roomNumber: String?) {
        _uiState.update {
            it.copy(
                scheduleViewMode = ScheduleViewMode.BY_ROOM,
                activeFilterRoomNumber = roomNumber,
                activeTab = AppTab.SCHEDULE
            )
        }
    }

    // News Controls
    fun selectNewsCategory(category: String) {
        _uiState.update { it.copy(selectedNewsCategory = category) }
    }

    fun openNewsDetail(news: CampusNewsItem) {
        _uiState.update { it.copy(readNewsItem = news) }
    }

    fun closeNewsDetail() {
        _uiState.update { it.copy(readNewsItem = null) }
    }

    // Group Picker with multi-level filtering
    fun openGroupPicker() {
        _uiState.update {
            it.copy(
                isGroupPickerOpen = true,
                groupPickerQuery = "",
                groupFilterInstitute = "Все",
                groupFilterCourse = 0,
                filteredGroups = repository.getAllGroups()
            )
        }
    }

    fun closeGroupPicker() {
        _uiState.update { it.copy(isGroupPickerOpen = false) }
    }

    fun onGroupPickerQueryChanged(query: String) {
        _uiState.update { it.copy(groupPickerQuery = query) }
        applyGroupFilters()
    }

    fun onGroupFilterInstituteSelected(institute: String) {
        _uiState.update { it.copy(groupFilterInstitute = institute) }
        applyGroupFilters()
    }

    fun onGroupFilterCourseSelected(course: Int) {
        _uiState.update { it.copy(groupFilterCourse = course) }
        applyGroupFilters()
    }

    private fun applyGroupFilters() {
        val state = _uiState.value
        val list = repository.getFilteredGroups(
            instituteFilter = state.groupFilterInstitute,
            courseFilter = state.groupFilterCourse,
            query = state.groupPickerQuery
        )
        _uiState.update { it.copy(filteredGroups = list) }
    }

    fun selectAndSaveGroup(group: StudentGroup) {
        repository.saveGroup(group)
        _uiState.update {
            it.copy(
                currentGroup = group,
                isGroupPickerOpen = false
            )
        }
        loadScheduleForGroup(group.id)
    }

    // UI Studio Theme Methods
    fun openUiStudio() {
        _uiState.update { it.copy(isUiStudioOpen = true) }
    }

    fun closeUiStudio() {
        _uiState.update { it.copy(isUiStudioOpen = false) }
    }

    fun selectUiTheme(mode: UiThemeMode) {
        repository.saveTheme(mode)
        _uiState.update { it.copy(uiThemeMode = mode, isUiStudioOpen = false) }
    }
}
