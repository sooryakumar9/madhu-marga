package com.example.madhu_marga_2.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.madhu_marga_2.data.Inspection
import com.example.madhu_marga_2.viewmodel.HiveViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionLogScreen(
    hiveId: Long,
    viewModel: HiveViewModel,
    onBack: () -> Unit
) {
    val inspections by viewModel.getInspectionsForHive(hiveId).collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "DIAGNOSTIC ARCHIVE",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            "Colony Intelligence Log",
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp),
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(Icons.Default.AddModerator, contentDescription = "Add Inspection")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (inspections.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Dns,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "NO TELEMETRY LOGGED", 
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(24.dp)
                ) {
                    items(inspections.reversed()) { inspection ->
                        AdvancedInspectionCard(inspection)
                    }
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }

        if (showAddDialog) {
            AdvancedInspectionWorkflow(
                onDismiss = { showAddDialog = false },
                onSave = { queen, pests, activity, notes, temp, honey, brood ->
                    viewModel.addInspection(hiveId, queen, pests, activity, notes, temp, honey, brood)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun AdvancedInspectionCard(inspection: Inspection) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dateStr = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date(inspection.date))
                Text(
                    dateStr.uppercase(), 
                    style = MaterialTheme.typography.labelSmall, 
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
                
                StatusTag(inspection.activityLevel)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Core Subsystems
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SubsystemIndicator("QUEEN", inspection.isQueenPresent, Modifier.weight(1f))
                SubsystemIndicator("PESTS", !inspection.pestsSeen, Modifier.weight(1f))
                SubsystemIndicator("BROOD", inspection.broodCondition != "Poor", Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(20.dp))
            
            // Environmental & Production
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoBlock("INTERNAL TEMP", String.format(Locale.getDefault(), "%.1f°C", inspection.temperature))
                InfoBlock("HONEY FLOW", inspection.honeyFlow.uppercase())
            }
            
            if (inspection.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(20.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.03f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "COMMANDER NOTES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = inspection.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusTag(level: String) {
    val color = when (level) {
        "High" -> MaterialTheme.colorScheme.tertiary
        "Medium" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.secondary
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Text(
            level.uppercase(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            fontSize = 9.sp
        )
    }
}

@Composable
fun SubsystemIndicator(label: String, active: Boolean, modifier: Modifier = Modifier) {
    val color = if (active) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            label, 
            style = MaterialTheme.typography.labelSmall, 
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            fontSize = 9.sp
        )
    }
}

@Composable
fun InfoBlock(label: String, value: String) {
    Column {
        Text(
            label, 
            style = MaterialTheme.typography.labelSmall, 
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )
        Text(
            value, 
            style = MaterialTheme.typography.bodyMedium, 
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AdvancedInspectionWorkflow(
    onDismiss: () -> Unit,
    onSave: (Boolean, Boolean, String, String, Double, String, String) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    
    // Form State
    var queenPresent by remember { mutableStateOf(true) }
    var pestsSeen by remember { mutableStateOf(false) }
    var activityLevel by remember { mutableStateOf("Medium") }
    var temperature by remember { mutableFloatStateOf(34.0f) }
    var honeyFlow by remember { mutableStateOf("Normal") }
    var broodCondition by remember { mutableStateOf("Healthy") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        title = {
            Column {
                Text(
                    "DIAGNOSTIC PROTOCOL", 
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text("Module $step of 3", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                when (step) {
                    1 -> {
                        // Biological Status
                        ProtocolToggle("QUEEN DETECTED", queenPresent) { queenPresent = it }
                        ProtocolToggle("ANOMALIES / PESTS", pestsSeen) { pestsSeen = it }
                        
                        Column {
                            Text(
                                "FORAGER ACTIVITY", 
                                style = MaterialTheme.typography.labelSmall, 
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.height(12.dp))
                            SegmentedControl(
                                options = listOf("Low", "Medium", "High"),
                                selected = activityLevel,
                                onSelected = { activityLevel = it }
                            )
                        }
                    }
                    2 -> {
                        // Environmental & Interior
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    "CORE TEMPERATURE", 
                                    style = MaterialTheme.typography.labelSmall, 
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    letterSpacing = 1.sp
                                )
                                Text(String.format(Locale.getDefault(), "%.1f°C", temperature), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                            }
                            Slider(
                                value = temperature,
                                onValueChange = { temperature = it },
                                valueRange = 20f..45f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                )
                            )
                        }
                        
                        Column {
                            Text(
                                "HONEY FLOW RATE", 
                                style = MaterialTheme.typography.labelSmall, 
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.height(12.dp))
                            SegmentedControl(
                                options = listOf("Low", "Normal", "High"),
                                selected = honeyFlow,
                                onSelected = { honeyFlow = it }
                            )
                        }
                        
                        Column {
                            Text(
                                "BROOD VIABILITY", 
                                style = MaterialTheme.typography.labelSmall, 
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.height(12.dp))
                            SegmentedControl(
                                options = listOf("Poor", "Healthy", "Excellent"),
                                selected = broodCondition,
                                onSelected = { broodCondition = it }
                            )
                        }
                    }
                    3 -> {
                        // Analysis Override
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("COMMANDER NOTES", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                            placeholder = { Text("Describe specific observations...", style = MaterialTheme.typography.bodySmall) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            textStyle = MaterialTheme.typography.bodySmall,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (step < 3) step++ else onSave(queenPresent, pestsSeen, activityLevel, notes, temperature.toDouble(), honeyFlow, broodCondition)
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    if (step < 3) "CONTINUE PROTOCOL" else "COMMIT LOG", 
                    fontWeight = FontWeight.Black, 
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }
        },
        dismissButton = {
            if (step > 1) {
                TextButton(onClick = { step-- }, modifier = Modifier.fillMaxWidth()) {
                    Text("BACK", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                }
            } else {
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("ABORT", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            }
        },
        shape = RoundedCornerShape(32.dp)
    )
}

@Composable
fun ProtocolToggle(label: String, active: Boolean, onToggle: (Boolean) -> Unit) {
    Surface(
        onClick = { onToggle(!active) },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label, 
                style = MaterialTheme.typography.labelSmall, 
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 0.5.sp
            )
            Switch(
                checked = active, 
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.tertiary,
                    checkedTrackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

@Composable
fun SegmentedControl(options: List<String>, selected: String, onSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onSelected(option) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
