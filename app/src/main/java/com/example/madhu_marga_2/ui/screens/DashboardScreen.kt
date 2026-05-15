package com.example.madhu_marga_2.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.madhu_marga_2.data.AlertSeverity
import com.example.madhu_marga_2.data.SmartAlert
import com.example.madhu_marga_2.logic.AnalyticsEngine
import com.example.madhu_marga_2.logic.Recommendation
import com.example.madhu_marga_2.logic.RecommendationPriority
import com.example.madhu_marga_2.viewmodel.HiveViewModel
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: HiveViewModel,
    onNavigateToFlora: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToHives: () -> Unit,
    onAddHive: () -> Unit
) {
    val totalHives by viewModel.totalHivesCount.collectAsState()
    val totalHoney by viewModel.totalHoneyCollected.collectAsState()
    val smartAlerts by viewModel.smartAlerts.collectAsState()
    val healthScore by viewModel.apiaryHealthScore.collectAsState()
    val pendingInspections by viewModel.pendingInspectionsCount.collectAsState()
    
    val allHives by viewModel.allHives.collectAsState(initial = emptyList())
    val allInspections by viewModel.allInspections.collectAsState()
    val allHarvests by viewModel.allHarvests.collectAsState(initial = emptyList())

    val globalAdvisory = remember(allHives, allInspections, allHarvests) {
        AnalyticsEngine.getGlobalAdvisory(allHives, allInspections, allHarvests)
    }

    val bgColor = MaterialTheme.colorScheme.background
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.06f), Color.Transparent),
                    center = Offset(size.width * 0.15f, size.height * 0.05f),
                    radius = size.width * 0.8f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(secondaryColor.copy(alpha = 0.04f), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.35f),
                    radius = size.width * 0.7f
                )
            )
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "APIARY OVERVIEW",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Text(
                                "Madhu-Marga Systems",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onNavigateToAlerts,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            BadgedBox(
                                badge = {
                                    if (smartAlerts.isNotEmpty()) {
                                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                                            Text(smartAlerts.size.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Outlined.NotificationsNone, contentDescription = "Alerts")
                            }
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
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item { OperationalHeader() }

                item {
                    MetricsGrid(
                        hives = totalHives,
                        honey = totalHoney,
                        alerts = smartAlerts.size,
                        pending = pendingInspections
                    )
                }

                item { HealthInterface(healthScore) }

                if (smartAlerts.isNotEmpty()) {
                    item {
                        IntelligenceFeed(smartAlerts.take(3), onNavigateToAlerts)
                    }
                }

                item { SmartRecommendation(advisory = globalAdvisory, onNavigate = onNavigateToFlora) }

                item { ActionDock(onAddHive = onAddHive, onAnalyze = onNavigateToHives) }
            }
        }
    }
}

@Composable
fun OperationalHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "SYSTEM STATUS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "alpha"
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = alpha), CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "COLONY MONITOR ACTIVE",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        WeatherModule()
    }
}

@Composable
fun WeatherModule() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.WbSunny, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("28°C", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MetricsGrid(hives: Int, honey: Double, alerts: Int, pending: Int) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile("HIVES", hives.toString(), Icons.Default.Hive, Modifier.weight(1f))
            MetricTile("YIELD", String.format(Locale.getDefault(), "%.1fkg", honey), Icons.Default.Scale, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile("PENDING", pending.toString(), Icons.AutoMirrored.Filled.FactCheck, Modifier.weight(1f))
            MetricTile("ALERTS", alerts.toString(), Icons.Default.NotificationImportant, Modifier.weight(1f), isCritical = alerts > 0)
        }
    }
}

@Composable
fun MetricTile(label: String, value: String, icon: ImageVector, modifier: Modifier, isCritical: Boolean = false) {
    val containerColor = if (isCritical) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    val accentColor = if (isCritical) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier,
        color = containerColor,
        shape = RoundedCornerShape(28.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HealthInterface(score: Int) {
    val healthColor = if (score > 80) MaterialTheme.colorScheme.tertiary else if (score > 50) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
    val outlineColor = MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.size(280.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(220.dp)) {
                val strokeWidth = 1.dp.toPx()
                drawArc(
                    color = outlineColor.copy(alpha = 0.1f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )
                drawArc(
                    color = healthColor,
                    startAngle = -90f,
                    sweepAngle = (score / 100f) * 360f,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(healthColor.copy(alpha = 0.12f), Color.Transparent),
                        radius = size.minDimension / 2
                    )
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "APIARY HEALTH",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "$score%",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-2).sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(14.dp))
                    Text(" NOMINAL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Black)
                }
            }
            
            MiniMetricAround(label = "TEMP", value = "32°C", angle = -50f, radius = 130.dp, icon = Icons.Default.Thermostat)
            MiniMetricAround(label = "HUMID", value = "64%", angle = 50f, radius = 130.dp, icon = Icons.Default.WaterDrop)
            MiniMetricAround(label = "BLOOM", value = "HIGH", angle = 130f, radius = 130.dp, icon = Icons.Default.LocalFlorist)
            MiniMetricAround(label = "BIO", value = "98%", angle = 230f, radius = 130.dp, icon = Icons.Default.Eco)
        }
    }
}

@Composable
fun MiniMetricAround(label: String, value: String, angle: Float, radius: androidx.compose.ui.unit.Dp, icon: ImageVector) {
    val angleRad = Math.toRadians(angle.toDouble())
    val xOffset = (radius.value * cos(angleRad)).dp
    val yOffset = (radius.value * sin(angleRad)).dp

    Surface(
        modifier = Modifier.offset(x = xOffset, y = yOffset),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        shadowElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
            Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, fontSize = 10.sp)
        }
    }
}

@Composable
fun IntelligenceFeed(alerts: List<SmartAlert>, onNavigate: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "SMART ALERTS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary
            )
            TextButton(onClick = onNavigate) {
                Text("VIEW ALL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
        
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = RoundedCornerShape(28.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                alerts.forEach { alert ->
                    AlertItemRow(alert)
                }
            }
        }
    }
}

@Composable
fun AlertItemRow(alert: SmartAlert) {
    val color = when (alert.severity) {
        AlertSeverity.CRITICAL -> MaterialTheme.colorScheme.error
        AlertSeverity.WARNING -> MaterialTheme.colorScheme.secondary
        AlertSeverity.NORMAL -> MaterialTheme.colorScheme.primary
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(alert.title.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = color, letterSpacing = 0.5.sp)
            Text(alert.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun SmartRecommendation(advisory: Recommendation?, onNavigate: () -> Unit) {
    val displayTitle = advisory?.title ?: "SYSTEM ADVISORY"
    val displayDesc = advisory?.description ?: "No critical actions pending. Optimal nectar flow predicted in local sector."
    val color = when(advisory?.priority) {
        RecommendationPriority.CRITICAL -> MaterialTheme.colorScheme.error
        RecommendationPriority.HIGH -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        onClick = onNavigate,
        modifier = Modifier.padding(24.dp).fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.15f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (advisory != null) Icons.Default.Lightbulb else Icons.Default.AutoAwesome, 
                    contentDescription = null, 
                    tint = color, 
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(displayTitle.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = color, letterSpacing = 1.sp)
                Text(displayDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun ActionDock(onAddHive: () -> Unit, onAnalyze: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onAddHive,
            modifier = Modifier.weight(1f).height(60.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 2.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("DEPLOY", fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
        }
        Button(
            onClick = onAnalyze,
            modifier = Modifier.weight(1f).height(60.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.secondary),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 2.dp)
        ) {
            Icon(Icons.Default.QueryStats, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("INVENTORY", fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
        }
    }
}
