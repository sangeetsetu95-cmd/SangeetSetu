package com.sangeetsetu.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.sangeetsetu.app.model.SubscriptionPlan
import com.sangeetsetu.app.navigation.Screen
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.viewmodel.AdminViewModel

/**
 * Administrative screen for managing subscription plans.
 * 
 * This screen allows administrators to:
 * 1. View all active subscription plans.
 * 2. Create new subscription plans by specifying name, price, duration, and description.
 * 3. Edit existing plans (logic placeholder).
 *
 * It uses a premium luxury theme consistent with the rest of the application.
 *
 * @param onBack Callback to navigate back to the previous screen.
 * @param onNavigate Callback to navigate to other administrative sections.
 * @param viewModel ViewModel providing the list of plans and operations to save them.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSubscriptionScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val plans by viewModel.subscriptionPlans.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedPlanForEdit by remember { mutableStateOf<SubscriptionPlan?>(null) }

    LaunchedEffect(Unit) { viewModel.fetchSubscriptionPlans() }

    if (showAddDialog || selectedPlanForEdit != null) {
        AddSubscriptionDialog(
            plan = selectedPlanForEdit,
            onDismiss = { showAddDialog = false; selectedPlanForEdit = null },
            onSave = { plan ->
                viewModel.saveSubscriptionPlan(plan) {
                    Toast.makeText(context, "Subscription Plan Saved", Toast.LENGTH_SHORT).show()
                    showAddDialog = false
                    selectedPlanForEdit = null
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Subscription Management", color = PremiumWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumGold)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Rounded.Add, null, tint = PremiumGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PremiumNavyDark)
            )
        },
        bottomBar = {
            AdminBottomNav(currentRoute = Screen.AdminSubscriptions.route, onNavigate = onNavigate)
        },
        containerColor = PremiumNavy
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PremiumGold)
            }
        } else if (plans.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.Star, null, tint = PremiumGray.copy(0.2f), modifier = Modifier.size(80.dp))
                    Text("No subscription plans found", color = PremiumGray)
                    Button(onClick = { showAddDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = PremiumGold)) {
                        Text("Create Plan", color = PremiumNavy)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(plans) { plan ->
                    SubscriptionPlanItem(plan, onEdit = { selectedPlanForEdit = plan })
                }
            }
        }
    }
}

/**
 * Individual item representing a subscription plan in the management list.
 * 
 * @param plan The subscription plan data model.
 * @param onEdit Callback to initiate editing this plan.
 */
@Composable
fun SubscriptionPlanItem(plan: SubscriptionPlan, onEdit: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = PremiumNavyLight,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(plan.name, color = PremiumGold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("${plan.durationMonths} Months Validity", color = PremiumGray, fontSize = 12.sp)
                }
            }
            
            HorizontalDivider(color = Color.White.copy(0.05f), modifier = Modifier.padding(vertical = 12.dp))
            
            Text(plan.description, color = PremiumWhite, fontSize = 13.sp)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { onEdit() }) {
                    Text("Edit Plan", color = PremiumGold)
                }
            }
        }
    }
}

/**
 * Dialog for adding or editing a subscription plan in the system.
 * 
 * @param plan The existing plan to edit, or null for a new plan.
 * @param onDismiss Callback to close the dialog.
 * @param onSave Callback when the admin confirms saving the plan.
 */
@Composable
fun AddSubscriptionDialog(
    plan: SubscriptionPlan? = null,
    onDismiss: () -> Unit,
    onSave: (SubscriptionPlan) -> Unit
) {
    var name by remember { mutableStateOf(plan?.name ?: "") }
    var duration by remember { mutableStateOf(plan?.durationMonths?.toString() ?: "1") }
    var description by remember { mutableStateOf(plan?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PremiumNavyLight,
        title = { Text(if (plan == null) "New Subscription Plan" else "Edit Subscription Plan", color = PremiumGold, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SubscriptionEditField("Plan Name", name) { name = it }
                SubscriptionEditField("Duration (Months)", duration) { duration = it }
                SubscriptionEditField("Description", description) { description = it }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val d = duration.toIntOrNull() ?: 1
                    val finalPlan = (plan ?: SubscriptionPlan()).copy(
                        name = name, 
                        durationMonths = d, 
                        description = description
                    )
                    onSave(finalPlan)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PremiumGold)
            ) {
                Text("Save Plan", color = PremiumNavy, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = PremiumGray)
            }
        }
    )
}

/**
 * Specialized text field for editing subscription plan properties.
 */
@Composable
fun SubscriptionEditField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(label, color = PremiumGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(color = PremiumWhite, fontSize = 14.sp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PremiumGold,
                unfocusedBorderColor = CardBorder
            )
        )
    }
}

