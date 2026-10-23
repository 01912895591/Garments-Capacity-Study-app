package com.example.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.database.CapacityStudy
import com.example.data.database.StudyProcess
import java.io.File
import java.io.FileOutputStream

object PdfReportExporter {

    fun generateAndSharePdf(
        context: Context,
        study: CapacityStudy,
        processes: List<StudyProcess>,
        share: Boolean = true,
        print: Boolean = false
    ) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        val textPaint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
        }

        // Draw Header background
        paint.color = Color.parseColor("#0F172A") // Modern Indigo Slate
        canvas.drawRect(0f, 0f, 595f, 65f, paint)

        // Title
        textPaint.apply {
            color = Color.WHITE
            textSize = 18f
            isFakeBoldText = true
        }
        canvas.drawText("GARMENTS CAPACITY GRAPH & STUDY REPORT", 20f, 40f, textPaint)

        // General info labels (Left Column of metadata tables)
        var y = 75f
        
        // Count processes' operational cycle time
        val totalManpower = processes.sumOf { it.getOperatorCountByMachineCategory("M") + it.getOperatorCountByMachineCategory("IM") + it.getOperatorCountByMachineCategory("OPERATOR") }
        val totalOperators = processes.sumOf { it.getOperatorCountByMachineCategory("OPERATOR") }
        val totalHelpers = processes.sumOf { it.getOperatorCountByMachineCategory("M") }
        val totalIronman = processes.sumOf { it.getOperatorCountByMachineCategory("IM") }
        
        val totalCycles = processes.sumOf { 
            (if (it.isOp1Active()) it.getOp1Average() else 0.0) +
            (if (it.isOp2Active()) it.getOp2Average() else 0.0) +
            (if (it.isOp3Active()) it.getOp3Average() else 0.0)
        }
        val basicPitchTime = if (totalManpower > 0) totalCycles / totalManpower else 0.0
        val workerPotential = if (basicPitchTime > 0) 3600.0 / basicPitchTime else 0.0
        val productivityGap = if (workerPotential > 0) ((study.currentPcsPerHour - workerPotential) / workerPotential) * 100 else 0.0

        val box1Labels = listOf(
            "TOTAL CYCLE TIME" to String.format("%.2f s", totalCycles),
            "BASIC PITCH TIME" to String.format("%.2f s", basicPitchTime),
            "WORKER POTENTIAL/HR" to String.format("%.0f", workerPotential),
            "TARGET/HOUR" to study.targetPerHour.toString(),
            "CURRENT PCS/HOUR" to study.currentPcsPerHour.toString(),
            "PRODUCTIVITY GAP (%)" to String.format("%.2f%%", productivityGap)
        )

        val box2Labels = listOf(
            "TOTAL OPERATOR" to totalOperators.toString(),
            "TOTAL HELPER" to totalHelpers.toString(),
            "TOTAL IRONMAN" to totalIronman.toString(),
            "TOTAL MANPOWER" to totalManpower.toString(),
            "STUDIED BY" to study.studiedBy,
            "DATE" to study.date
        )

        val box3Labels = listOf(
            "LINE NO." to study.lineNo,
            "BUYER" to study.buyer,
            "STYLE NO." to study.styleNo,
            "DAYS RUN" to study.daysRun,
            "GRAPH INFO" to study.graphInfo,
            "SMV" to study.smv
        )

        // Draw info box cell borders & dividers
        paint.color = Color.parseColor("#C4C6D0")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        
        // 3 Container borders
        canvas.drawRect(15f, 75f, 200f, 160f, paint)
        canvas.drawRect(205f, 75f, 390f, 160f, paint)
        canvas.drawRect(395f, 75f, 580f, 160f, paint)

        // Draw horizontal lines inside metadata tables (6 rows)
        paint.color = Color.parseColor("#E2E8F0")
        paint.strokeWidth = 0.5f
        for (i in 1..5) {
            val dividerY = 75f + (i * 14.16f)
            canvas.drawLine(15f, dividerY, 200f, dividerY, paint)
            canvas.drawLine(205f, dividerY, 390f, dividerY, paint)
            canvas.drawLine(395f, dividerY, 580f, dividerY, paint)
        }

        // Draw vertical dividers inside metadata tables
        paint.color = Color.parseColor("#C4C6D0")
        canvas.drawLine(130f, 75f, 130f, 160f, paint)
        canvas.drawLine(310f, 75f, 310f, 160f, paint)
        canvas.drawLine(490f, 75f, 490f, 160f, paint)

        // Paint setups
        val labelPaint = Paint().apply {
            color = Color.parseColor("#00478F")
            isAntiAlias = true
            textSize = 6.5f
            isFakeBoldText = true
        }
        val valuePaint = Paint().apply {
            color = Color.parseColor("#002E69")
            isAntiAlias = true
            textSize = 7f
            isFakeBoldText = true
        }

        // Draw cells text
        for (i in 0 until 6) {
            val yPos = 75f + (i * 14.16f) + 10f
            
            // Box 1
            canvas.drawText(box1Labels[i].first, 18f, yPos, labelPaint)
            canvas.drawText(box1Labels[i].second, 135f, yPos, valuePaint)
            
            // Box 2
            canvas.drawText(box2Labels[i].first, 208f, yPos, labelPaint)
            canvas.drawText(box2Labels[i].second, 315f, yPos, valuePaint)
            
            // Box 3
            canvas.drawText(box3Labels[i].first, 398f, yPos, labelPaint)
            canvas.drawText(box3Labels[i].second, 495f, yPos, valuePaint)
        }

        // Now draw the processes grid table side-by-side with horizontal capacity graph
        val tableY = 175f
        
        // Draw Header background
        paint.color = Color.parseColor("#2F3033")
        paint.style = Paint.Style.FILL
        canvas.drawRect(15f, tableY, 360f, tableY + 18f, paint)
        canvas.drawRect(370f, tableY, 580f, tableY + 18f, paint)
        
        // Draw header text
        textPaint.apply {
            color = Color.parseColor("#D1E1FF")
            textSize = 7.2f
            isFakeBoldText = true
        }
        canvas.drawText("SL", 17f, tableY + 12f, textPaint)
        canvas.drawText("PROCESS NAME", 31f, tableY + 12f, textPaint)
        canvas.drawText("M/C", 117f, tableY + 12f, textPaint)
        canvas.drawText("OP-1", 147f, tableY + 12f, textPaint)
        canvas.drawText("CAP", 197f, tableY + 12f, textPaint)
        canvas.drawText("OP-2", 219f, tableY + 12f, textPaint)
        canvas.drawText("CAP", 269f, tableY + 12f, textPaint)
        canvas.drawText("OP-3", 291f, tableY + 12f, textPaint)
        canvas.drawText("CAP", 341f, tableY + 12f, textPaint)

        // Draw graph scale ticks inside header
        val scalePaint = Paint().apply {
            color = Color.parseColor("#E2E2E6")
            textSize = 6.2f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val maxCapValue = 250.0
        val graphXStart = 370f
        val graphWidth = 190f // 370f to 560f span
        for (ticks in listOf(0, 50, 100, 150, 200, 250)) {
            val tickX = graphXStart + (ticks / maxCapValue * graphWidth).toFloat()
            canvas.drawText(ticks.toString(), tickX - 5f, tableY + 12f, scalePaint)
        }

        // Sorting by sl (sequence number wise) as requested
        val sortedProcesses = processes.sortedBy { it.sl }
        val totalRows = sortedProcesses.take(25) // Up to 25 rows fit beautifully on single page
        val rowHeight = 16f
        val rowY = tableY + 18f
        val bottomY = rowY + (totalRows.size * rowHeight)

        // Draw vertical scale guidelines in graph background
        paint.color = Color.parseColor("#E2E8F0")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.5f
        for (ticks in listOf(50, 100, 150, 200, 250)) {
            val tickX = graphXStart + (ticks / maxCapValue * graphWidth).toFloat()
            canvas.drawLine(tickX, rowY, tickX, bottomY, paint)
        }

        // Draw vertical red target line going through all future bars
        val targetX = graphXStart + (study.targetPerHour / maxCapValue * graphWidth).toFloat().coerceIn(0f, graphWidth)
        val targetLinePaint = Paint().apply {
            color = Color.parseColor("#EF4444")
            strokeWidth = 1.2f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(targetX, rowY, targetX, bottomY, targetLinePaint)

        // Draw rows
        paint.style = Paint.Style.FILL
        for ((index, process) in totalRows.withIndex()) {
            val yOffset = rowY + (index * rowHeight)
            
            // 1. Draw alternate background colors
            paint.style = Paint.Style.FILL
            paint.color = if (index % 2 == 0) Color.parseColor("#FFFFFF") else Color.parseColor("#F8FAFC")
            canvas.drawRect(15f, yOffset, 360f, yOffset + rowHeight, paint)
            canvas.drawRect(370f, yOffset, 580f, yOffset + rowHeight, paint)
            
            // 2. Draw row border lines
            paint.color = Color.parseColor("#C4C6D0")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.5f
            canvas.drawLine(15f, yOffset + rowHeight, 360f, yOffset + rowHeight, paint)
            canvas.drawLine(370f, yOffset + rowHeight, 580f, yOffset + rowHeight, paint)
            
            // 3. Draw vertical table cell borders
            canvas.drawLine(15f, yOffset, 15f, yOffset + rowHeight, paint)
            canvas.drawLine(28f, yOffset, 28f, yOffset + rowHeight, paint)
            canvas.drawLine(115f, yOffset, 115f, yOffset + rowHeight, paint)
            canvas.drawLine(145f, yOffset, 145f, yOffset + rowHeight, paint)
            canvas.drawLine(195f, yOffset, 195f, yOffset + rowHeight, paint)
            canvas.drawLine(217f, yOffset, 217f, yOffset + rowHeight, paint)
            canvas.drawLine(267f, yOffset, 267f, yOffset + rowHeight, paint)
            canvas.drawLine(289f, yOffset, 289f, yOffset + rowHeight, paint)
            canvas.drawLine(339f, yOffset, 339f, yOffset + rowHeight, paint)
            canvas.drawLine(360f, yOffset, 360f, yOffset + rowHeight, paint)
            
            // Graph borders
            canvas.drawLine(370f, yOffset, 370f, yOffset + rowHeight, paint)
            canvas.drawLine(580f, yOffset, 580f, yOffset + rowHeight, paint)

            // 4. Draw texts
            textPaint.apply {
                color = Color.BLACK
                textSize = 6.2f
                isFakeBoldText = false
                isAntiAlias = true
            }

            // SL
            canvas.drawText(process.sl.toString(), 18f, yOffset + 11f, textPaint)
            
            // Process Name
            var pName = process.processName
            if (pName.length > 20) pName = pName.take(18) + ".."
            canvas.drawText(pName, 31f, yOffset + 11f, textPaint)
            
            // MC
            canvas.drawText(process.mc.uppercase(), 117f, yOffset + 11f, textPaint)

            val caps = process.getOpCapacities()
            
            // OP-1 & Cap-1
            var op1 = if (process.isOp1Active()) process.op1Name else "-"
            if (op1.length > 12) op1 = op1.take(10) + ".."
            canvas.drawText(op1, 147f, yOffset + 11f, textPaint)
            val cap1Str = if (process.isOp1Active() && caps.first > 0) String.format("%.0f", caps.first) else "-"
            canvas.drawText(cap1Str, 197f, yOffset + 11f, textPaint)

            // OP-2 & Cap-2
            var op2 = if (process.isOp2Active()) process.op2Name else "-"
            if (op2.length > 12) op2 = op2.take(10) + ".."
            canvas.drawText(op2, 219f, yOffset + 11f, textPaint)
            val cap2Str = if (process.isOp2Active() && caps.second > 0) String.format("%.0f", caps.second) else "-"
            canvas.drawText(cap2Str, 269f, yOffset + 11f, textPaint)

            // OP-3 & Cap-3
            var op3 = if (process.isOp3Active()) process.op3Name else "-"
            if (op3.length > 12) op3 = op3.take(10) + ".."
            canvas.drawText(op3, 291f, yOffset + 11f, textPaint)
            val cap3Str = if (process.isOp3Active() && caps.third > 0) String.format("%.0f", caps.third) else "-"
            canvas.drawText(cap3Str, 341f, yOffset + 11f, textPaint)

            // 5. Draw horizontal bar on the graph side
            val totalCapacity = caps.first + caps.second + caps.third
            if (totalCapacity > 0.0) {
                val barW = ((totalCapacity / maxCapValue) * graphWidth).toFloat().coerceAtMost(graphWidth)
                
                // Color depending on Target achievement
                paint.style = Paint.Style.FILL
                paint.color = if (totalCapacity >= study.targetPerHour) {
                    Color.parseColor("#10B981") // Green
                } else if (totalCapacity >= study.targetPerHour * 0.8) {
                    Color.parseColor("#F59E0B") // Amber
                } else {
                    Color.parseColor("#EF4444") // Red
                }
                
                canvas.drawRect(373f, yOffset + 3f, 373f + barW, yOffset + 13f, paint)

                textPaint.apply {
                    color = Color.parseColor("#334155")
                    textSize = 5.8f
                    isFakeBoldText = true
                }
                canvas.drawText(String.format("%.0f", totalCapacity), 373f + barW + 3f, yOffset + 11f, textPaint)
            }
        }

        // Draw Legends at index bottom + padding
        val legendY = bottomY + 15f
        if (legendY + 25f < 835f) {
            paint.style = Paint.Style.FILL
            
            // Green Box
            paint.color = Color.parseColor("#10B981")
            canvas.drawRect(25f, legendY, 35f, legendY + 10f, paint)
            textPaint.apply {
                color = Color.BLACK
                textSize = 7.5f
                isFakeBoldText = true
            }
            canvas.drawText("Target Met (>= Target)", 40f, legendY + 8f, textPaint)
            
            // Amber Box
            paint.color = Color.parseColor("#F59E0B")
            canvas.drawRect(180f, legendY, 190f, legendY + 10f, paint)
            canvas.drawText("Caution (>= 80% Target)", 195f, legendY + 8f, textPaint)
            
            // Red Box
            paint.color = Color.parseColor("#EF4444")
            canvas.drawRect(340f, legendY, 350f, legendY + 10f, paint)
            canvas.drawText("Severe Bottleneck (< 80% Target)", 355f, legendY + 8f, textPaint)

            // Red line legend
            paint.color = Color.parseColor("#EF4444")
            paint.strokeWidth = 1.2f
            canvas.drawLine(495f, legendY + 5f, 510f, legendY + 5f, paint)
            canvas.drawText("Target Line (${study.targetPerHour} pcs/hr)", 515f, legendY + 8f, textPaint)
        }

        pdfDocument.finishPage(page)

        // Save PDF to cache directory
        val fileName = "capacity_study_${study.id}.pdf"
        val pdfFile = File(context.cacheDir, fileName)
        try {
            val fos = FileOutputStream(pdfFile)
            pdfDocument.writeTo(fos)
            fos.close()
            pdfDocument.close()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            if (share) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Capacity Study Report - SL ${study.styleNo}")
                    putExtra(Intent.EXTRA_TEXT, "Hello, here is the official Industrial Engineering Capacity Study Report for Line ${study.lineNo}.")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(intent, "Share Capacity Report")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } else if (print) {
                // Standard print intent or view intent
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "PDF Report successfully saved!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error exporting report: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
