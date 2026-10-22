package com.example.splitify.domain.usecase

import java.util.Locale

class GenerateShareTextUseCase {
    operator fun invoke(title: String, amount: Double, count: Int, perPerson: Double): String {
        val displayTitle = title.ifBlank { "Expense" }
        return "Split for $displayTitle: Total ₹${String.format(Locale.getDefault(), "%.2f", amount)} between $count people -> ₹${String.format(Locale.getDefault(), "%.2f", perPerson)} each"
    }
}
