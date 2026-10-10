package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.SearchCategory
import com.example.ui.viewmodel.SearchResultItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: MainViewModel,
    uiState: com.example.ui.viewmodel.MainUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTeacherModal by remember { mutableStateOf<Teacher?>(null) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                // Smart Search Input
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = {
                        Text(
                            text = "Например: химия мехатроника 60002…",
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Поиск",
                            tint = PolyGreenPrimary
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Очистить")
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                        focusedBorderColor = PolyGreenPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Fast Category Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(SearchCategory.values()) { category ->
                        val isSelected = category == uiState.searchCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onSearchCategorySelected(category) },
                            label = { Text(category.title, fontSize = 13.sp) },
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
        },
        modifier = modifier
    ) { innerPadding ->
        if (uiState.searchResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Ничего не найдено",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Попробуйте ввести фамилию, предмет или номер аудитории.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(uiState.searchResults, key = { it.id }) { item ->
                    if (item.category == SearchCategory.TEACHERS && item.payload is Teacher) {
                        TeacherCardWithPhotoAndScheduleScrn(
                            teacher = item.payload,
                            roleBadge = item.roleBadge,
                            onViewSchedule = {
                                viewModel.filterScheduleByTeacher(item.payload)
                            },
                            onOpenModal = {
                                selectedTeacherModal = item.payload
                            }
                        )
                    } else {
                        GenericSearchResultCardScrn(
                            item = item,
                            onClick = {
                                when (item.category) {
                                    SearchCategory.ROOMS -> {
                                        val pair = item.payload as? Pair<*, *>
                                        val building = pair?.first as? Building
                                        val room = pair?.second as? Room
                                        if (building != null && room != null) {
                                            viewModel.navigateToRoomOnMap(building.abbr, room.number, room.floor)
                                        }
                                    }
                                    SearchCategory.GROUPS -> {
                                        (item.payload as? StudentGroup)?.let {
                                            viewModel.selectAndSaveGroup(it)
                                        }
                                    }
                                    SearchCategory.BUILDINGS -> {
                                        (item.payload as? Building)?.let {
                                            viewModel.selectBuilding(it)
                                            viewModel.selectTab(com.example.ui.viewmodel.AppTab.POLYMAP)
                                        }
                                    }
                                    SearchCategory.SERVICES -> {
                                        val service = item.payload as? PolyService
                                        if (service != null) {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(service.url))
                                            context.startActivity(intent)
                                        }
                                    }
                                    else -> {}
                                }
                            }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(36.dp))
                }
            }
        }
    }

    // Teacher Details Dialog with contacts
    selectedTeacherModal?.let { teacher ->
        AlertDialog(
            onDismissRequest = { selectedTeacherModal = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(PolyGreenContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = teacher.fullName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolyGreenPrimary
                    )
                }
            },
            title = {
                Text(text = teacher.fullName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = teacher.chair,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = teacher.institute,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp), tint = PolyGreenPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(teacher.email, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp), tint = PolyGreenPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(teacher.phone, fontSize = 13.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp), tint = PolyAmber)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Консультации: ${teacher.consultations}", fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.filterScheduleByTeacher(teacher)
                        selectedTeacherModal = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PolyGreenPrimary)
                ) {
                    Text("Расписание преподавателя")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedTeacherModal = null }) {
                    Text("Закрыть")
                }
            }
        )
    }
}

@Composable
fun TeacherCardWithPhotoAndScheduleScrn(
    teacher: Teacher,
    roleBadge: String?,
    onViewSchedule: () -> Unit,
    onOpenModal: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenModal)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Photo / Portrait Avatar
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(PolyGreenContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = teacher.fullName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolyGreenPrimary
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = teacher.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = teacher.degree,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (roleBadge != null) {
                    Surface(
                        color = PolyGreenContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = roleBadge,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolyGreenOnContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Immediate Teaching Assignment / Schedule Preview right inside card!
            if (teacher.assignments.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Расписание занятий:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        teacher.assignments.forEach { assign ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "${assign.subject} (${assign.role})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${assign.room} ${assign.buildingAbbr} • ${assign.dayOfWeek.take(2)}",
                                    fontSize = 11.sp,
                                    color = PolyGreenPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = teacher.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                TextButton(onClick = onViewSchedule) {
                    Text("Открыть расписание →", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PolyGreenPrimary)
                }
            }
        }
    }
}

@Composable
fun GenericSearchResultCardScrn(
    item: SearchResultItem,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when (item.category) {
                            SearchCategory.ROOMS -> Color(0xFFE0F2FE)
                            SearchCategory.GROUPS -> Color(0xFFFEF3C7)
                            SearchCategory.BUILDINGS -> Color(0xFFF3E8FF)
                            SearchCategory.SERVICES -> Color(0xFFFFEDD5)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (item.category) {
                        SearchCategory.ROOMS -> Icons.Default.MeetingRoom
                        SearchCategory.GROUPS -> Icons.Default.Group
                        SearchCategory.BUILDINGS -> Icons.Default.Apartment
                        SearchCategory.SERVICES -> Icons.Default.Language
                        else -> Icons.Default.Search
                    },
                    contentDescription = null,
                    tint = when (item.category) {
                        SearchCategory.ROOMS -> Color(0xFF0284C7)
                        SearchCategory.GROUPS -> PolyAmberDark
                        SearchCategory.BUILDINGS -> Color(0xFF9333EA)
                        SearchCategory.SERVICES -> Color(0xFFEA580C)
                        else -> MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (item.roleBadge != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = item.roleBadge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}
