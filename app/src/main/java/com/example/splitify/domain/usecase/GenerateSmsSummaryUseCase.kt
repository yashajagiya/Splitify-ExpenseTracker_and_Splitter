package com.example.splitify.domain.usecase

import com.example.splitify.domain.model.Expense
import java.util.Locale

class GenerateSmsSummaryUseCase {
    operator fun invoke(friendName: String, expenses: List<Expense>, totalOwed: Double): String {
        val builder = StringBuilder()
        builder.append("Hi $friendName, here is your expense balance summary from SplitFlow:\n\n")

        if (expenses.isEmpty()) {
            builder.append("No active expenses linked to your profile.\n")
        } else {
            expenses.forEachIndexed { index, exp ->
                val payerText = if (exp.paidBy.equals("You", ignoreCase = true)) "Paid by You" else "Paid by ${exp.paidBy}"
                builder.append("${index + 1}. ${exp.title} ($payerText via ${exp.paymentMethod}): ₹${String.format(Locale.getDefault(), "%.2f", exp.perPersonAmount)} share\n")
            }
            builder.append("\n------------------------\n")
            if (totalOwed >= 0) {
                builder.append("TOTAL AMOUNT TO PAY: ₹${String.format(Locale.getDefault(), "%.2f", totalOwed)}\n")
            } else {
                builder.append("TOTAL AMOUNT YOU ARE OWED: ₹${String.format(Locale.getDefault(), "%.2f", -totalOwed)}\n")
            }
        }

        builder.append("\nPlease settle the balance when possible. Thank you!")
        return builder.toString()
    }
}
