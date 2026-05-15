package com.example.madhu_marga_2.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.madhu_marga_2.data.AlertSeverity
import com.example.madhu_marga_2.data.Harvest
import com.example.madhu_marga_2.data.Hive
import com.example.madhu_marga_2.data.Inspection
import com.example.madhu_marga_2.logic.AnalyticsEngine
import com.example.madhu_marga_2.viewmodel.HiveViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: HiveViewModel, onBack: () -> Unit) {
    val hives by viewModel.allHives.collectAsState(initial = emptyList())
    val harvests by viewModel.allHarvests.collectAsState(initial = emptyList())
    val alerts by viewModel.smartAlerts.collectAsState()
    val allInspections by viewModel.allInspections.collectAsState()
    val performanceMatrix by viewModel.performanceMatrix.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "SYSTEM ANALYTICS",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            "Global Intelligence Dashboard",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                AnalyticsHeader("PRODUCTION PERFORMANCE")
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                ) {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("HONEY YIELD TREND (KG)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = primaryColor)
                        Spacer(Modifier.height(24.dp))
                        LargeProductionChart(harvests, primaryColor)
                    }
                }
            }

            item {
                AnalyticsHeader("PERFORMANCE MATRIX")
                PerformanceMatrixGrid(performanceMatrix)
            }

            item {
                AnalyticsHeader("UNIT PRODUCTIVITY COMPARISON")
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        val comparison = AnalyticsEngine.getComparisonMatrix(hives, harvests)
                        if (comparison.isEmpty()) {
                            Text("No telemetry available for comparison.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.padding(16.dp))
                        } else {
                            ComparisonBarChart(comparison.take(5))
                            Spacer(Modifier.height(16.dp))
                            comparison.take(5).forEachIndexed { index, item ->
                                ComparisonRow(index + 1, item.first, item.second)
                                if (index < comparison.take(5).size - 1) HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
                            }
                        }
                    }
                }
            }

            item {
                AnalyticsHeader("INTELLIGENCE HEALTH")
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AlertDistributionCard(alerts, Modifier.weight(1f))
                    ProductivityScoreCard(harvests, Modifier.weight(1f))
                }
            }

            item {
                AnalyticsHeader("OPERATIONAL VITALITY")
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("GLOBAL HEALTH INDEX", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                            
                            val freq = AnalyticsEngine.getInspectionFrequency(allInspections)
                            Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                                Text(
                                    String.format(Locale.getDefault(), "%.1f INSP/WK", freq),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        val trend = AnalyticsEngine.getHealthTrend(allInspections)
                        LargeHealthTrendChart(trend, MaterialTheme.colorScheme.tertiary)
                    }
                }
            }
            
            item {
                AnalyticsHeader("AUDIT FREQUENCY")
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("WEEKLY INSPECTION LOGS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(24.dp))
                        val trends = AnalyticsEngine.getInspectionTrends(allInspections)
                        InspectionFrequencyChart(trends, MaterialTheme.colorScheme.primary)
                    }
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun PerformanceMatrixGrid(matrix: Map<String, Int>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MatrixCard("ELITE UNITS", matrix["ELITE UNITS"] ?: 0, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
            MatrixCard("DEVELOPING", matrix["DEVELOPING"] ?: 0, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MatrixCard("STRAINED", matrix["STRAINED"] ?: 0, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
            MatrixCard("CRITICAL", matrix["CRITICAL"] ?: 0, MaterialTheme.colorScheme.error, Modifier.weight(1f))
        }
    }
}

@Composable
fun MatrixCard(label: String, count: Int, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = color, letterSpacing = 1.sp)
            Spacer(Modifier.height(8.dp))
            Text(count.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
fun ComparisonBarChart(data: List<Pair<String, Double>>) {
    val maxVal = data.maxOfOrNull { it.second }?.toFloat()?.coerceAtLeast(1f) ?: 1f
    val animateProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animateProgress.animateTo(1f, animationSpec = tween(1200, easing = FastOutSlowInEasing))
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        data.forEach { (id, yield) ->
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(id.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${yield.toInt()}kg", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
                    Box(modifier = Modifier
                        .fillMaxWidth(fraction = (yield.toFloat() / maxVal) * animateProgress.value)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))))
                    )
                }
            }
        }
    }
}

@Composable
fun ComparisonRow(rank: Int, hiveId: String, yield: Double) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(24.dp).background(
            if (rank == 1) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), 
            CircleShape), 
            contentAlignment = Alignment.Center
        ) {
            Text(rank.toString(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = if (rank == 1) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.width(16.dp))
        Text(hiveId.uppercase(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        Text("${yield.toInt()}kg", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun AnalyticsHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.5.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
fun LargeProductionChart(harvests: List<Harvest>, color: Color) {
    val points = harvests.takeLast(12).map { it.quantity.toFloat() }
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(points) {
        animationProgress.animateTo(1f, animationSpec = tween(1200, easing = FastOutSlowInEasing))
    }

    Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
        if (points.isEmpty()) return@Canvas
        val path = Path()
        val fillPath = Path()
        val width = size.width
        val height = size.height
        val max = (points.maxOrNull() ?: 1f).coerceAtLeast(1f)
        
        points.forEachIndexed { index, p ->
            val x = if (points.size > 1) index * (width / (points.size - 1)) else width / 2
            val targetY = height - (p / max * height)
            val animatedY = height - ((height - targetY) * animationProgress.value)
            
            if (index == 0) {
                path.moveTo(x, animatedY)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, animatedY)
            } else {
                path.lineTo(x, animatedY)
                fillPath.lineTo(x, animatedY)
            }
            if (index == points.size - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }
        }

        drawPath(
            fillPath, 
            brush = Brush.verticalGradient(listOf(color.copy(alpha = 0.3f), Color.Transparent)),
            alpha = animationProgress.value
        )
        drawPath(path, color, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
        
        points.forEachIndexed { index, p ->
            val x = if (points.size > 1) index * (width / (points.size - 1)) else width / 2
            val targetY = height - (p / max * height)
            val animatedY = height - ((height - targetY) * animationProgress.value)
            drawCircle(color, radius = 5.dp.toPx(), center = Offset(x, animatedY))
            drawCircle(Color.White, radius = 2.dp.toPx(), center = Offset(x, animatedY))
        }
    }
}

@Composable
fun LargeHealthTrendChart(trend: List<Float>, color: Color) {
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(trend) {
        animationProgress.animateTo(1f, animationSpec = tween(1500, easing = FastOutSlowInEasing))
    }

    Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
        if (trend.isEmpty()) return@Canvas
        val path = Path()
        val width = size.width
        val height = size.height
        
        trend.forEachIndexed { index, p ->
            val x = if (trend.size > 1) index * (width / (trend.size - 1)) else width / 2
            val targetY = height - (p * height)
            val animatedY = height - ((height - targetY) * animationProgress.value)
            
            if (index == 0) path.moveTo(x, animatedY) else {
                val prevX = (index - 1) * (width / (trend.size - 1))
                val prevP = trend[index - 1]
                val prevY = height - ((height - (height - prevP * height)) * animationProgress.value)
                path.cubicTo(
                    prevX + (x - prevX) / 2, prevY,
                    prevX + (x - prevX) / 2, animatedY,
                    x, animatedY
                )
            }
        }

        drawPath(path, color, style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round))
        
        val dashPath = Path().apply {
            moveTo(0f, height * 0.75f)
            lineTo(width, height * 0.75f)
        }
        drawPath(dashPath, color.copy(alpha = 0.1f), style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)))
    }
}

@Composable
fun InspectionFrequencyChart(trends: List<Int>, color: Color) {
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(trends) {
        animationProgress.animateTo(1f, animationSpec = tween(1000, easing = FastOutSlowInEasing))
    }

    Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
        if (trends.isEmpty()) return@Canvas
        val width = size.width
        val height = size.height
        val max = (trends.maxOrNull() ?: 1).toFloat().coerceAtLeast(1f)
        val barWidth = (width / trends.size) * 0.7f
        val spacing = (width / trends.size) * 0.3f
        
        trends.forEachIndexed { index, v ->
            val targetHeight = (v / max) * height
            val animatedHeight = targetHeight * animationProgress.value
            drawRoundRect(
                color = color.copy(alpha = 0.7f),
                topLeft = Offset(index * (barWidth + spacing), height - animatedHeight),
                size = Size(barWidth, animatedHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
            )
            
            if (v > 0) {
                drawRect(
                    color = color,
                    topLeft = Offset(index * (barWidth + spacing), height - animatedHeight),
                    size = Size(barWidth, 4.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun AlertDistributionCard(alerts: List<com.example.madhu_marga_2.data.SmartAlert>, modifier: Modifier) {
    val freq = AnalyticsEngine.getAlertFrequency(alerts)
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(alerts) {
        animationProgress.animateTo(1f, animationSpec = tween(1500, easing = FastOutSlowInEasing))
    }

    Surface(
        modifier = modifier.height(180.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("ALERT DENSITY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(Modifier.weight(1f))
            Box(contentAlignment = Alignment.Center) {
                val total = alerts.size.coerceAtLeast(1)
                Canvas(modifier = Modifier.size(70.dp)) {
                    var startAngle = -90f
                    for (severity in AlertSeverity.entries) {
                        val count = freq[severity] ?: 0
                        val sweepAngle = (count.toFloat() / total) * 360f * animationProgress.value
                        val color = when(severity) {
                            AlertSeverity.CRITICAL -> Color(0xFFE53935)
                            AlertSeverity.WARNING -> Color(0xFFFFB300)
                            else -> Color(0xFF43A047)
                        }
                        if (sweepAngle > 0) {
                            drawArc(color, startAngle, sweepAngle, false, style = Stroke(10.dp.toPx(), cap = StrokeCap.Round))
                        }
                        startAngle += sweepAngle
                    }
                    if (alerts.isEmpty()) {
                        drawCircle(Color.Gray.copy(alpha = 0.1f), style = Stroke(10.dp.toPx()))
                    }
                }
                Text(alerts.size.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.weight(1f))
            Text("DETECTION VOLUME", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun ProductivityScoreCard(harvests: List<Harvest>, modifier: Modifier) {
    val score = AnalyticsEngine.calculateProductivityScore(harvests)
    val primaryColor = MaterialTheme.colorScheme.primary
    val animatedScore = animateIntAsState(targetValue = score, animationSpec = tween(1000), label = "score")

    Surface(
        modifier = modifier.height(180.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("PRODUCTIVITY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(Modifier.weight(1f))
            Text("${animatedScore.value}", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black, color = primaryColor)
            Text("INDEX SCORE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            Spacer(Modifier.weight(1f))
            LinearProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                color = primaryColor,
                trackColor = primaryColor.copy(alpha = 0.1f)
            )
        }
    }
}
