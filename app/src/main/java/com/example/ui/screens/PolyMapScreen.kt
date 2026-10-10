package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Building
import com.example.data.model.Room
import com.example.data.model.RoomDailySlot
import com.example.ui.components.PolyMapCanvas
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolyMapScreen(
    viewModel: MainViewModel,
    uiState: com.example.ui.viewmodel.MainUiState,
    modifier: Modifier = Modifier
) {
    val selectedBuilding = uiState.selectedBuilding
    val activeFloor = selectedBuilding.floors[uiState.selectedFloorNumber]

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "PolyMap",
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
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "•  ${selectedBuilding.abbr}, ${uiState.selectedFloorNumber} эт.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Top Building Selector Chips in Scrn.gallery style
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 6.dp)
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.buildings) { building ->
                        val isSelected = building.id == selectedBuilding.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectBuilding(building) },
                            label = { Text(building.name.take(16) + if (building.name.length > 16) "…" else "", fontSize = 13.sp) },
                            leadingIcon = {
                                Surface(
                                    color = if (isSelected) Color.White.copy(alpha = 0.2f) else PolyGreenContainer,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = building.abbr,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else PolyGreenPrimary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(0.75.dp, if (isSelected) PolyGreenPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PolyGreenPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // 2. Vector Interactive PolyMap Canvas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 54.dp)
            ) {
                PolyMapCanvas(
                    floor = activeFloor,
                    highlightedRoomNumber = uiState.highlightedRoomNumber,
                    onRoomSelected = { room ->
                        viewModel.inspectRoom(room)
                    }
                )
            }

            // 3. Floating Vertical Thumb-zone Floor Selector (Right edge)
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(6.dp)
                ) {
                    selectedBuilding.availableFloors.sortedDescending().forEach { floorNum ->
                        val isFloorActive = floorNum == uiState.selectedFloorNumber
                        Surface(
                            onClick = { viewModel.selectFloor(floorNum) },
                            shape = CircleShape,
                            color = if (isFloorActive) PolyGreenPrimary else Color.Transparent,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (floorNum == 0) "Ц" else "$floorNum",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isFloorActive) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // 4. Room Inspection Card with Immediate Full Schedule for Today
            AnimatedVisibility(
                visible = uiState.inspectedRoom != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                uiState.inspectedRoom?.let { room ->
                    RoomImmediateScheduleSheetScrn(
                        room = room,
                        building = selectedBuilding,
                        slots = uiState.inspectedRoomSlots,
                        onViewWeekSchedule = {
                            viewModel.filterScheduleByRoom(room.number)
                        },
                        onClose = {
                            viewModel.clearInspectedRoom()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RoomImmediateScheduleSheetScrn(
    room: Room,
    building: Building,
    slots: List<RoomDailySlot>,
    onViewWeekSchedule: () -> Unit,
    onClose: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.68f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .width(42.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Room Title Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = PolyGreenContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Ауд. ${room.number}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolyGreenOnContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = room.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${building.name} • ${room.floor} этаж",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Закрыть")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Расписание аудитории на сегодня (1–6 пары):",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Slots Table
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(slots) { slot ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (slot.isOccupied) PolyAmberContainer.copy(alpha = 0.45f) else PolyGreenContainer.copy(alpha = 0.35f),
                        border = BorderStroke(0.5.dp, if (slot.isOccupied) PolyAmber.copy(alpha = 0.4f) else PolyGreenPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "${slot.pairIndex} пара",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.width(50.dp)
                            )
                            Text(
                                text = "${slot.timeStart}–${slot.timeEnd}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(85.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (slot.isOccupied) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = slot.subject ?: "Занятие",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${slot.teacherName ?: ""} • гр. ${slot.groupName ?: ""}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            } else {
                                Text(
                                    text = "Свободно для самоподготовки",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = PolyGreenPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onViewWeekSchedule,
                colors = ButtonDefaults.buttonColors(containerColor = PolyGreenPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Полное недельное расписание аудитории")
            }
        }
    }
}
