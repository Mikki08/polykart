package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.EmptyDayCard
import com.example.ui.components.WeekdaySelector
import com.example.ui.theme.PolyGreenContainer
import com.example.ui.theme.PolyGreenOnContainer
import com.example.ui.theme.PolyGreenPrimary
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScheduleViewMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: MainViewModel,
    uiState: com.example.ui.viewmodel.MainUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Недельное расписание",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = PolyGreenContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "ruz.spbstu.ru",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = PolyGreenOnContainer,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Text(
                            text = when (uiState.scheduleViewMode) {
                                ScheduleViewMode.BY_GROUP -> "Группа ${uiState.currentGroup.name} (${uiState.currentGroup.specialization})"
                                ScheduleViewMode.BY_ROOM -> "Аудитория ${uiState.activeFilterRoomNumber ?: "Все"}"
                                ScheduleViewMode.BY_TEACHER -> "${uiState.activeFilterTeacher?.fullName ?: "Преподаватель"}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadScheduleForGroup(uiState.currentGroup.id) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить расписание")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Mode Segmented Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.scheduleViewMode == ScheduleViewMode.BY_GROUP,
                    onClick = { viewModel.setScheduleViewMode(ScheduleViewMode.BY_GROUP) },
                    label = { Text("По группе", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.75.dp, if (uiState.scheduleViewMode == ScheduleViewMode.BY_GROUP) PolyGreenPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PolyGreenPrimary, selectedLabelColor = Color.White)
                )
                FilterChip(
                    selected = uiState.scheduleViewMode == ScheduleViewMode.BY_ROOM,
                    onClick = { viewModel.setScheduleViewMode(ScheduleViewMode.BY_ROOM) },
                    label = { Text("По аудитории", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.MeetingRoom, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.75.dp, if (uiState.scheduleViewMode == ScheduleViewMode.BY_ROOM) PolyGreenPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PolyGreenPrimary, selectedLabelColor = Color.White)
                )
                FilterChip(
                    selected = uiState.scheduleViewMode == ScheduleViewMode.BY_TEACHER,
                    onClick = { viewModel.setScheduleViewMode(ScheduleViewMode.BY_TEACHER) },
                    label = { Text("Преподаватель", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.75.dp, if (uiState.scheduleViewMode == ScheduleViewMode.BY_TEACHER) PolyGreenPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PolyGreenPrimary, selectedLabelColor = Color.White)
                )
            }

            // Filter Active Banner
            if (uiState.scheduleViewMode != ScheduleViewMode.BY_GROUP) {
                Surface(
                    color = PolyGreenContainer,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.5.dp, PolyGreenPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (uiState.scheduleViewMode == ScheduleViewMode.BY_ROOM) {
                                "Фильтр: ауд. ${uiState.activeFilterRoomNumber ?: "Все"}"
                            } else {
                                "Фильтр: ${uiState.activeFilterTeacher?.fullName ?: "Все"}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = PolyGreenOnContainer
                        )
                        TextButton(onClick = { viewModel.setScheduleViewMode(ScheduleViewMode.BY_GROUP) }) {
                            Text("Сбросить к группе", fontSize = 12.sp, color = PolyGreenPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Weekday Selector (Пн..Сб)
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                WeekdaySelector(
                    weekSchedule = uiState.weekSchedule,
                    selectedIndex = uiState.selectedDayIndex,
                    onSelectDay = { viewModel.selectDay(it) }
                )
            }

            // Lessons List
            val selectedDay = uiState.weekSchedule.getOrNull(uiState.selectedDayIndex)
            val allLessons = selectedDay?.lessons ?: emptyList()

            val filteredLessons = when (uiState.scheduleViewMode) {
                ScheduleViewMode.BY_GROUP -> allLessons
                ScheduleViewMode.BY_ROOM -> {
                    val roomNum = uiState.activeFilterRoomNumber
                    if (roomNum.isNullOrBlank()) allLessons else allLessons.filter { it.roomNumber.contains(roomNum, ignoreCase = true) }
                }
                ScheduleViewMode.BY_TEACHER -> {
                    val teacher = uiState.activeFilterTeacher
                    if (teacher == null) allLessons else allLessons.filter { it.teacherName.contains(teacher.fullName.take(6), ignoreCase = true) }
                }
            }

            if (filteredLessons.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyDayCard()
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredLessons, key = { it.id }) { lesson ->
                        TodayLessonCardScrn(
                            lesson = lesson,
                            onNavigateToMap = {
                                viewModel.navigateToRoomOnMap(lesson.buildingAbbr, lesson.roomNumber, lesson.floor)
                            },
                            onOpenSdo = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(lesson.sdoUrl))
                                context.startActivity(intent)
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(36.dp))
                    }
                }
            }
        }
    }
}
