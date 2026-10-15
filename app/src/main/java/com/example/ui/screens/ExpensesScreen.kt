package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import com.example.data.model.Expense
import com.example.data.model.Member
import com.example.ui.components.CommunityCompactAdCard
import com.example.ui.components.ExpenseItemCard
import com.example.ui.theme.ColorExpenseRed
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.SapphireNavy

@Composable
fun ExpensesScreen(
    expenses: List<Expense>,
    searchQuery: String,
    categoryFilter: String,
    timePeriodFilter: String,
    memberFilter: Long? = null,
    members: List<Member> = emptyList(),
    activeMember: Member? = null,
    categoryBudgets: Map<String, Double> = emptyMap(),
    isAdmin: Boolean,
    isProSubscriber: Boolean = false,
    onNavigateToSubscription: (() -> Unit)? = null,
    onSearchChange: (String) -> Unit,
    onCategoryFilterChange: (String) -> Unit,
    onTimePeriodChange: (String) -> Unit,
    onMemberFilterChange: (Long?) -> Unit = {},
    onManageBudgetsClick: () -> Unit = {},
    onAddExpenseClick: () -> Unit,
    onOpenExpenseForm: (() -> Unit)? = null,
    onEditExpense: (Expense) -> Unit,
    onDeleteExpense: (Expense) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current

    val categories = listOf(
        "ALL",
        "Events & Festivals",
        "Maintenance",
        "Repairs",
        "Utilities",
        "Refreshments",
        "Admin & Legal",
        "Miscellaneous"
    )

    val totalExpensesAmount = expenses.sumOf { it.amount }

    Scaffold(
        modifier = modifier.testTag("expenses_screen"),
        floatingActionButton = {
            AnimatedVisibility(
                visible = isAdmin || activeMember != null,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
                label = "fab_add_expense_visibility"
            ) {
                FloatingActionButton(
                    onClick = { onOpenExpenseForm?.invoke() ?: onAddExpenseClick() },
                    containerColor = ColorExpenseRed,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_expense")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = strings.addExpenseFab)
                }
            }
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth >= 680.dp
            val isTabletExpanded = maxWidth >= 900.dp
            val gridColumns = when {
                isTabletExpanded -> 3
                isWideScreen -> 2
                else -> 1
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = if (isWideScreen) 24.dp else 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Search Bar and Quick Actions (Adaptive layout)
                if (isWideScreen && isAdmin) {
                    val activeCatBudget = if (categoryFilter != "ALL") categoryBudgets[categoryFilter] ?: 0.0 else 0.0
                    val activeCatSpent = if (categoryFilter != "ALL") expenses.filter { it.category.equals(categoryFilter, ignoreCase = true) }.sumOf { it.amount } else 0.0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchChange,
                            placeholder = { Text(strings.searchExpensesPlaceholder) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchChange("") }) {
                                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("expense_search_input"),
                            singleLine = true
                        )

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Savings, contentDescription = null, tint = SapphireNavy, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (categoryFilter != "ALL" && activeCatBudget > 0) {
                                        val pct = (activeCatSpent / activeCatBudget).coerceIn(0.0, 1.0).toFloat()
                                        Column {
                                            Text(
                                                text = "$categoryFilter: ₹${String.format("%,.0f", activeCatSpent)} / ₹${String.format("%,.0f", activeCatBudget)}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (activeCatSpent > activeCatBudget) ColorExpenseRed else MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            LinearProgressIndicator(
                                                progress = { pct },
                                                modifier = Modifier
                                                    .fillMaxWidth(0.9f)
                                                    .height(4.dp),
                                                color = if (activeCatSpent > activeCatBudget) ColorExpenseRed else SapphireNavy,
                                                trackColor = MaterialTheme.colorScheme.outlineVariant
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = strings.categoryBudgetsTitle,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = onManageBudgetsClick,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("btn_manage_category_budgets")
                                ) {
                                    Text(strings.setBudgetsBtn, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }
                } else {
                    // Mobile Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text(strings.searchExpensesPlaceholder) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_search_input"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    categories.forEach { cat ->
                        val isSelected = categoryFilter.equals(cat, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) ColorExpenseRed else MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { onCategoryFilterChange(cat) }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (cat == "ALL") strings.filterAll else cat,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Member Filter Chips Row
                if (members.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👤 ${strings.filterByMember}:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // All Members chip
                        val isAllSelected = memberFilter == null || memberFilter == 0L
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isAllSelected) SapphireNavy else MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { onMemberFilterChange(null) }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("filter_expense_all_members")
                        ) {
                            Text(
                                text = strings.allMembersFilter,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isAllSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // My Records chip (if activeMember is present)
                        if (activeMember != null) {
                            val isMyRecords = memberFilter == activeMember.id
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isMyRecords) SapphireNavy else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        onMemberFilterChange(if (isMyRecords) null else activeMember.id)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                    .testTag("filter_expense_my_records")
                            ) {
                                Text(
                                    text = "⭐ ${strings.myEntriesOnly}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isMyRecords) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isMyRecords) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Individual Committee Members
                        members.forEach { member ->
                            val isSelected = memberFilter == member.id
                            val shortName = member.name.split(" ").firstOrNull() ?: member.name
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSelected) SapphireNavy else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        onMemberFilterChange(if (isSelected) null else member.id)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                    .testTag("filter_expense_member_${member.id}")
                            ) {
                                Text(
                                    text = "$shortName (${member.role.split(" ").firstOrNull() ?: "Member"})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Summary row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${expenses.size} ${strings.expensesTitle}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${strings.totalLabel}: ₹ ${String.format("%,.0f", totalExpensesAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = ColorExpenseRed
                    )
                }

                // Category Budget Caps Bar (if mobile and filtered or if budgets configured)
                if (isAdmin && !isWideScreen) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val activeCatBudget = if (categoryFilter != "ALL") categoryBudgets[categoryFilter] ?: 0.0 else 0.0
                    val activeCatSpent = if (categoryFilter != "ALL") expenses.filter { it.category.equals(categoryFilter, ignoreCase = true) }.sumOf { it.amount } else 0.0

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Savings, contentDescription = null, tint = SapphireNavy, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                if (categoryFilter != "ALL" && activeCatBudget > 0) {
                                    val pct = (activeCatSpent / activeCatBudget).coerceIn(0.0, 1.0).toFloat()
                                    Column {
                                        Text(
                                            text = "$categoryFilter: ₹${String.format("%,.0f", activeCatSpent)} / ₹${String.format("%,.0f", activeCatBudget)} (${(pct * 100).toInt()}%)",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (activeCatSpent > activeCatBudget) ColorExpenseRed else MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        LinearProgressIndicator(
                                            progress = { pct },
                                            modifier = Modifier
                                                .fillMaxWidth(0.9f)
                                                .height(4.dp),
                                            color = if (activeCatSpent > activeCatBudget) ColorExpenseRed else SapphireNavy,
                                            trackColor = MaterialTheme.colorScheme.outlineVariant
                                        )
                                    }
                                } else {
                                    Text(
                                        text = strings.categoryBudgetsTitle,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = onManageBudgetsClick,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .height(28.dp)
                                    .testTag("btn_manage_category_budgets")
                            ) {
                                Text(strings.setBudgetsBtn, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (expenses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = strings.noExpensesFound,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = strings.tryClearingFilters,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(gridColumns),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(expenses, key = { it.id }) { expense ->
                            ExpenseItemCard(
                                expense = expense,
                                isAdmin = isAdmin,
                                onEdit = onEditExpense,
                                onDelete = onDeleteExpense
                            )
                        }
                    }
                }
            }
        }
    }
}
