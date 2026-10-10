package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    uiState: com.example.ui.viewmodel.MainUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val todaySchedule = uiState.weekSchedule.firstOrNull { it.isToday }
        ?: uiState.weekSchedule.firstOrNull()

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Политех Спутник",
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
                                    text = "СПбПУ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = PolyGreenOnContainer,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Text(
                            text = "${todaySchedule?.dayOfWeek ?: "Сегодня"} • ${if (uiState.isWeekOdd) "Нечётная" else "Чётная"} неделя",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // UI Studio Theme Button
                        IconButton(onClick = { viewModel.openUiStudio() }) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Студия интерфейсов",
                                tint = PolyGreenPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Group Pill Switcher
                        Surface(
                            onClick = { viewModel.openGroupPicker() },
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = PolyGreenPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = uiState.currentGroup.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Current / Next Pair Bento Hero Card
            item {
                Spacer(modifier = Modifier.height(4.dp))
                PairHeroCardScrn(
                    status = uiState.currentPairStatus,
                    onNavigateToMap = { bldg, room, floor ->
                        viewModel.navigateToRoomOnMap(bldg, room, floor)
                    },
                    onOpenSdo = { url ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    }
                )
            }

            // 2. Today's Schedule (Strictly Today, as requested!)
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Расписание на сегодня",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = { viewModel.selectTab(com.example.ui.viewmodel.AppTab.SCHEDULE) }) {
                        Text("Вся неделя →", color = PolyGreenPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }

                if (todaySchedule == null || todaySchedule.lessons.isEmpty()) {
                    EmptyTodayCard()
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        todaySchedule.lessons.forEach { lesson ->
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
                    }
                }
            }

            // 3. Student Personal Notes & Tasks (Interactive & Editable)
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Заметки и задачи",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${uiState.studentNotes.count { !it.isCompleted }} активных задач",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FilledTonalButton(
                        onClick = { viewModel.openAddNoteDialog() },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = PolyGreenContainer,
                            contentColor = PolyGreenOnContainer
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Заметка", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (uiState.studentNotes.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Нет заметок",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Добавьте напоминание о сдаче лабораторной работы или консультации.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.studentNotes.forEach { note ->
                            StudentNoteCardScrn(
                                note = note,
                                onToggle = { viewModel.toggleNote(note.id) },
                                onEdit = { viewModel.openEditNoteDialog(note) },
                                onDelete = { viewModel.deleteNote(note.id) }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }

    // Add / Edit Note Dialog
    if (uiState.isEditingNoteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeNoteDialog() },
            title = {
                Text(
                    text = if (uiState.editingNoteId != null) "Редактировать заметку" else "Новая заметка",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = uiState.editingNoteText,
                    onValueChange = { viewModel.onNoteTextChanged(it) },
                    placeholder = { Text("Например: Защитить лабу по химии в ауд. 128 ГК") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.saveCurrentNoteDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = PolyGreenPrimary)
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeNoteDialog() }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun PairHeroCardScrn(
    status: CurrentPairStatus,
    onNavigateToMap: (String, String, Int) -> Unit,
    onOpenSdo: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (status) {
                is CurrentPairStatus.InProgress -> PolyGreenDark
                is CurrentPairStatus.Upcoming -> MaterialTheme.colorScheme.surface
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            0.75.dp,
            if (status is CurrentPairStatus.InProgress) Color.Transparent
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            when (status) {
                is CurrentPairStatus.InProgress -> {
                    val lesson = status.lesson
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(PolyGreenLight)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ИДЁТ ПАРА",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PolyGreenLight,
                                letterSpacing = 1.sp
                            )
                        }
                        Surface(
                            color = Color(0x33FFFFFF),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Осталось ${status.minutesRemaining} мин",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = lesson.subject,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { status.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = PolyGreenLight,
                        trackColor = Color(0x33FFFFFF)
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = PolyGreenLight, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${lesson.buildingName}, ауд. ${lesson.roomNumber} (${lesson.floor} эт.)",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xCCFFFFFF), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = lesson.teacherName,
                            color = Color(0xCCFFFFFF),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { onNavigateToMap(lesson.buildingAbbr, lesson.roomNumber, lesson.floor) },
                            colors = ButtonDefaults.buttonColors(containerColor = PolyGreenLight, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("На карте PolyMap", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        FilledTonalButton(
                            onClick = { onOpenSdo(lesson.sdoUrl) },
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0x28FFFFFF), contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("В СДО", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
                is CurrentPairStatus.Upcoming -> {
                    val lesson = status.lesson
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(PolyAmber))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "СЛЕДУЮЩАЯ ПАРА",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PolyAmberDark,
                                letterSpacing = 1.sp
                            )
                        }
                        Surface(
                            color = PolyAmberContainer,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Через ${status.minutesUntilStart} мин",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PolyAmberDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = lesson.subject,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = PolyGreenPrimary, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${lesson.buildingName}, ауд. ${lesson.roomNumber}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = lesson.teacherName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { onNavigateToMap(lesson.buildingAbbr, lesson.roomNumber, lesson.floor) },
                            colors = ButtonDefaults.buttonColors(containerColor = PolyGreenPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("На карте PolyMap", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = { onOpenSdo(lesson.sdoUrl) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("В СДО", fontSize = 13.sp)
                        }
                    }
                }
                else -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = PolyGreenPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "На сегодня пары окончены 🎉",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Отличного отдыха или продуктивной самоподготовки в коворкинге НИК.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodayLessonCardScrn(
    lesson: Lesson,
    onNavigateToMap: () -> Unit,
    onOpenSdo: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time Pill
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(56.dp)
            ) {
                Text(
                    text = lesson.timeStart,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = lesson.timeEnd,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = when (lesson.type) {
                        LessonType.LECTURE -> PolyBlueLectureContainer
                        LessonType.LAB -> PolyPurpleLabContainer
                        else -> PolyGreenContainer
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = lesson.type.displayName.take(4),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (lesson.type) {
                            LessonType.LECTURE -> PolyBlueLecture
                            LessonType.LAB -> PolyPurpleLab
                            else -> PolyGreenOnContainer
                        },
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lesson.subject,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${lesson.buildingAbbr}, ауд. ${lesson.roomNumber} • ${lesson.teacherName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick Actions
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(onClick = onNavigateToMap, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Map, contentDescription = "На карте", tint = PolyGreenPrimary, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onOpenSdo, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.School, contentDescription = "В СДО", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun StudentNoteCardScrn(
    note: StudentNote,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            IconButton(onClick = onToggle, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = if (note.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = "Отметить",
                    tint = if (note.isCompleted) PolyGreenPrimary else MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = note.text,
                style = MaterialTheme.typography.bodyMedium,
                textDecoration = if (note.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                color = if (note.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onToggle() }
            )

            IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Редактировать", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun EmptyTodayCard() {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(Icons.Outlined.Weekend, contentDescription = null, tint = PolyGreenPrimary, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text("Сегодня занятий нет", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("Отличный день для самоподготовки или отдыха.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
