package com.example.madhu_marga_2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.madhu_marga_2.data.*
import com.example.madhu_marga_2.logic.AnalyticsEngine
import com.example.madhu_marga_2.logic.DiagnosisResult
import com.example.madhu_marga_2.logic.HiveDiagnosisEngine
import com.example.madhu_marga_2.logic.Recommendation
import com.example.madhu_marga_2.logic.RiskLevel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class TimelineEntry(
    val id: String,
    val timestamp: Long,
    val type: TimelineType,
    val title: String,
    val summary: String,
    val severity: AlertSeverity = AlertSeverity.NORMAL
)

enum class TimelineType {
    INSPECTION, HARVEST, ALERT, DIAGNOSIS, HEALTH_CHANGE
}

class HiveViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HiveRepository
    val allHives: Flow<List<Hive>>
    val allHarvests: Flow<List<Harvest>>

    init {
        val hiveDao = AppDatabase.getDatabase(application).hiveDao()
        repository = HiveRepository(hiveDao)
        allHives = repository.allHives
        allHarvests = repository.allHarvests
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val allInspections: StateFlow<List<Inspection>> = allHives.flatMapLatest { hives ->
        if (hives.isEmpty()) flowOf(emptyList())
        else {
            val inspectionFlows = hives.map { repository.getInspectionsForHive(it.id) }
            combine(inspectionFlows) { it.flatMap { list -> list } }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalHivesCount: StateFlow<Int> = allHives
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalHoneyCollected: StateFlow<Double> = allHarvests
        .map { harvests -> harvests.sumOf { it.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val pendingInspectionsCount: StateFlow<Int> = allHives.flatMapLatest { hives ->
        if (hives.isEmpty()) flowOf(0)
        else {
            val pendingFlows = hives.map { hive ->
                repository.getInspectionsForHive(hive.id).map { inspections ->
                    val lastInspection = inspections.maxByOrNull { it.date }
                    lastInspection == null || (System.currentTimeMillis() - lastInspection.date) > (7 * 24 * 60 * 60 * 1000)
                }
            }
            combine(pendingFlows) { it.count { isPending -> isPending } }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val performanceMatrix: StateFlow<Map<String, Int>> = combine(
        allHives,
        allInspections,
        allHarvests
    ) { hives, inspections, harvests ->
        AnalyticsEngine.getPerformanceMatrix(hives, inspections, harvests)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    @OptIn(ExperimentalCoroutinesApi::class)
    val globalAdvisory: StateFlow<Recommendation?> = combine(
        allHives,
        allInspections,
        allHarvests
    ) { hives, inspections, harvests ->
        AnalyticsEngine.getGlobalAdvisory(hives, inspections, harvests)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val smartAlerts: StateFlow<List<SmartAlert>> = allHives.flatMapLatest { hives ->
        if (hives.isEmpty()) flowOf(emptyList())
        else {
            val hiveAlertsFlows = hives.map { hive ->
                combine(
                    repository.getInspectionsForHive(hive.id),
                    repository.getHarvestsForHive(hive.id)
                ) { inspections, harvests ->
                    HiveDiagnosisEngine.diagnose(hive, inspections, harvests).alerts
                }
            }
            combine(hiveAlertsFlows) { it.flatMap { alerts -> alerts } }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val apiaryHealthScore: StateFlow<Int> = allHives.flatMapLatest { hives ->
        if (hives.isEmpty()) flowOf(100)
        else {
            val scoreFlows = hives.map { hive ->
                combine(
                    repository.getInspectionsForHive(hive.id),
                    repository.getHarvestsForHive(hive.id)
                ) { inspections, harvests ->
                    HiveDiagnosisEngine.diagnose(hive, inspections, harvests).score
                }
            }
            combine(scoreFlows) { if (it.isEmpty()) 100 else it.average().toInt() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 100)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getDiagnosisForHive(hiveId: Long): Flow<DiagnosisResult?> {
        return allHives.flatMapLatest { hives ->
            val hive = hives.find { it.id == hiveId } ?: return@flatMapLatest flowOf(null)
            combine(
                repository.getInspectionsForHive(hiveId),
                repository.getHarvestsForHive(hiveId)
            ) { inspections, harvests ->
                HiveDiagnosisEngine.diagnose(hive, inspections, harvests)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTimelineForHive(hiveId: Long): Flow<List<TimelineEntry>> {
        return combine(
            repository.getInspectionsForHive(hiveId),
            repository.getHarvestsForHive(hiveId),
            allHives.map { it.find { h -> h.id == hiveId } }
        ) { inspections, harvests, hive ->
            val entries = mutableListOf<TimelineEntry>()
            val sortedInspections = inspections.sortedBy { it.date }
            
            var previousScore: Int? = null
            
            sortedInspections.forEachIndexed { index, inspection ->
                entries.add(TimelineEntry(
                    id = "insp_${inspection.id}",
                    timestamp = inspection.date,
                    type = TimelineType.INSPECTION,
                    title = "System Audit",
                    summary = "Bio-Telemetry recorded. Queen: ${if(inspection.isQueenPresent) "Detected" else "Absent"} | Temp: ${inspection.temperature}°C."
                ))
                
                if (hive != null) {
                    val pastInspections = sortedInspections.take(index + 1)
                    val pastHarvests = harvests.filter { it.date <= inspection.date }
                    val diagnosisAtTime = HiveDiagnosisEngine.diagnose(hive, pastInspections, pastHarvests)
                    
                    val currentScore = diagnosisAtTime.score
                    if (previousScore != null && currentScore != previousScore) {
                        val diff = currentScore - previousScore
                        entries.add(TimelineEntry(
                            id = "health_change_${inspection.id}",
                            timestamp = inspection.date + 500,
                            type = TimelineType.HEALTH_CHANGE,
                            title = "Vitality Calibration",
                            summary = "Stability index shifted by ${if (diff > 0) "+" else ""}$diff% based on biological audit.",
                            severity = if (diff < -15) AlertSeverity.CRITICAL else if (diff < 0) AlertSeverity.WARNING else AlertSeverity.NORMAL
                        ))
                    }
                    previousScore = currentScore
                }
            }
            
            harvests.forEach {
                entries.add(TimelineEntry(
                    id = "harv_${it.id}",
                    timestamp = it.date,
                    type = TimelineType.HARVEST,
                    title = "Yield Extraction",
                    summary = "Successfully extracted ${it.quantity}kg. Production logs updated."
                ))
            }

            if (hive != null && inspections.isNotEmpty()) {
                val currentDiagnosis = HiveDiagnosisEngine.diagnose(hive, inspections, harvests)
                
                entries.add(TimelineEntry(
                    id = "diag_${hive.id}_current",
                    timestamp = System.currentTimeMillis(),
                    type = TimelineType.DIAGNOSIS,
                    title = "AI Diagnosis: ${currentDiagnosis.status.uppercase()}",
                    summary = "Vitality Index: ${currentDiagnosis.score}% | Reliability: ${currentDiagnosis.confidenceScore}%",
                    severity = when(currentDiagnosis.status) {
                        "Critical" -> AlertSeverity.CRITICAL
                        "Warning" -> AlertSeverity.WARNING
                        else -> AlertSeverity.NORMAL
                    }
                ))

                currentDiagnosis.alerts.forEach { alert ->
                    entries.add(TimelineEntry(
                        id = "alert_${alert.id}",
                        timestamp = alert.timestamp,
                        type = TimelineType.ALERT,
                        title = alert.title,
                        summary = alert.description,
                        severity = alert.severity
                    ))
                }
            }

            entries.sortByDescending { it.timestamp }
            entries.distinctBy { it.id }
        }
    }

    fun addHive(hiveId: String, location: String) {
        viewModelScope.launch { repository.insertHive(Hive(hiveId = hiveId, location = location)) }
    }

    fun addInspection(hiveId: Long, isQueenPresent: Boolean, pestsSeen: Boolean, activityLevel: String, notes: String, temperature: Double, honeyFlow: String, broodCondition: String) {
        viewModelScope.launch {
            repository.insertInspection(
                Inspection(
                    hiveId = hiveId,
                    date = System.currentTimeMillis(),
                    isQueenPresent = isQueenPresent,
                    pestsSeen = pestsSeen,
                    activityLevel = activityLevel,
                    notes = notes,
                    temperature = temperature,
                    honeyFlow = honeyFlow,
                    broodCondition = broodCondition
                )
            )
        }
    }

    fun addHarvest(hiveId: Long, quantity: Double) {
        viewModelScope.launch {
            repository.insertHarvest(
                Harvest(
                    hiveId = hiveId,
                    date = System.currentTimeMillis(),
                    quantity = quantity
                )
            )
        }
    }

    fun getInspectionsForHive(hiveId: Long): Flow<List<Inspection>> = repository.getInspectionsForHive(hiveId)
    fun getHarvestsForHive(hiveId: Long): Flow<List<Harvest>> = repository.getHarvestsForHive(hiveId)
}
