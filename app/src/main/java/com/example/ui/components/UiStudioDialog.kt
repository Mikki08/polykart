package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UiThemeMode
import com.example.ui.theme.PolyGreenContainer
import com.example.ui.theme.PolyGreenOnContainer
import com.example.ui.theme.PolyGreenPrimary

@Composable
fun UiStudioDialog(
    isOpen: Boolean,
    activeTheme: UiThemeMode,
    onSelectTheme: (UiThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(PolyGreenContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = PolyGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Студия интерфейсов",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Выберите готовый дизайн с Scrn.gallery",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(UiThemeMode.values()) { mode ->
                        val isActive = mode == activeTheme
                        ThemeOptionPreviewCard(
                            mode = mode,
                            isActive = isActive,
                            onSelect = {
                                onSelectTheme(mode)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeOptionPreviewCard(
    mode: UiThemeMode,
    isActive: Boolean,
    onSelect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (mode) {
                UiThemeMode.IOS_BENTO -> Color(0xFFF8FAF8)
                UiThemeMode.DARK_GLASS -> Color(0xFF121815)
                UiThemeMode.MINIMAL_EDITORIAL -> Color(0xFFFAFAFA)
            }
        ),
        border = BorderStroke(
            if (isActive) 2.dp else 0.75.dp,
            if (isActive) PolyGreenPrimary else Color(0x33888888)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 4.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = when (mode) {
                        UiThemeMode.IOS_BENTO -> PolyGreenContainer
                        UiThemeMode.DARK_GLASS -> Color(0xFF064E3B)
                        UiThemeMode.MINIMAL_EDITORIAL -> Color(0xFFE2E8F0)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = mode.badge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (mode) {
                            UiThemeMode.IOS_BENTO -> PolyGreenOnContainer
                            UiThemeMode.DARK_GLASS -> Color(0xFF34D399)
                            UiThemeMode.MINIMAL_EDITORIAL -> Color(0xFF0F172A)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                if (isActive) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = PolyGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Активен",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolyGreenPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = mode.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = when (mode) {
                    UiThemeMode.DARK_GLASS -> Color.White
                    else -> Color(0xFF0F172A)
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = mode.subtitle,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = when (mode) {
                    UiThemeMode.DARK_GLASS -> Color(0xFF9CA3AF)
                    else -> Color(0xFF475569)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Mini visual representation wireframe preview
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when (mode) {
                    UiThemeMode.IOS_BENTO -> Color.White
                    UiThemeMode.DARK_GLASS -> Color(0xFF1E2723)
                    UiThemeMode.MINIMAL_EDITORIAL -> Color(0xFFF1F5F9)
                },
                border = BorderStroke(0.5.dp, Color(0x1F000000)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (mode) {
                                    UiThemeMode.IOS_BENTO -> PolyGreenPrimary
                                    UiThemeMode.DARK_GLASS -> Color(0xFF10B981)
                                    UiThemeMode.MINIMAL_EDITORIAL -> Color(0xFF0F172A)
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .width(90.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when (mode) {
                                        UiThemeMode.DARK_GLASS -> Color.White.copy(alpha = 0.8f)
                                        else -> Color(0xFF334155)
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(50.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.Gray.copy(alpha = 0.4f))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) PolyGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isActive) "Стиль выбран" else "Активировать этот дизайн",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
