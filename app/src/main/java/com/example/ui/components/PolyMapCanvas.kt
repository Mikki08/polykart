package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import com.example.data.model.Floor
import com.example.data.model.Room
import com.example.data.model.RoomType

@Composable
fun PolyMapCanvas(
    floor: Floor?,
    highlightedRoomNumber: String?,
    onRoomSelected: (Room) -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Reset transform when floor changes
    LaunchedEffect(floor?.floorNumber) {
        scale = 1f
        offset = Offset.Zero
    }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.6f, 3.5f)
        offset += offsetChange
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .transformable(state = transformState)
            .pointerInput(floor) {
                detectTapGestures { tapOffset ->
                    if (floor == null) return@detectTapGestures
                    // Convert screen tap coordinates into canvas 1000x1000 space
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    val centerOffsetX = (canvasWidth - 1000f * scale) / 2f + offset.x
                    val centerOffsetY = (canvasHeight - 650f * scale) / 2f + offset.y

                    val localX = (tapOffset.x - centerOffsetX) / scale
                    val localY = (tapOffset.y - centerOffsetY) / scale

                    // Find if any room was tapped
                    val clickedRoom = floor.rooms.firstOrNull { room ->
                        localX in room.x..(room.x + room.width) &&
                        localY in room.y..(room.y + room.height)
                    }

                    if (clickedRoom != null) {
                        onRoomSelected(clickedRoom)
                    }
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        ) {
            if (floor == null) return@Canvas

            // Center base canvas of 1000 x 650
            val baseX = (size.width - 1000f) / 2f
            val baseY = (size.height - 650f) / 2f

            // 1. Draw floor building perimeter outline
            drawRoundRect(
                color = Color(0xFFF1F5F9),
                topLeft = Offset(baseX + 40f, baseY + 120f),
                size = Size(920f, 430f),
                cornerRadius = CornerRadius(16f, 16f)
            )
            drawRoundRect(
                color = Color(0xFFCBD5E1),
                topLeft = Offset(baseX + 40f, baseY + 120f),
                size = Size(920f, 430f),
                cornerRadius = CornerRadius(16f, 16f),
                style = Stroke(width = 3f)
            )

            // 2. Draw Hallways / Corridors
            floor.hallways.forEach { hw ->
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(baseX + hw.x1, baseY + hw.y1),
                    end = Offset(baseX + hw.x2, baseY + hw.y2),
                    strokeWidth = hw.strokeWidth,
                    cap = StrokeCap.Round
                )
            }

            // 3. Draw Rooms
            floor.rooms.forEach { room ->
                val isHighlighted = highlightedRoomNumber != null &&
                    room.number.contains(highlightedRoomNumber, ignoreCase = true)

                val roomColor = when (room.type) {
                    RoomType.LECTURE_HALL -> Color(0xFFE0F2FE) // Sky
                    RoomType.CLASSROOM -> Color(0xFFDCFCE7)    // Green
                    RoomType.LAB -> Color(0xFFF3E8FF)          // Purple
                    RoomType.DEAN_OFFICE -> Color(0xFFFEF3C7)  // Amber
                    RoomType.CAFE -> Color(0xFFFFEDD5)         // Orange
                    RoomType.WC -> Color(0xFFCCFBF1)           // Teal
                    RoomType.STAIRS -> Color(0xFFE2E8F0)       // Slate
                    RoomType.COWORKING -> Color(0xFFEDE9FE)    // Violet
                    RoomType.LIBRARY -> Color(0xFFFEF08A)      // Yellow
                    RoomType.ENTRANCE -> Color(0xFFF1F5F9)
                }

                val strokeColor = if (isHighlighted) {
                    Color(0xFFE11D48) // Red-Pink pulse accent for search match
                } else {
                    when (room.type) {
                        RoomType.LECTURE_HALL -> Color(0xFF0284C7)
                        RoomType.CLASSROOM -> Color(0xFF16A34A)
                        RoomType.LAB -> Color(0xFF9333EA)
                        RoomType.DEAN_OFFICE -> Color(0xFFD97706)
                        RoomType.CAFE -> Color(0xFFEA580C)
                        RoomType.WC -> Color(0xFF0D9488)
                        else -> Color(0xFF94A3B8)
                    }
                }

                val roomTopLeft = Offset(baseX + room.x, baseY + room.y)
                val roomSize = Size(room.width, room.height)

                // Fill room
                drawRoundRect(
                    color = if (isHighlighted) Color(0xFFFFE4E6) else roomColor,
                    topLeft = roomTopLeft,
                    size = roomSize,
                    cornerRadius = CornerRadius(8f, 8f)
                )

                // Room border (thicker if highlighted)
                drawRoundRect(
                    color = strokeColor,
                    topLeft = roomTopLeft,
                    size = roomSize,
                    cornerRadius = CornerRadius(8f, 8f),
                    style = Stroke(width = if (isHighlighted) 4.5f else 1.8f)
                )

                // Text labels using Android Native Canvas
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        isAntiAlias = true
                        textAlign = android.graphics.Paint.Align.CENTER
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }

                    // Main Room Number
                    paint.color = if (isHighlighted) android.graphics.Color.parseColor("#9F1239") else android.graphics.Color.parseColor("#0F172A")
                    paint.textSize = if (room.width > 120f) 22f else 18f
                    val centerX = baseX + room.x + (room.width / 2f)
                    val centerY = baseY + room.y + (room.height / 2f) - (if (room.title.isNotEmpty()) 6f else 0f)

                    drawText(room.number, centerX, centerY, paint)

                    // Secondary Subtitle/Label
                    if (room.title.isNotEmpty() && room.height > 60f) {
                        paint.textSize = 12f
                        paint.typeface = android.graphics.Typeface.DEFAULT
                        paint.color = android.graphics.Color.parseColor("#475569")
                        val shortTitle = if (room.title.length > 16) room.title.take(14) + "…" else room.title
                        drawText(shortTitle, centerX, centerY + 20f, paint)
                    }
                }
            }
        }
    }
}
