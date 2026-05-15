package com.example.madhu_marga_2.logic

import com.example.madhu_marga_2.data.AlertSeverity
import com.example.madhu_marga_2.data.Harvest
import com.example.madhu_marga_2.data.Hive
import com.example.madhu_marga_2.data.Inspection
import com.example.madhu_marga_2.data.SmartAlert
import java.util.Calendar
import java.util.UUID

data class DiagnosisResult(
    val score: Int,
    val status: String,
    val alerts: List<SmartAlert>,
    val recommendations: List<Recommendation>,
    val confidenceScore: Int,
    val insights: List<String>,
    val risks: List<HealthRisk>,
    val forecast: YieldForecast? = null,
    val scoreBreakdown: List<ScoreComponent> = emptyList()
)

data class ScoreComponent(
    val label: String,
    val current: Int,
    val max: Int,
    val status: String,
    val description: String = ""
)

data class Recommendation(
    val title: String,
    val description: String,
    val priority: RecommendationPriority,
    val id: String = UUID.randomUUID().toString()
)

data class HealthRisk(
    val factor: String,
    val level: RiskLevel,
    val description: String
)

data class YieldForecast(
    val expectedKg: Double,
    val confidence: Int,
    val trend: String
)

enum class RiskLevel {
    LOW, MODERATE, HIGH, EXTREME
}

enum class RecommendationPriority {
    LOW, MEDIUM, HIGH, CRITICAL
}

object FloraEngine {
    data class BloomPhase(val name: String, val level: Int)

    fun getCurrentBloom(): BloomPhase {
        val month = Calendar.getInstance().get(Calendar.MONTH)
        return when (month) {
            in 2..4 -> BloomPhase("Spring Surge", 85)
            in 5..7 -> BloomPhase("Summer Peak", 95)
            in 8..10 -> BloomPhase("Autumn Maintenance", 40)
            else -> BloomPhase("Winter Dearth", 10)
        }
    }
}

object HiveDiagnosisEngine {
    
    fun diagnose(hive: Hive, inspections: List<Inspection>, harvests: List<Harvest>): DiagnosisResult {
        val sortedInspections = inspections.sortedByDescending { it.date }
        val latest = sortedInspections.firstOrNull()
        val previous = sortedInspections.getOrNull(1)
        val bloom = FloraEngine.getCurrentBloom()
        
        if (latest == null) {
            return DiagnosisResult(
                score = hive.lastHealthScore,
                status = "STABLE",
                alerts = emptyList(),
                recommendations = listOf(
                    Recommendation("Initial Audit Required", "Perform a comprehensive inspection to calibrate the AI Doctor.", RecommendationPriority.HIGH)
                ),
                confidenceScore = 5,
                insights = listOf("System awaiting telemetry data."),
                risks = emptyList()
            )
        }

        val risks = HealthRiskEvaluator.evaluate(latest, previous, inspections, bloom)
        val recommendations = RecommendationEngine.generate(latest, risks, harvests, bloom)
        val alerts = generateAlerts(hive.id, risks)
        
        val inspFreq = AnalyticsEngine.getInspectionFrequency(inspections)
        
        // Refined Weighted Scoring
        val bioScore = calculateBioVitality(latest) // Max 40
        val pathogenScore = if (latest.pestsSeen) 0 else 20 // Max 20
        val operationalScore = calculateOperationalScore(latest, inspFreq) // Max 20
        val envScore = calculateEnvironmentalAlignment(latest, bloom) // Max 20
        
        val rawScore = bioScore + pathogenScore + operationalScore + envScore
        var riskDeduction = 0
        risks.forEach { 
            riskDeduction += when(it.level) {
                RiskLevel.EXTREME -> 30
                RiskLevel.HIGH -> 15
                RiskLevel.MODERATE -> 8
                else -> 2
            }
        }
        
        val finalScore = (rawScore - riskDeduction).coerceIn(0, 100)
        
        val breakdown = listOf(
            ScoreComponent("Biological Vitality", bioScore, 40, 
                getComponentStatus(bioScore, 40), "Queen presence, brood pattern and population density."),
            ScoreComponent("Pathogen Resistance", pathogenScore, 20, 
                getComponentStatus(pathogenScore, 20), "Current pest load and resistance to local pathogens."),
            ScoreComponent("Management Quality", operationalScore, 20, 
                getComponentStatus(operationalScore, 20), "Inspection cadence and beekeeper intervention quality."),
            ScoreComponent("Flora Alignment", envScore, 20, 
                getComponentStatus(envScore, 20), "Efficiency of resource collection relative to available bloom.")
        )

        val recencyFactor = ((System.currentTimeMillis() - latest.date) / (1000 * 60 * 60 * 24)).coerceAtMost(30)
        val confidence = (50 + (inspections.size * 3) - recencyFactor.toInt() * 2).coerceIn(10, 99)

        val status = when {
            finalScore >= 85 -> "Stable"
            finalScore >= 65 -> "Warning"
            else -> "Critical"
        }

        val insights = generateInsights(latest, previous, hive, harvests, inspFreq, bloom)

        return DiagnosisResult(
            score = finalScore,
            status = status,
            alerts = alerts,
            recommendations = recommendations,
            confidenceScore = confidence,
            insights = insights,
            risks = risks,
            forecast = AnalyticsEngine.predictYield(harvests, inspections),
            scoreBreakdown = breakdown
        )
    }

    private fun getComponentStatus(current: Int, max: Int): String {
        val ratio = current.toFloat() / max
        return when {
            ratio >= 0.8f -> "Optimal"
            ratio >= 0.5f -> "Degraded"
            else -> "Critical"
        }
    }

    private fun calculateBioVitality(latest: Inspection): Int {
        var score = 0
        if (latest.isQueenPresent) score += 20
        score += when(latest.broodCondition) {
            "Excellent" -> 20
            "Healthy" -> 12
            "Poor" -> 4
            else -> 0
        }
        return score
    }

    private fun calculateOperationalScore(latest: Inspection, freq: Double): Int {
        var score = 0
        if (freq >= 0.5) score += 10
        if (freq >= 1.0) score += 5
        if (latest.activityLevel != "Low") score += 5
        return score.coerceAtMost(20)
    }

    private fun calculateEnvironmentalAlignment(latest: Inspection, bloom: FloraEngine.BloomPhase): Int {
        var score = 10 // Base alignment
        if (bloom.level > 70 && latest.honeyFlow == "High") score += 10
        if (bloom.level > 70 && latest.honeyFlow == "Normal") score += 5
        if (bloom.level < 30 && latest.activityLevel == "Low") score += 5 // Good conservation
        return score.coerceAtMost(20)
    }

    private fun generateAlerts(hiveId: Long, risks: List<HealthRisk>): List<SmartAlert> {
        return risks.filter { it.level >= RiskLevel.HIGH }.map { risk ->
            SmartAlert(
                id = UUID.randomUUID().toString(),
                title = risk.factor,
                description = risk.description,
                timestamp = System.currentTimeMillis(),
                severity = if (risk.level == RiskLevel.EXTREME) AlertSeverity.CRITICAL else AlertSeverity.WARNING,
                suggestedAction = "Execute ${risk.factor} Mitigation Plan",
                hiveId = hiveId
            )
        }
    }

    private fun generateInsights(latest: Inspection, previous: Inspection?, hive: Hive, harvests: List<Harvest>, freq: Double, bloom: FloraEngine.BloomPhase): List<String> {
        val insights = mutableListOf<String>()
        if (latest.honeyFlow == "High") insights.add("Intense nectar flow detected.")
        if (bloom.level > 80 && latest.honeyFlow != "High") insights.add("Unit is under-performing relative to flora peak.")
        if (latest.activityLevel == "High") insights.add("Robust foraging activity observed.")
        if (freq > 1.0) insights.add("Optimal maintenance cadence.")
        
        if (previous != null) {
            if (latest.temperature > previous.temperature + 3 && latest.temperature > 37) {
                insights.add("Internal thermal surge detected.")
            }
            if (latest.isQueenPresent && !previous.isQueenPresent) {
                insights.add("Queen recovery/introduction confirmed.")
            }
        }
        return insights
    }
}

object HealthRiskEvaluator {
    fun evaluate(latest: Inspection, previous: Inspection?, all: List<Inspection>, bloom: FloraEngine.BloomPhase): List<HealthRisk> {
        val risks = mutableListOf<HealthRisk>()
        if (!latest.isQueenPresent) risks.add(HealthRisk("Queen Failure", RiskLevel.EXTREME, "No queen detected. Colony survival at critical risk."))
        if (latest.pestsSeen) risks.add(HealthRisk("Pathogen Load", RiskLevel.HIGH, "Active pest infestation detected."))
        if (latest.broodCondition == "Poor") risks.add(HealthRisk("Brood Decline", RiskLevel.HIGH, "Failing or irregular brood pattern detected."))
        
        if (latest.temperature > 40.0) risks.add(HealthRisk("Extreme Hyperthermia", RiskLevel.EXTREME, "Dangerous internal heat. Risk of comb collapse."))
        else if (latest.temperature < 31.0 && latest.temperature > 0) risks.add(HealthRisk("Thermal Insufficiency", RiskLevel.MODERATE, "Low brood temperature."))
        
        if (previous != null) {
            if (latest.activityLevel == "Low" && previous.activityLevel != "Low") {
                risks.add(HealthRisk("Activity Suppression", RiskLevel.HIGH, "Sudden drop in foraging activity."))
            }
            if (latest.isQueenPresent && latest.activityLevel == "High" && latest.broodCondition == "Excellent" && bloom.level > 90) {
                risks.add(HealthRisk("Swarming Impulse", RiskLevel.HIGH, "High vigor during peak bloom. Unit likely to swarm."))
            }
        }
        
        if (bloom.level < 20 && latest.activityLevel == "High") {
            risks.add(HealthRisk("Resource Exhaustion", RiskLevel.HIGH, "High metabolic drain during dearth. Starvation risk elevated."))
        }
        
        return risks
    }
}

object RecommendationEngine {
    fun generate(latest: Inspection, risks: List<HealthRisk>, harvests: List<Harvest>, bloom: FloraEngine.BloomPhase): List<Recommendation> {
        val recs = mutableListOf<Recommendation>()
        risks.forEach { risk ->
            when (risk.factor) {
                "Queen Failure" -> recs.add(Recommendation("Urgent Re-queening", "Introduce a mated queen or fresh brood frame.", RecommendationPriority.CRITICAL))
                "Pathogen Load" -> recs.add(Recommendation("Pest Treatment", "Apply organic treatments (Oxalic/Formic acid) immediately.", RecommendationPriority.HIGH))
                "Extreme Hyperthermia" -> recs.add(Recommendation("Emergency Cooling", "Open screen bottom board and provide shade.", RecommendationPriority.CRITICAL))
                "Swarming Impulse" -> recs.add(Recommendation("Perform Split", "Divide the colony or add a large honey super to relieve pressure.", RecommendationPriority.HIGH))
                "Resource Exhaustion" -> recs.add(Recommendation("Emergency Feeding", "Provide 2:1 sugar syrup immediately to prevent starvation.", RecommendationPriority.CRITICAL))
                "Brood Decline" -> recs.add(Recommendation("Protein Supplement", "Provide pollen patties to stimulate growth.", RecommendationPriority.HIGH))
            }
        }
        if (latest.honeyFlow == "High" && bloom.level > 70) {
            recs.add(Recommendation("Add Honey Super", "Strong flow detected. Expand storage to maximize yield.", RecommendationPriority.MEDIUM))
        }
        if (recs.isEmpty()) recs.add(Recommendation("Standard Monitoring", "Colony health is nominal.", RecommendationPriority.LOW))
        return recs.distinctBy { it.title }.sortedByDescending { it.priority }
    }
}

object AnalyticsEngine {
    fun calculateProductivityScore(harvests: List<Harvest>): Int {
        if (harvests.isEmpty()) return 0
        val total = harvests.sumOf { it.quantity }
        val firstDate = harvests.minOf { it.date }
        val days = (System.currentTimeMillis() - firstDate) / (1000 * 60 * 60 * 24)
        val score = ((total / days.coerceAtLeast(1).toDouble()) * 200).toInt()
        return score.coerceIn(0, 100)
    }

    fun getComparisonMatrix(hives: List<Hive>, allHarvests: List<Harvest>): List<Pair<String, Double>> {
        return hives.map { hive ->
            val total = allHarvests.filter { it.hiveId == hive.id }.sumOf { it.quantity }
            hive.hiveId to total
        }.sortedByDescending { it.second }
    }

    fun getHealthTrend(inspections: List<Inspection>): List<Float> {
        return inspections.sortedBy { it.date }.takeLast(10).map { insp ->
            var s = 0.4f
            if (insp.isQueenPresent) s += 0.3f
            if (!insp.pestsSeen) s += 0.2f
            if (insp.broodCondition == "Excellent") s += 0.1f
            if (insp.broodCondition == "Poor") s -= 0.2f
            s.coerceIn(0f, 1f)
        }
    }

    fun getInspectionFrequency(inspections: List<Inspection>): Double {
        if (inspections.size < 2) return 0.0
        val sorted = inspections.sortedBy { it.date }
        val spanDays = (sorted.last().date - sorted.first().date) / (1000 * 60 * 60 * 24)
        if (spanDays == 0L) return 0.0
        return (inspections.size.toDouble() / spanDays) * 7.0
    }

    fun getInspectionTrends(inspections: List<Inspection>): List<Int> {
        val now = System.currentTimeMillis()
        val weekMs = 7L * 24 * 60 * 60 * 1000
        return (0..5).map { weekIndex ->
            val end = now - (weekIndex * weekMs)
            val start = end - weekMs
            inspections.count { it.date in start..end }
        }.reversed()
    }

    fun getAlertFrequency(alerts: List<SmartAlert>): Map<AlertSeverity, Int> {
        return alerts.groupBy { it.severity }.mapValues { it.value.size }
    }
    
    fun getGlobalAdvisory(hives: List<Hive>, allInspections: List<Inspection>, allHarvests: List<Harvest>): Recommendation? {
        val allRecs = hives.map { hive ->
            val hiveInsps = allInspections.filter { it.hiveId == hive.id }
            val hiveHarvs = allHarvests.filter { it.hiveId == hive.id }
            HiveDiagnosisEngine.diagnose(hive, hiveInsps, hiveHarvs).recommendations
        }.flatten()
        return allRecs.filter { it.priority != RecommendationPriority.LOW }.maxByOrNull { it.priority }
    }

    fun predictYield(harvests: List<Harvest>, inspections: List<Inspection>): YieldForecast {
        val score = calculateProductivityScore(harvests)
        val latest = inspections.sortedByDescending { it.date }.firstOrNull()
        val bloom = FloraEngine.getCurrentBloom()
        
        var multiplier = 1.0
        if (latest?.honeyFlow == "High") multiplier *= 1.5
        if (bloom.level > 80) multiplier *= 1.3
        if (latest?.broodCondition == "Excellent") multiplier *= 1.2
        
        val expected = (score * 0.5) * multiplier
        val confidence = (inspections.size * 5).coerceIn(10, 95)
        val trend = if (multiplier > 1.2) "RISING" else if (multiplier < 0.8) "DECLINING" else "STABLE"
        
        return YieldForecast(expected, confidence, trend)
    }

    fun getPerformanceMatrix(hives: List<Hive>, allInspections: List<Inspection>, allHarvests: List<Harvest>): Map<String, Int> {
        var elites = 0
        var developing = 0
        var strained = 0
        var underperformers = 0

        hives.forEach { hive ->
            val hiveInsps = allInspections.filter { it.hiveId == hive.id }
            val hiveHarvs = allHarvests.filter { it.hiveId == hive.id }
            val diag = HiveDiagnosisEngine.diagnose(hive, hiveInsps, hiveHarvs)
            val prod = calculateProductivityScore(hiveHarvs)

            val highHealth = diag.score >= 75
            val highProd = prod >= 50

            when {
                highHealth && highProd -> elites++
                highHealth && !highProd -> developing++
                !highHealth && highProd -> strained++
                else -> underperformers++
            }
        }

        return mapOf(
            "ELITE UNITS" to elites,
            "DEVELOPING" to developing,
            "STRAINED" to strained,
            "CRITICAL" to underperformers
        )
    }
}
