package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.StudentGroup
import com.example.ui.theme.PolyGreenContainer
import com.example.ui.theme.PolyGreenOnContainer
import com.example.ui.theme.PolyGreenPrimary

@Composable
fun GroupPickerDialog(
    isOpen: Boolean,
    query: String,
    selectedInstitute: String,
    selectedCourse: Int,
    results: List<StudentGroup>,
    onQueryChanged: (String) -> Unit,
    onInstituteSelected: (String) -> Unit,
    onCourseSelected: (Int) -> Unit,
    onGroupSelected: (StudentGroup) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val institutes = listOf("Все", "ИКНКБ", "ИММиТ", "ФИЗМЕХ", "ИПМЭиТ", "ИСИ", "ИЭ", "ГИ", "ИБСиБ")
    val courses = listOf(Pair(0, "Все курсы"), Pair(1, "1 курс"), Pair(2, "2 курс"), Pair(3, "3 курс"), Pair(4, "4 курс"))

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Настройка группы",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Фильтры институтов, курсов и направлений",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search field
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChanged,
                    placeholder = { Text("Поиск (напр. 60002 или мехатроника)") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PolyGreenPrimary) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PolyGreenPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 1. Institute Filter Chips
                Text(
                    text = "Институт:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(institutes) { inst ->
                        val isSelected = inst == selectedInstitute
                        FilterChip(
                            selected = isSelected,
                            onClick = { onInstituteSelected(inst) },
                            label = { Text(inst, fontSize = 12.sp) },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.5.dp, if (isSelected) PolyGreenPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PolyGreenPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 2. Course Filter Chips
                Text(
                    text = "Курс обучения:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(courses) { (courseNum, courseTitle) ->
                        val isSelected = courseNum == selectedCourse
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCourseSelected(courseNum) },
                            label = { Text(courseTitle, fontSize = 12.sp) },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.5.dp, if (isSelected) PolyGreenPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PolyGreenPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Найдено групп: ${results.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Results list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(results, key = { it.id }) { group ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onGroupSelected(group) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Surface(
                                    color = PolyGreenContainer,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Group,
                                        contentDescription = null,
                                        tint = PolyGreenPrimary,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Группа ${group.name}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Surface(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                        ) {
                                            Text(
                                                text = "${group.instituteAbbr} • ${group.courseNumber} курс",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PolyGreenPrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = group.specialization,
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
    }
}
