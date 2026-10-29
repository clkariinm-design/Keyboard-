package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CashFlowPoint
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun AuraCashFlowChart(
    points: List<CashFlowPoint>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800),
        label = "chartAnim"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
            .fillMaxWidth()
            .testTag("cash_flow_chart")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cash Flow Trajectory",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Weekly Income vs Expenses",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(EmeraldLight, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "In",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(SunsetCoral, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Out",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tooltip if selected
            selectedIndex?.let { index ->
                if (index in points.indices) {
                    val p = points[index]
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 6.dp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${p.label}: ",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "+$currencySymbol${String.format(Locale.US, "%.0f", p.income)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = EmeraldLight,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "-$currencySymbol${String.format(Locale.US, "%.0f", p.expense)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = SunsetCoral,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Canvas Chart
            val incomeColor = EmeraldLight
            val expenseColor = SunsetCoral
            val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .pointerInput(points) {
                        detectTapGestures { offset ->
                            val sectionWidth = size.width / (points.size.coerceAtLeast(1))
                            val tappedIndex = (offset.x / sectionWidth).toInt().coerceIn(0, points.size - 1)
                            selectedIndex = if (selectedIndex == tappedIndex) null else tappedIndex
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (points.isEmpty()) return@Canvas

                    val width = size.width
                    val height = size.height
                    val paddingBottom = 24.dp.toPx()
                    val chartHeight = height - paddingBottom

                    val maxVal = points.maxOfOrNull { maxOf(it.income, it.expense) }?.coerceAtLeast(100.0) ?: 100.0

                    // Draw 3 horizontal grid lines
                    for (i in 0..2) {
                        val y = chartHeight * (i / 2f)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    val spacing = width / (points.size - 1).coerceAtLeast(1)

                    // Draw Bars for each day
                    val barWidth = 14.dp.toPx()
                    points.forEachIndexed { i, pt ->
                        val xCenter = i * spacing

                        // Income bar
                        val inBarH = (pt.income / maxVal).toFloat() * chartHeight * progress
                        if (inBarH > 0f) {
                            drawRoundRect(
                                color = incomeColor.copy(alpha = 0.85f),
                                topLeft = Offset(xCenter - barWidth - 2f, chartHeight - inBarH),
                                size = androidx.compose.ui.geometry.Size(barWidth, inBarH),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                            )
                        }

                        // Expense bar
                        val outBarH = (pt.expense / maxVal).toFloat() * chartHeight * progress
                        if (outBarH > 0f) {
                            drawRoundRect(
                                color = expenseColor.copy(alpha = 0.85f),
                                topLeft = Offset(xCenter + 2f, chartHeight - outBarH),
                                size = androidx.compose.ui.geometry.Size(barWidth, outBarH),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                            )
                        }

                        // Selected indicator
                        if (selectedIndex == i) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.6f),
                                start = Offset(xCenter, 0f),
                                end = Offset(xCenter, chartHeight),
                                strokeWidth = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                            )
                        }
                    }
                }
            }

            // X-Axis labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                points.forEach { pt ->
                    Text(
                        text = pt.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
