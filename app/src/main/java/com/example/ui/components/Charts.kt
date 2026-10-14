package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryExpenseSummary
import com.example.data.model.MonthlyFlowSummary
import com.example.data.model.PaymentModeSummary
import com.example.ui.theme.ColorExpenseRed
import com.example.ui.theme.ColorExpenseRedDark
import com.example.ui.theme.ColorReceivedGreen
import com.example.ui.theme.ColorReceivedGreenDark
import com.example.ui.theme.LocalThemeIsDark
import com.example.ui.theme.SapphireNavy
import com.example.ui.theme.SocietyTealPrimary
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

val CategoryChartColors = listOf(
    Color(0xFFE53935), // Tent & Decoration (Red)
    Color(0xFFFB8C00), // Sound System & Mic (Amber/Orange)
    Color(0xFF1E88E5), // Electricity & Generator (Blue)
    Color(0xFF43A047), // Priest Dakshina & Puja (Green)
    Color(0xFF8E24AA), // Sweets & Prasadam (Purple)
    Color(0xFF00ACC1), // Cleaning & Sanitation (Cyan)
    Color(0xFF5D4037), // Miscellaneous (Brown)
    Color(0xFFD81B60), // Deep Pink
    Color(0xFF3949AB)  // Indigo
)

// High-contrast, vibrant palette for Dark Mode visibility
val CategoryChartColorsDark = listOf(
    Color(0xFFFF6E6E), // Tent & Decoration (Vibrant Coral Red)
    Color(0xFFFFB74D), // Sound System & Mic (Warm Amber)
    Color(0xFF64B5F6), // Electricity & Generator (Sky Blue)
    Color(0xFF81C784), // Priest Dakshina & Puja (Soft Mint Green)
    Color(0xFFBA68C8), // Sweets & Prasadam (Lilac Purple)
    Color(0xFF4DD0E1), // Cleaning & Sanitation (Bright Cyan)
    Color(0xFFA1887F), // Miscellaneous (Warm Taupe)
    Color(0xFFF06292), // Radiant Pink
    Color(0xFF7986CB)  // Periwinkle Indigo
)

/**
 * Recharts-style interactive Monthly Inflow vs Outflow Dual-Bar Chart.
 * Features:
 * - Animated bar growth with spring physics
 * - Subtle grid line axes
 * - Interactive tap selection with custom Tooltip Card showing Inflow, Outflow & Net
 * - Legend with color identifiers
 */
@Composable
fun MonthlyFlowBarChart(
    monthlyData: List<MonthlyFlowSummary>,
    modifier: Modifier = Modifier
) {
    var selectedMonthIndex by remember { mutableStateOf<Int?>(null) }
    val isDark = LocalThemeIsDark.current

    val inflowColor = if (isDark) ColorReceivedGreenDark else ColorReceivedGreen
    val outflowColor = if (isDark) ColorExpenseRedDark else ColorExpenseRed
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_flow_chart_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Title & Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Monthly Inflow vs. Outflow",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap any bar to inspect monthly analytics",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Recharts-style Legend Tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(inflowColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Inflow (₹)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(outflowColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Outflow (₹)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (monthlyData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transaction history available yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val maxVal = max(
                    1.0,
                    monthlyData.maxOfOrNull { max(it.inflow, it.outflow) } ?: 1.0
                )

                var animationTriggered by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { animationTriggered = true }

                val animatedProgress by animateFloatAsState(
                    targetValue = if (animationTriggered) 1f else 0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    label = "chartAnim"
                )

                val gridLineColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.45f)

                // Chart Canvas with Gridlines & Bars
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                ) {
                    // Background Grid Lines
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val gridCount = 4
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        for (i in 0..gridCount) {
                            val y = size.height * (i.toFloat() / gridCount.toFloat())
                            drawLine(
                                color = gridLineColor,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = dashEffect
                            )
                        }
                    }

                    // Interactive Bars
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        monthlyData.forEachIndexed { index, item ->
                            val isSelected = selectedMonthIndex == index

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable {
                                        selectedMonthIndex = if (selectedMonthIndex == index) null else index
                                    }
                                    .padding(horizontal = 4.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                ) {
                                    // Inflow Bar
                                    val inflowRatio = (item.inflow / maxVal).toFloat().coerceIn(0.04f, 1f) * animatedProgress
                                    val inflowBarHeight = (120 * inflowRatio).dp

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(inflowBarHeight)
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        if (isSelected) Color(0xFF00E676) else inflowColor,
                                                        if (isSelected) Color(0xFF00C853) else if (isDark) Color(0xFF2E7D32) else Color(0xFF1B5E20)
                                                    )
                                                )
                                            )
                                    )

                                    // Outflow Bar
                                    val outflowRatio = (item.outflow / maxVal).toFloat().coerceIn(0.04f, 1f) * animatedProgress
                                    val outflowBarHeight = (120 * outflowRatio).dp

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(outflowBarHeight)
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        if (isSelected) Color(0xFFFF5252) else outflowColor,
                                                        if (isSelected) Color(0xFFD50000) else if (isDark) Color(0xFFC62828) else Color(0xFF8E0000)
                                                    )
                                                )
                                            )
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Month Label with indicator
                                Text(
                                    text = item.monthName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                                    color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Selected Month Interactive Recharts-style Tooltip Card
                AnimatedVisibility(
                    visible = selectedMonthIndex != null && selectedMonthIndex in monthlyData.indices,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    selectedMonthIndex?.let { index ->
                        val data = monthlyData.getOrNull(index)
                        if (data != null) {
                            val net = data.inflow - data.outflow
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp)),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${data.monthName} Financial Summary",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Inflow: ₹${String.format("%,.0f", data.inflow)}  •  Outflow: ₹${String.format("%,.0f", data.outflow)}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (net >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                            contentDescription = null,
                                            tint = if (net >= 0) inflowColor else outflowColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${if (net >= 0) "+₹" else "-₹"}${String.format("%,.0f", kotlin.math.abs(net))}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (net >= 0) inflowColor else outflowColor
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
}

/**
 * Recharts-style interactive Pie & Donut Chart for Expense Breakdown by Category.
 * Features:
 * - Interactive slice selection that highlights percentage and amount in the central aperture
 * - Clean SVG/Canvas arc rendering with animated sweep
 * - Legend checklist with proportion chips
 */
@Composable
fun ExpensesCategoryDonutChart(
    categories: List<CategoryExpenseSummary>,
    modifier: Modifier = Modifier
) {
    var selectedCategoryIndex by remember { mutableStateOf<Int?>(null) }
    val isDark = LocalThemeIsDark.current
    val palette = if (isDark) CategoryChartColorsDark else CategoryChartColors

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("expenses_pie_chart_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PieChart,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFFFFB74D) else Color(0xFFFB8C00),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Expense Breakdown by Category",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Category distribution and budget shares",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (categories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No expense records logged for this period", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val totalAmount = categories.sumOf { it.amount }

                var animationTriggered by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { animationTriggered = true }

                val animatedProgress by animateFloatAsState(
                    targetValue = if (animationTriggered) 1f else 0f,
                    animationSpec = tween(durationMillis = 900),
                    label = "donutAnim"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Interactive Donut Canvas (Left)
                    Box(
                        modifier = Modifier
                            .size(145.dp)
                            .testTag("pie_chart_canvas"),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(135.dp)) {
                            var startAngle = -90f
                            val baseStrokeWidth = 26.dp.toPx()

                            categories.forEachIndexed { index, cat ->
                                val sweepAngle = (cat.percentage * 3.6f) * animatedProgress
                                val isSelected = selectedCategoryIndex == index
                                val color = palette[index % palette.size]
                                val currentStroke = if (isSelected) baseStrokeWidth + 8.dp.toPx() else baseStrokeWidth

                                drawArc(
                                    color = if (selectedCategoryIndex != null && !isSelected) color.copy(alpha = 0.35f) else color,
                                    startAngle = startAngle + 1.5f,
                                    sweepAngle = (sweepAngle - 3f).coerceAtLeast(0.5f),
                                    useCenter = false,
                                    style = Stroke(width = currentStroke, cap = StrokeCap.Round)
                                )
                                startAngle += sweepAngle
                            }
                        }

                        // Central Aperture Callout
                        val activeCategory = selectedCategoryIndex?.let { categories.getOrNull(it) }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { selectedCategoryIndex = null }
                                .padding(4.dp)
                        ) {
                            if (activeCategory != null) {
                                Text(
                                    text = "${activeCategory.percentage.toInt()}%",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = palette[selectedCategoryIndex!! % palette.size]
                                )
                                Text(
                                    text = activeCategory.category,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            } else {
                                Text(
                                    text = "₹${String.format("%,.0f", totalAmount / 1000)}k",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Total Spent",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Category Interactive Legend (Right)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.take(6).forEachIndexed { index, cat ->
                            val color = palette[index % palette.size]
                            val isSelected = selectedCategoryIndex == index

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) color.copy(alpha = 0.18f) else Color.Transparent
                                    )
                                    .clickable {
                                        selectedCategoryIndex = if (selectedCategoryIndex == index) null else index
                                    }
                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(color, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = cat.category,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }
                                Text(
                                    text = "₹${String.format("%,.0f", cat.amount)} (${cat.percentage.toInt()}%)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Payment Mode Breakdown Card.
 */
@Composable
fun PaymentModeBreakdownCard(
    modeData: List<PaymentModeSummary>,
    modifier: Modifier = Modifier
) {
    val isDark = LocalThemeIsDark.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("payment_mode_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Collection by Payment Mode",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "UPI, Bank Transfer, and Cash split",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            val total = modeData.sumOf { it.amount }

            if (modeData.isEmpty() || total == 0.0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No collection records yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    modeData.forEach { item ->
                        val pct = if (total > 0) (item.amount / total).toFloat() else 0f
                        val modeColor = when {
                            item.mode.contains("UPI", ignoreCase = true) -> if (isDark) Color(0xFFB39DDB) else Color(0xFF5E35B1)
                            item.mode.contains("Cash", ignoreCase = true) -> if (isDark) Color(0xFF80CBC4) else SocietyTealPrimary
                            item.mode.contains("Bank", ignoreCase = true) -> if (isDark) Color(0xFF90CAF9) else Color(0xFF1565C0)
                            else -> if (isDark) Color(0xFFBCAAA4) else Color(0xFF6D4C41)
                        }

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.mode,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "₹ ${String.format("%,.0f", item.amount)} (${(pct * 100).toInt()}%)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = modeColor
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(9.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(pct)
                                        .fillMaxHeight()
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(modeColor.copy(alpha = 0.75f), modeColor)
                                            ),
                                            RoundedCornerShape(5.dp)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
