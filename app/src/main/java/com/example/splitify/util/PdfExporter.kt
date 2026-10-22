package com.example.splitify.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.model.Friend
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842

    fun exportOverallStatementPdf(
        context: Context,
        expenses: List<Expense>,
        totalSpent: Double,
        averageExpense: Double
    ) {
        try {
            val pdfDocument = PdfDocument()
            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            val paint = Paint()
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val dateStr = SimpleDateFormat("MMMM dd, yyyy · hh:mm a", Locale.getDefault()).format(Date())

            // Header Banner
            drawHeader(canvas, paint, "SPLITFLOW - OVERALL EXPENSE STATEMENT", "Generated on $dateStr")

            // Overview Summary Cards
            drawSummaryBox(
                canvas = canvas,
                paint = paint,
                topY = 75f,
                label1 = "TOTAL SPENT",
                val1 = String.format(Locale.getDefault(), "₹%.2f", totalSpent),
                label2 = "AVERAGE / TRANS",
                val2 = String.format(Locale.getDefault(), "₹%.2f", averageExpense),
                label3 = "TOTAL TRANSACTIONS",
                val3 = "${expenses.size} Records"
            )

            // Category Bar Chart
            drawCategoryChart(canvas, paint, expenses, totalSpent, startY = 155f)

            // Table Header
            var currentY = 240f
            currentY = drawTableHeader(canvas, paint, currentY, isFriendView = false)

            // Table Rows
            for ((index, expense) in expenses.withIndex()) {
                if (currentY > (PAGE_HEIGHT - 60f)) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    currentY = 40f
                    currentY = drawTableHeader(canvas, paint, currentY, isFriendView = false)
                }

                currentY = drawTableRow(canvas, paint, expense, currentY, index % 2 == 0, isFriendView = false, dateFormat)
            }

            // Footer / Explanation
            drawFooter(canvas, paint, currentY + 20f, "This statement includes all expense records stored in SplitFlow.")

            pdfDocument.finishPage(page)

            // Save PDF File
            val fileName = "SplitFlow_Overall_Statement_${System.currentTimeMillis()}.pdf"
            val file = File(context.getExternalFilesDir(null), fileName)
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()

            openPdf(context, file)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error generating PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun exportFriendStatementPdf(
        context: Context,
        friend: Friend,
        expenses: List<Expense>,
        totalOwed: Double
    ) {
        try {
            val pdfDocument = PdfDocument()
            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            val paint = Paint()
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val dateStr = SimpleDateFormat("MMMM dd, yyyy · hh:mm a", Locale.getDefault()).format(Date())

            val phoneText = if (friend.phoneNumber.isNotBlank()) " (${friend.phoneNumber})" else ""
            val totalExpenseSum = expenses.sumOf { it.totalAmount }

            // Header Banner
            drawHeader(canvas, paint, "SPLITFLOW - STATEMENT FOR ${friend.name.uppercase(Locale.getDefault())}$phoneText", "Generated on $dateStr")

            val statusLabel = if (totalOwed >= 0) "TO COLLECT FROM ${friend.name.uppercase(Locale.getDefault())}" else "YOU OWE ${friend.name.uppercase(Locale.getDefault())}"
            val statusValue = String.format(Locale.getDefault(), "₹%.2f", if (totalOwed >= 0) totalOwed else -totalOwed)

            // Overview Summary Cards
            drawSummaryBox(
                canvas = canvas,
                paint = paint,
                topY = 75f,
                label1 = statusLabel,
                val1 = statusValue,
                label2 = "LINKED EXPENSES TOTAL",
                val2 = String.format(Locale.getDefault(), "₹%.2f", totalExpenseSum),
                label3 = "TOTAL TRANSACTIONS",
                val3 = "${expenses.size} Records"
            )

            // Category Bar Chart
            drawCategoryChart(canvas, paint, expenses, totalExpenseSum, startY = 155f)

            // Settlement Banner Text (Beginner-Friendly Explanation)
            val explanationText = if (totalOwed > 0) {
                "EASY SUMMARY: Based on all shared transactions above, ${friend.name} owes you a net total of $statusValue."
            } else if (totalOwed < 0) {
                "EASY SUMMARY: Based on all shared transactions above, you owe ${friend.name} a net total of $statusValue."
            } else {
                "EASY SUMMARY: All balances between you and ${friend.name} are completely settled!"
            }

            // Table Header
            var currentY = 240f
            currentY = drawTableHeader(canvas, paint, currentY, isFriendView = true)

            // Table Rows
            for ((index, expense) in expenses.withIndex()) {
                if (currentY > (PAGE_HEIGHT - 80f)) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    currentY = 40f
                    currentY = drawTableHeader(canvas, paint, currentY, isFriendView = true)
                }

                currentY = drawTableRow(canvas, paint, expense, currentY, index % 2 == 0, isFriendView = true, dateFormat)
            }

            // Footer Explanation
            drawFooter(canvas, paint, currentY + 20f, explanationText)

            pdfDocument.finishPage(page)

            // Save PDF File
            val cleanFriendName = friend.name.replace("[^a-zA-Z0-9]".toRegex(), "_")
            val fileName = "SplitFlow_Statement_${cleanFriendName}_${System.currentTimeMillis()}.pdf"
            val file = File(context.getExternalFilesDir(null), fileName)
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()

            openPdf(context, file)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error generating PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun drawHeader(canvas: Canvas, paint: Paint, title: String, subtitle: String) {
        paint.color = Color.parseColor("#1976D2") // Primary Blue
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 60f, paint)

        paint.color = Color.WHITE
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText(title, 20f, 32f, paint)

        paint.textSize = 9f
        paint.isFakeBoldText = false
        canvas.drawText(subtitle, 20f, 48f, paint)
    }

    private fun drawSummaryBox(
        canvas: Canvas,
        paint: Paint,
        topY: Float,
        label1: String,
        val1: String,
        label2: String,
        val2: String,
        label3: String,
        val3: String
    ) {
        val boxWidth = (PAGE_WIDTH - 60f) / 3f
        val boxHeight = 65f

        val boxColors = listOf("#E3F2FD", "#F3E5F5", "#E8F5E9")
        val labels = listOf(label1, label2, label3)
        val values = listOf(val1, val2, val3)

        for (i in 0..2) {
            val left = 20f + i * (boxWidth + 10f)
            val rect = RectF(left, topY, left + boxWidth, topY + boxHeight)

            paint.color = Color.parseColor(boxColors[i])
            canvas.drawRoundRect(rect, 8f, 8f, paint)

            paint.color = Color.parseColor("#333333")
            paint.textSize = 8f
            paint.isFakeBoldText = true
            canvas.drawText(labels[i], left + 10f, topY + 22f, paint)

            paint.color = Color.parseColor("#0D47A1")
            paint.textSize = 11f
            paint.isFakeBoldText = true
            canvas.drawText(values[i], left + 10f, topY + 48f, paint)
        }
    }

    private fun drawCategoryChart(
        canvas: Canvas,
        paint: Paint,
        expenses: List<Expense>,
        totalSpent: Double,
        startY: Float
    ) {
        paint.color = Color.parseColor("#212121")
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText("CATEGORY BREAKDOWN & SPENDING DISTRIBUTION", 20f, startY + 12f, paint)

        if (totalSpent <= 0 || expenses.isEmpty()) {
            paint.textSize = 9f
            paint.isFakeBoldText = false
            paint.color = Color.GRAY
            canvas.drawText("No category transactions to display in chart.", 20f, startY + 30f, paint)
            return
        }

        val categoryGroup = expenses.groupBy { it.category }.mapValues { it.value.sumOf { exp -> exp.totalAmount } }
        val categoryColors = mapOf(
            "Food" to "#FF9800",
            "Travel" to "#2196F3",
            "Stay" to "#9C27B0",
            "Shopping" to "#E91E63",
            "Entertainment" to "#FF5722",
            "Other" to "#607D8B"
        )

        var currentX = 20f
        val chartWidth = PAGE_WIDTH - 40f
        val barHeight = 16f
        val barY = startY + 22f

        // Draw stacked bar
        for ((cat, amount) in categoryGroup) {
            val sliceWidth = ((amount / totalSpent) * chartWidth).toFloat()
            paint.color = Color.parseColor(categoryColors[cat] ?: "#9E9E9E")
            canvas.drawRect(currentX, barY, currentX + sliceWidth, barY + barHeight, paint)
            currentX += sliceWidth
        }

        // Draw legend items below bar
        var legendX = 20f
        var legendY = barY + barHeight + 16f

        paint.textSize = 8f
        paint.isFakeBoldText = false

        for ((cat, amount) in categoryGroup) {
            val colorHex = categoryColors[cat] ?: "#9E9E9E"
            val percentage = (amount / totalSpent) * 100

            paint.color = Color.parseColor(colorHex)
            canvas.drawCircle(legendX + 4f, legendY - 3f, 4f, paint)

            paint.color = Color.parseColor("#333333")
            val legendText = "$cat: ₹${String.format(Locale.getDefault(), "%.0f", amount)} (${String.format(Locale.getDefault(), "%.0f", percentage)}%)"
            canvas.drawText(legendText, legendX + 12f, legendY, paint)

            legendX += paint.measureText(legendText) + 20f
            if (legendX > PAGE_WIDTH - 100f) {
                legendX = 20f
                legendY += 14f
            }
        }
    }

    private fun drawTableHeader(canvas: Canvas, paint: Paint, startY: Float, isFriendView: Boolean): Float {
        val rowHeight = 22f

        paint.color = Color.parseColor("#1E88E5")
        canvas.drawRect(20f, startY, PAGE_WIDTH - 20f, startY + rowHeight, paint)

        paint.color = Color.WHITE
        paint.textSize = 9f
        paint.isFakeBoldText = true

        canvas.drawText("Date", 25f, startY + 15f, paint)
        canvas.drawText("Title / Description", 95f, startY + 15f, paint)
        canvas.drawText("Category", 235f, startY + 15f, paint)
        canvas.drawText("Paid By", 325f, startY + 15f, paint)
        canvas.drawText("Method", 395f, startY + 15f, paint)
        canvas.drawText("Total (₹)", 465f, startY + 15f, paint)

        if (isFriendView) {
            canvas.drawText("Share (₹)", 525f, startY + 15f, paint)
        }

        return startY + rowHeight
    }

    private fun drawTableRow(
        canvas: Canvas,
        paint: Paint,
        expense: Expense,
        startY: Float,
        isEven: Boolean,
        isFriendView: Boolean,
        dateFormat: SimpleDateFormat
    ): Float {
        val rowHeight = 22f

        // Row background
        paint.color = if (isEven) Color.parseColor("#FAFAFA") else Color.parseColor("#F0F4C3")
        canvas.drawRect(20f, startY, PAGE_WIDTH - 20f, startY + rowHeight, paint)

        paint.color = Color.parseColor("#212121")
        paint.textSize = 8f
        paint.isFakeBoldText = false

        // Date
        val dateText = dateFormat.format(Date(expense.timestamp))
        canvas.drawText(dateText, 25f, startY + 14f, paint)

        // Title (truncated)
        val titleText = if (expense.title.length > 24) expense.title.take(22) + ".." else expense.title
        canvas.drawText(titleText, 95f, startY + 14f, paint)

        // Category
        canvas.drawText(expense.category, 235f, startY + 14f, paint)

        // Paid By
        canvas.drawText(expense.paidBy, 325f, startY + 14f, paint)

        // Method
        canvas.drawText(expense.paymentMethod, 395f, startY + 14f, paint)

        // Total
        paint.isFakeBoldText = true
        canvas.drawText(String.format(Locale.getDefault(), "%.2f", expense.totalAmount), 465f, startY + 14f, paint)

        // Share Amount for friend
        if (isFriendView) {
            val shareVal = expense.perPersonAmount
            val isYouPaid = expense.paidBy.equals("You", ignoreCase = true)
            paint.color = if (isYouPaid) Color.parseColor("#2E7D32") else Color.parseColor("#C62828")
            canvas.drawText(String.format(Locale.getDefault(), "%.2f", shareVal), 525f, startY + 14f, paint)
        }

        return startY + rowHeight
    }

    private fun drawFooter(canvas: Canvas, paint: Paint, startY: Float, text: String) {
        val boxHeight = 40f
        val rect = RectF(20f, startY, PAGE_WIDTH - 20f, startY + boxHeight)

        paint.color = Color.parseColor("#FFF8E1") // Light Warm Amber
        canvas.drawRoundRect(rect, 6f, 6f, paint)

        paint.color = Color.parseColor("#F57F17")
        paint.strokeWidth = 1f
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(rect, 6f, 6f, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#5D4037")
        paint.textSize = 8.5f
        paint.isFakeBoldText = true

        canvas.drawText("SETTLEMENT NOTICE / SUMMARY NOTES:", 30f, startY + 16f, paint)

        paint.textSize = 8f
        paint.isFakeBoldText = false
        canvas.drawText(text, 30f, startY + 30f, paint)
    }

    private fun openPdf(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            if (viewIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(viewIntent)
            } else {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Open or Share Statement PDF"))
            }

            Toast.makeText(context, "PDF Statement generated successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "PDF saved to Downloads folder.", Toast.LENGTH_SHORT).show()
        }
    }
}
