package com.example.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "capacity_studies")
data class CapacityStudy(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val lineNo: String = "C",
    val buyer: String = "ROYAL FASHIONS",
    val styleNo: String = "4102/B",
    val studiedBy: String = "Basher",
    val date: String = "1-Jul-24",
    val targetPerHour: Int = 150,
    val currentPcsPerHour: Int = 100,
    val daysRun: String = "",
    val graphInfo: String = "",
    val smv: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "study_processes",
    foreignKeys = [
        ForeignKey(
            entity = CapacityStudy::class,
            parentColumns = ["id"],
            childColumns = ["studyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["studyId"])]
)
data class StudyProcess(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studyId: Int,
    val sl: Int,
    val processName: String,
    val mc: String, // Machine type (e.g. SNL, KANCHA, OL, FOA, BH, 'M' for helper/manual, 'IM' for ironman)
    
    // Operator 1
    val op1Name: String = "",
    val op1C1: Double = 0.0,
    val op1C2: Double = 0.0,
    val op1C3: Double = 0.0,
    val op1C4: Double = 0.0,
    val op1C5: Double = 0.0,
    
    // Operator 2
    val op2Name: String = "",
    val op2C1: Double = 0.0,
    val op2C2: Double = 0.0,
    val op2C3: Double = 0.0,
    val op2C4: Double = 0.0,
    val op2C5: Double = 0.0,
    
    // Operator 3
    val op3Name: String = "",
    val op3C1: Double = 0.0,
    val op3C2: Double = 0.0,
    val op3C3: Double = 0.0,
    val op3C4: Double = 0.0,
    val op3C5: Double = 0.0,
    
    val relation: String = "Parallel" // "Parallel" or "Sequence"
) {
    // Helper to calculate average of non-zero cycles
    private fun calcAvg(c1: Double, c2: Double, c3: Double, c4: Double, c5: Double): Double {
        val list = listOf(c1, c2, c3, c4, c5).filter { it > 0.0 }
        return if (list.isEmpty()) 0.0 else list.average()
    }

    fun getOp1Average(): Double = calcAvg(op1C1, op1C2, op1C3, op1C4, op1C5)
    fun getOp2Average(): Double = calcAvg(op2C1, op2C2, op2C3, op2C4, op2C5)
    fun getOp3Average(): Double = calcAvg(op3C1, op3C2, op3C3, op3C4, op3C5)

    fun isOp1Active(): Boolean = op1Name.isNotBlank() || (op1C1 + op1C2 + op1C3 + op1C4 + op1C5 > 0.0)
    fun isOp2Active(): Boolean = op2Name.isNotBlank() || (op2C1 + op2C2 + op2C3 + op2C4 + op2C5 > 0.0)
    fun isOp3Active(): Boolean = op3Name.isNotBlank() || (op3C1 + op3C2 + op3C3 + op3C4 + op3C5 > 0.0)

    // Helper functions to count operators based on machine type
    fun getOperatorCountByMachineCategory(category: String): Int {
        val count = (if (isOp1Active()) 1 else 0) + (if (isOp2Active()) 1 else 0) + (if (isOp3Active()) 1 else 0)
        val isMatch = when (category.uppercase()) {
            "M" -> mc.uppercase() == "M"
            "IM" -> mc.uppercase() == "IM"
            "OPERATOR" -> mc.uppercase() != "M" && mc.uppercase() != "IM"
            else -> false
        }
        return if (isMatch) count else 0
    }

    // Get capacities based on relationship
    fun getOpCapacities(): Triple<Double, Double, Double> {
        val op1Avg = getOp1Average()
        val op2Avg = getOp2Average()
        val op3Avg = getOp3Average()

        if (relation == "Sequence") {
            // Sequence relationship: they split the task. Their capacities depend on the sum of their average times.
            val sumAverages = (if (isOp1Active()) op1Avg else 0.0) +
                              (if (isOp2Active()) op2Avg else 0.0) +
                              (if (isOp3Active()) op3Avg else 0.0)
            if (sumAverages <= 0.0) return Triple(0.0, 0.0, 0.0)
            val cap = 3600.0 * 0.9 / sumAverages
            return Triple(
                if (isOp1Active()) cap else 0.0,
                if (isOp2Active()) cap else 0.0,
                if (isOp3Active()) cap else 0.0
            )
        } else {
            // Parallel relationship (or only 1 operator active): independent
            val cap1 = if (op1Avg > 0.0) 3600.0 * 0.9 / op1Avg else 0.0
            val cap2 = if (op2Avg > 0.0) 3600.0 * 0.9 / op2Avg else 0.0
            val cap3 = if (op3Avg > 0.0) 3600.0 * 0.9 / op3Avg else 0.0
            return Triple(cap1, cap2, cap3)
        }
    }
}
