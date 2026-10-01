package com.example.expencetracker.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expencetracker.ThemeToggleButton
import com.example.expencetracker.data.ExpenseEntity
import com.example.expencetracker.data.FriendEntity
import com.example.expencetracker.util.PdfExporter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: ExpenseViewModel
) {
    val allExpenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val allFriends by viewModel.allFriends.collectAsStateWithLifecycle()

    val analyticsTargetType by viewModel.analyticsTargetType.collectAsStateWithLifecycle()
    val analyticsSelectedFriendId by viewModel.analyticsSelectedFriendId.collectAsStateWithLifecycle()

    val targetTotalSpent by viewModel.targetTotalSpent.collectAsStateWithLifecycle()
    val targetAverageExpense by viewModel.targetAverageExpense.collectAsStateWithLifecycle()
    val targetBiggestExpense by viewModel.targetBiggestExpense.collectAsStateWithLifecycle()
    val targetCategorySpendingMap by viewModel.targetCategorySpendingMap.collectAsStateWithLifecycle()
    val targetPaymentMethodSpendingMap by viewModel.targetPaymentMethodSpendingMap.collectAsStateWithLifecycle()

    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Spending Analytics",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Expense Insights & Breakdown",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (allExpenses.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                PdfExporter.exportOverallStatementPdf(
                                    context = context,
                                    expenses = allExpenses,
                                    totalSpent = targetTotalSpent,
                                    averageExpense = targetAverageExpense
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "Export Full PDF Statement",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    ThemeToggleButton(viewModel = viewModel)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (allExpenses.isEmpty()) {
            EmptyAnalyticsView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Analytics View Filter Chips (Overall, My Share, Friend Personal)
                item {
                    AnalyticsTargetFilterRow(
                        currentTargetType = analyticsTargetType,
                        currentFriendId = analyticsSelectedFriendId,
                        friends = allFriends,
                        onSelectTarget = { type, friendId ->
                            viewModel.setAnalyticsTarget(type, friendId)
                        }
                    )
                }

                // Export PDF Banner Button
                item {
                    Button(
                        onClick = {
                            PdfExporter.exportOverallStatementPdf(
                                context = context,
                                expenses = allExpenses,
                                totalSpent = targetTotalSpent,
                                averageExpense = targetAverageExpense
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Export PDF Report",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                // Key Metrics Summary Cards Row
                item {
                    AnalyticsOverviewCard(
                        totalSpent = targetTotalSpent,
                        averageExpense = targetAverageExpense,
                        totalTransactions = allExpenses.size,
                        targetType = analyticsTargetType
                    )
                }

                // Biggest Expense Highlight Card
                targetBiggestExpense?.let { expense ->
                    item {
                        BiggestExpenseCard(expense = expense)
                    }
                }

                // Category Pie Chart Card
                item {
                    CategoryPieChartCard(
                        categorySpendingMap = targetCategorySpendingMap,
                        totalSpent = targetTotalSpent
                    )
                }

                // Payment Method Breakdown Card
                item {
                    PaymentMethodBreakdownCard(
                        paymentMethodSpendingMap = targetPaymentMethodSpendingMap,
                        totalSpent = targetTotalSpent
                    )
                }
            }
        }
    }
}

@Composable
fun AnalyticsTargetFilterRow(
    currentTargetType: AnalyticsTargetType,
    currentFriendId: Long?,
    friends: List<FriendEntity>,
    onSelectTarget: (AnalyticsTargetType, Long?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "VIEW ANALYTICS FOR",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    val isSelected = currentTargetType == AnalyticsTargetType.OVERALL
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectTarget(AnalyticsTargetType.OVERALL, null) },
                        label = { Text("Overall App Expenses") },
                        trailingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null
                    )
                }

                item {
                    val isSelected = currentTargetType == AnalyticsTargetType.MY_SHARE
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectTarget(AnalyticsTargetType.MY_SHARE, null) },
                        label = { Text("Only My Personal Share") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        trailingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                        )
                    )
                }

                items(friends) { friend ->
                    val isSelected = currentTargetType == AnalyticsTargetType.FRIEND && currentFriendId == friend.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectTarget(AnalyticsTargetType.FRIEND, friend.id) },
                        label = { Text("${friend.name}'s Share") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        trailingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                            selectedLabelColor = MaterialTheme.colorScheme.onTertiary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AnalyticsOverviewCard(
    totalSpent: Double,
    averageExpense: Double,
    totalTransactions: Int,
    targetType: AnalyticsTargetType
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            val titleLabel = when (targetType) {
                AnalyticsTargetType.OVERALL -> "OVERALL APP TOTAL SPENDING"
                AnalyticsTargetType.MY_SHARE -> "MY PERSONAL SHARE SPENDING"
                AnalyticsTargetType.FRIEND -> "FRIEND PERSONAL SHARE SPENDING"
            }

            Text(
                text = titleLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = String.format(Locale.getDefault(), "₹%.2f", totalSpent),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Average / Transaction",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "₹%.2f", averageExpense),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Transactions",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "$totalTransactions Record${if (totalTransactions != 1) "s" else ""}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun BiggestExpenseCard(
    expense: ExpenseEntity
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BIGGEST EXPENSE",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expense.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${expense.category} · Paid by ${expense.paidBy} via ${expense.paymentMethod}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = String.format(Locale.getDefault(), "₹%.2f", expense.totalAmount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryPieChartCard(
    categorySpendingMap: Map<String, Double>,
    totalSpent: Double
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(categorySpendingMap) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    val categoryColors: Map<String, Color> = remember(categorySpendingMap) {
        categorySpendingMap.keys.associateWith { category ->
            getCategoryColorNonComposable(category)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CATEGORY SPENDING PIE CHART",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (totalSpent > 0 && categorySpendingMap.isNotEmpty()) {
                // Pie / Doughnut Canvas Chart
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(190.dp)) {
                        val strokeWidth = 36.dp.toPx()
                        var startAngle = -90f

                        categorySpendingMap.forEach { (category, amount) ->
                            val sweepAngle = ((amount / totalSpent) * 360f).toFloat() * progress.value
                            val color = categoryColors[category] ?: Color(0xFF6200EE)

                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidth)
                            )

                            startAngle += sweepAngle
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TOTAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "₹%.0f", totalSpent),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Legend Items
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categorySpendingMap.forEach { (category, amount) ->
                        val percentage = if (totalSpent > 0) (amount / totalSpent) * 100 else 0.0
                        val color = categoryColors[category] ?: Color(0xFF6200EE)
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$category: ₹${String.format(Locale.getDefault(), "%.0f", amount)} (${String.format(Locale.getDefault(), "%.1f", percentage)}%)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            } else {
                Text("No category data to display")
            }
        }
    }
}

@Composable
fun PaymentMethodBreakdownCard(
    paymentMethodSpendingMap: Map<String, Double>,
    totalSpent: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Payment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PAYMENT METHOD BREAKDOWN",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (totalSpent > 0 && paymentMethodSpendingMap.isNotEmpty()) {
                paymentMethodSpendingMap.forEach { (method, amount) ->
                    val ratio = (amount / totalSpent).toFloat()
                    val percentage = ratio * 100

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = getPaymentMethodIcon(method),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = method,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "₹${String.format(Locale.getDefault(), "%.2f", amount)} (${String.format(Locale.getDefault(), "%.1f", percentage)}%)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { ratio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    }
                }
            } else {
                Text("No payment method data available")
            }
        }
    }
}

@Composable
fun EmptyAnalyticsView(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No analytics available yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Add some expenses first to view detailed spending pie charts, payment breakdowns, and top expenses!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

fun getCategoryColorNonComposable(category: String): Color {
    return when (category.lowercase(Locale.getDefault())) {
        "food" -> Color(0xFFFF9800)
        "travel" -> Color(0xFF2196F3)
        "stay" -> Color(0xFF9C27B0)
        "shopping" -> Color(0xFFE91E63)
        "entertainment" -> Color(0xFFFF5722)
        else -> Color(0xFF673AB7)
    }
}
