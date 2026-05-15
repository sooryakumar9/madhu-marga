package com.example.madhu_marga_2.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.madhu_marga_2.data.AlertSeverity
import com.example.madhu_marga_2.data.Harvest
import com.example.madhu_marga_2.data.Hive
import com.example.madhu_marga_2.data.Inspection
import com.example.madhu_marga_2.logic.AnalyticsEngine
import com.example.madhu_marga_2.logic.DiagnosisResult
import com.example.madhu_marga_2.logic.RecommendationPriority
import com.example.madhu_marga_2.logic.RiskLevel
import com.example.madhu_marga_2.logic.ScoreComponent
import com.example.madhu_marga_2.viewmodel.HiveViewModel
import com.example.madhu_marga_2.viewmodel.TimelineEntry
import com.example.madhu_marga_2.viewmodel.TimelineType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiveDetailScreen(
    hiveId: Long,
    viewModel: HiveViewModel,
    onNavigateToInspections: () -> Unit,
    onNavigateToHarvests: () -> Unit,
    onBack: () -> Unit
) {
    val hives by viewModel.allHives.collectAsState(initial = emptyList())
    val hive = hives.find { it.id == hiveId }
    val inspections by viewModel.getInspectionsForHive(hiveId).collectAsState(initial = emptyList())
    val harvests by viewModel.getHarvestsForHive(hiveId).collectAsState(initial = emptyList())
    val diagnosis by viewModel.getDiagnosisForHive(hiveId).collectAsState(initial = null)
    val timeline by viewModel.getTimelineForHive(hiveId).collectAsState(initial = emptyList())

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "INTELLIGENCE UNIT",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            hive?.hiveId?.uppercase() ?: "FETCHING...",
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
        if (hive != null) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { visible = true }

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(600)) + slideInVertically(initialOffsetY = { 40 })
                ) {
                    Column {
                        HiveHeroSection(hive, inspections, diagnosis)
                        
                        AIDoctorSection(diagnosis)
                        
                        AnalyticsDashboardSection(harvests, inspections)
                        
                        IntelligenceTimelineSection(timeline)
                        
                        ManagementModule(onNavigateToInspections, onNavigateToHarvests)
                        Spacer(Modifier.height(48.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HiveHeroSection(hive: Hive, inspections: List<Inspection>, diagnosis: DiagnosisResult?) {
    val healthScore = diagnosis?.score ?: hive.lastHealthScore
    val trend = AnalyticsEngine.getHealthTrend(inspections)
    val isImproving = trend.size >= 2 && trend.last() >= trend[trend.size - 2]

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), Color.Transparent)
                    ),
                    CircleShape
                )
        )

        CircularHealthMonitor(score = healthScore)

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "VITALITY INDEX",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            val animatedScore by animateIntAsState(targetValue = healthScore, animationSpec = tween(1500), label = "score")
            Text(
                "${animatedScore}%",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-2).sp
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                val statusColor = when {
                    healthScore >= 85 -> MaterialTheme.colorScheme.tertiary 
                    healthScore >= 65 -> MaterialTheme.colorScheme.primary 
                    else -> MaterialTheme.colorScheme.error
                }
                
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.2f))
                ) {
                    Text(
                        diagnosis?.status?.uppercase() ?: "SCANNING",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
            
            if (trend.size > 1) {
                Spacer(Modifier.height(16.dp))
                Box(modifier = Modifier.width(100.dp).height(24.dp)) {
                    MiniLineChart(points = trend, color = if (isImproving) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AIDoctorSection(diagnosis: DiagnosisResult?) {
    if (diagnosis == null) return
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
        Text(
            "AI DIAGNOSTICS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(12.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "ANALYSIS: ${diagnosis.status.uppercase()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Confidence: ${diagnosis.confidenceScore}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
                
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
                        
                        diagnosis.forecast?.let { forecast ->
                            Spacer(Modifier.height(16.dp))
                            YieldForecastCard(forecast)
                        }

                        if (diagnosis.scoreBreakdown.isNotEmpty()) {
                            Spacer(Modifier.height(24.dp))
                            Text("VITALITY BREAKDOWN", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), letterSpacing = 1.sp)
                            Spacer(Modifier.height(12.dp))
                            diagnosis.scoreBreakdown.forEach { component ->
                                ScoreComponentRow(component)
                            }
                        }

                        if (diagnosis.risks.isNotEmpty()) {
                            Spacer(Modifier.height(24.dp))
                            Text("THREAT MATRIX", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f), letterSpacing = 1.sp)
                            Spacer(Modifier.height(8.dp))
                            diagnosis.risks.forEach { risk ->
                                RiskCard(risk)
                            }
                        }

                        if (diagnosis.insights.isNotEmpty()) {
                            Spacer(Modifier.height(24.dp))
                            Text("TELEMETRY INSIGHTS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), letterSpacing = 1.sp)
                            Spacer(Modifier.height(12.dp))
                            diagnosis.insights.forEach { insight ->
                                Surface(
                                    modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.03f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Insights, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                                        Spacer(Modifier.width(12.dp))
                                        Text(insight, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f))
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                        Text("ACTION PROTOCOLS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                        diagnosis.recommendations.forEach { recommendation ->
                            RecommendationRow(recommendation)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreComponentRow(component: ScoreComponent) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.padding(vertical = 8.dp).clickable { expanded = !expanded }) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(component.label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                if (expanded && component.description.isNotEmpty()) {
                    Text(component.description, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${component.current}/${component.max}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.width(8.dp))
                val statusColor = when(component.status) {
                    "Optimal" -> MaterialTheme.colorScheme.tertiary
                    "Degraded" -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.error
                }
                Box(modifier = Modifier.size(6.dp).background(statusColor, CircleShape))
            }
        }
        Spacer(Modifier.height(6.dp))
        val barColor = when(component.status) {
            "Optimal" -> MaterialTheme.colorScheme.tertiary
            "Degraded" -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.error
        }
        LinearProgressIndicator(
            progress = { component.current.toFloat() / component.max },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color = barColor,
            trackColor = barColor.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun YieldForecastCard(forecast: com.example.madhu_marga_2.logic.YieldForecast) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(52.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.QueryStats, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("PREDICTIVE YIELD", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                Text("Expected next harvest volume", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(String.format(Locale.getDefault(), "%.1f KG", forecast.expectedKg), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = if (forecast.trend == "RISING") Icons.AutoMirrored.Filled.TrendingUp else if (forecast.trend == "DECLINING") Icons.AutoMirrored.Filled.TrendingDown else Icons.Default.HorizontalRule
                    val color = if (forecast.trend == "RISING") MaterialTheme.colorScheme.tertiary else if (forecast.trend == "DECLINING") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                    Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = color)
                    Spacer(Modifier.width(4.dp))
                    Text(forecast.trend, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, fontWeight = FontWeight.Black, color = color)
                }
            }
        }
    }
}

@Composable
fun RiskCard(risk: com.example.madhu_marga_2.logic.HealthRisk) {
    Surface(
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.05f),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val iconColor = when(risk.level) {
                    RiskLevel.EXTREME -> Color(0xFFD32F2F)
                    RiskLevel.HIGH -> Color(0xFFF44336)
                    RiskLevel.MODERATE -> Color(0xFFFFA000)
                    else -> Color(0xFF388E3C)
                }
                Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp), tint = iconColor)
                Spacer(Modifier.width(12.dp))
                Text(risk.factor.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                Spacer(Modifier.weight(1f))
                Surface(color = iconColor.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                    Text(
                        risk.level.name, 
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall, 
                        fontSize = 9.sp,
                        color = iconColor, 
                        fontWeight = FontWeight.Black
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(risk.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun RecommendationRow(recommendation: com.example.madhu_marga_2.logic.Recommendation) {
    Surface(
        modifier = Modifier.padding(vertical = 6.dp).fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            val tint = when(recommendation.priority) {
                RecommendationPriority.CRITICAL -> MaterialTheme.colorScheme.error
                RecommendationPriority.HIGH -> MaterialTheme.colorScheme.secondary
                RecommendationPriority.MEDIUM -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.tertiary
            }
            Box(
                modifier = Modifier.size(32.dp).background(tint.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp), tint = tint)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(recommendation.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black)
                Text(recommendation.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun AnalyticsDashboardSection(harvests: List<Harvest>, inspections: List<Inspection>) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
        Text(
            "UNIT PERFORMANCE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(12.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AnalyticsCard(
                title = "YIELD VOLUMES",
                modifier = Modifier.weight(1f)
            ) {
                val yields = harvests.takeLast(7).map { it.quantity.toFloat() }
                MiniBarChart(values = yields, color = MaterialTheme.colorScheme.primary)
            }
            
            AnalyticsCard(
                title = "HEALTH DYNAMICS",
                modifier = Modifier.weight(1f)
            ) {
                val trend = AnalyticsEngine.getHealthTrend(inspections)
                MiniLineChart(points = trend, color = MaterialTheme.colorScheme.tertiary)
            }
        }
        
        Spacer(Modifier.height(12.dp))
        
        val prodScore = AnalyticsEngine.calculateProductivityScore(harvests)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { prodScore / 100f },
                        modifier = Modifier.size(40.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        strokeWidth = 3.dp,
                        trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    )
                    Text(prodScore.toString(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("PRODUCTIVITY INDEX", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text("Combined yield efficiency across active cycles.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun AnalyticsCard(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), letterSpacing = 1.sp)
            Spacer(Modifier.weight(1f))
            content()
        }
    }
}

@Composable
fun MiniLineChart(points: List<Float>, color: Color) {
    val animateProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animateProgress.animateTo(1f, animationSpec = tween(1000, easing = FastOutSlowInEasing))
    }
    
    Canvas(modifier = Modifier.fillMaxWidth().height(40.dp)) {
        if (points.isEmpty()) return@Canvas
        val path = Path()
        val width = size.width
        val height = size.height
        
        val effectivePoints = if (points.size == 1) listOf(points[0], points[0]) else points

        effectivePoints.forEachIndexed { index, p ->
            val x = index * (width / (effectivePoints.size - 1))
            val targetY = height - (p * height)
            val animatedY = height - ((height - targetY) * animateProgress.value)
            if (index == 0) path.moveTo(x, animatedY) else path.lineTo(x, animatedY)
        }
        drawPath(path, color, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
fun MiniBarChart(values: List<Float>, color: Color) {
    val animateProgress = remember { Animatable(0f) }
    LaunchedEffect(values) {
        animateProgress.animateTo(1f, animationSpec = tween(1000, easing = FastOutSlowInEasing))
    }

    Canvas(modifier = Modifier.fillMaxWidth().height(40.dp)) {
        if (values.isEmpty()) return@Canvas
        val width = size.width
        val height = size.height
        val max = (values.maxOrNull() ?: 1f).coerceAtLeast(1f)
        val barWidth = (width / values.size) * 0.6f
        val spacing = (width / values.size) * 0.4f
        
        values.forEachIndexed { index, v ->
            val targetHeight = (v / max) * height
            val animatedHeight = targetHeight * animateProgress.value
            drawRoundRect(
                color = color.copy(alpha = 0.6f),
                topLeft = Offset(index * (barWidth + spacing), height - animatedHeight),
                size = Size(barWidth, animatedHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
            )
        }
    }
}

@Composable
fun IntelligenceTimelineSection(timeline: List<TimelineEntry>) {
    val grouped = timeline.groupBy { 
        val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    Column(modifier = Modifier.padding(24.dp)) {
        Text(
            "UNIT TIMELINE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(20.dp))
        
        if (timeline.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    "INITIALIZING REGISTRY...", 
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        } else {
            grouped.forEach { (month, entries) ->
                Text(
                    month.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 16.dp),
                    letterSpacing = 1.sp
                )
                entries.forEachIndexed { index, entry ->
                    TimelineItemRow(entry, isLast = index == entries.size - 1 && month == grouped.keys.last())
                }
            }
        }
    }
}

@Composable
fun TimelineItemRow(entry: TimelineEntry, isLast: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    val dateStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(entry.timestamp))
    
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
            val color = when(entry.severity) {
                AlertSeverity.CRITICAL -> MaterialTheme.colorScheme.error
                AlertSeverity.WARNING -> MaterialTheme.colorScheme.secondary
                else -> when(entry.type) {
                    TimelineType.HARVEST -> MaterialTheme.colorScheme.primary
                    TimelineType.DIAGNOSIS -> MaterialTheme.colorScheme.tertiary
                    TimelineType.HEALTH_CHANGE -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.outline
                }
            }
            
            val icon = when(entry.type) {
                TimelineType.INSPECTION -> Icons.Default.FactCheck
                TimelineType.HARVEST -> Icons.Default.Scale
                TimelineType.ALERT -> Icons.Default.PriorityHigh
                TimelineType.DIAGNOSIS -> Icons.Default.Psychology
                TimelineType.HEALTH_CHANGE -> Icons.Default.Analytics
            }

            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
            }
            if (!isLast) {
                Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(
                    Brush.verticalGradient(listOf(color.copy(alpha = 0.4f), MaterialTheme.colorScheme.outline.copy(alpha = 0.05f)))
                ))
            }
        }
        
        Column(modifier = Modifier.padding(bottom = 24.dp, start = 16.dp).clickable { expanded = !expanded }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(dateStr.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), fontSize = 10.sp)
                Spacer(Modifier.width(12.dp))
                Text(entry.title.uppercase(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                Spacer(Modifier.weight(1f))
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(entry.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
            
            AnimatedVisibility(visible = expanded) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.padding(top = 10.dp).fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                           Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                           Spacer(Modifier.width(8.dp))
                           Text(
                                "EVENT LOG: ${entry.id.uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        val detailText = when(entry.type) {
                            TimelineType.INSPECTION -> "Full biological and environmental audit completed. Telemetry data has been processed by the AI Doctor."
                            TimelineType.HARVEST -> "Resource extraction successful. Productivity score has been updated in the global registry."
                            TimelineType.ALERT -> "System anomaly detected. Immediate action protocol recommended as per AI diagnostics."
                            TimelineType.DIAGNOSIS -> "Comprehensive health evaluation completed. Confidence level and vitality index calibrated."
                            TimelineType.HEALTH_CHANGE -> "Significant shift in colony status detected. Vitality index modified accordingly."
                        }
                        Text(
                            detailText,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CircularHealthMonitor(score: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Canvas(modifier = Modifier.size(240.dp)) {
        val strokeWidth = 1.dp.toPx()
        drawArc(
            color = Color.Black.copy(alpha = 0.05f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )
        drawArc(
            color = primaryColor,
            startAngle = -90f,
            sweepAngle = (score / 100f) * 360f,
            useCenter = false,
            style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
        )
        rotate(rotation) {
            drawArc(
                color = secondaryColor.copy(alpha = 0.2f),
                startAngle = 0f,
                sweepAngle = 60f,
                useCenter = false,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun ManagementModule(onInspect: () -> Unit, onHarvest: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onInspect,
            modifier = Modifier.weight(1f).height(64.dp),
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 4.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.FactCheck, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text("DIAGNOSE", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }
        Button(
            onClick = onHarvest,
            modifier = Modifier.weight(1f).height(64.dp),
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.secondary),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 4.dp)
        ) {
            Icon(Icons.Default.Scale, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text("HARVEST", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }
    }
}
