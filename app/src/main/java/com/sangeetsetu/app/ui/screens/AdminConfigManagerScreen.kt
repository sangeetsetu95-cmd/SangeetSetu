package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.sangeetsetu.app.model.ConfigItem
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.viewmodel.ConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminConfigManagerScreen(
    collectionName: String,
    title: String,
    onBack: () -> Unit,
    viewModel: ConfigViewModel = hiltViewModel()
) {
    val states by viewModel.states.collectAsStateWithLifecycle()
    val districts by viewModel.districts.collectAsStateWithLifecycle()
    val specialities by viewModel.specialities.collectAsStateWithLifecycle()
    val instruments by viewModel.instruments.collectAsStateWithLifecycle()
    val eventTypes by viewModel.eventTypes.collectAsStateWithLifecycle()
    val languages by viewModel.languages.collectAsStateWithLifecycle()
    val experienceLevels by viewModel.experienceLevels.collectAsStateWithLifecycle()

    val items = when(collectionName) {
        "states" -> states
        "districts" -> districts
        "specialities" -> specialities
        "instruments" -> instruments
        "event_types" -> eventTypes
        "languages" -> languages
        "experience_levels" -> experienceLevels
        else -> emptyList()
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ConfigItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, color = PremiumWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = PremiumWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground),
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, null, tint = PremiumGold)
                    }
                }
            )
        },
        containerColor = AppBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (items.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("No items found. Click + to add.", color = SecondaryText)
                    }
                }
            }
            items(items) { item ->
                ConfigItemCard(
                    item = item,
                    onEdit = { editingItem = it },
                    onDelete = { viewModel.deleteConfigItem(collectionName, it.id) }
                )
            }
        }
    }

    if (showAddDialog || editingItem != null) {
        ConfigEditDialog(
            item = editingItem,
            onDismiss = { 
                showAddDialog = false
                editingItem = null
            },
            onSave = { 
                viewModel.saveConfigItem(collectionName, it)
                showAddDialog = false
                editingItem = null
            }
        )
    }
}

@Composable
fun ConfigItemCard(
    item: ConfigItem,
    onEdit: (ConfigItem) -> Unit,
    onDelete: (ConfigItem) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBackground,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (item.isActive) BorderColor else Color.Red.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, color = PremiumWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                if (!item.description.isNullOrEmpty()) {
                    Text(item.description, color = SecondaryText, fontSize = 12.sp)
                }
                if (item.value.isNotEmpty()) {
                    Text("Value: ${item.value}", color = PremiumGold, fontSize = 12.sp)
                }
            }
            
            IconButton(onClick = { onEdit(item) }) {
                Icon(Icons.Default.Edit, null, tint = PremiumGold)
            }
            IconButton(onClick = { onDelete(item) }) {
                Icon(Icons.Default.Delete, null, tint = Color.Red.copy(0.7f))
            }
        }
    }
}

@Composable
fun ConfigEditDialog(
    item: ConfigItem?,
    onDismiss: () -> Unit,
    onSave: (ConfigItem) -> Unit
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var value by remember { mutableStateOf(item?.value ?: "") }
    var description by remember { mutableStateOf(item?.description ?: "") }
    var order by remember { mutableStateOf(item?.order?.toString() ?: "0") }
    var isActive by remember { mutableStateOf(item?.isActive ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Add New" else "Edit Item", color = PremiumGold) },
        containerColor = CardBackground,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name", color = AdminSecondaryText) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PremiumWhite,
                        unfocusedTextColor = PremiumWhite,
                        focusedBorderColor = PremiumGold,
                        unfocusedBorderColor = Color.White.copy(0.1f)
                    )
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Value (Optional)", color = AdminSecondaryText) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PremiumWhite,
                        unfocusedTextColor = PremiumWhite,
                        focusedBorderColor = PremiumGold,
                        unfocusedBorderColor = Color.White.copy(0.1f)
                    )
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", color = AdminSecondaryText) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PremiumWhite,
                        unfocusedTextColor = PremiumWhite,
                        focusedBorderColor = PremiumGold,
                        unfocusedBorderColor = Color.White.copy(0.1f)
                    )
                )
                OutlinedTextField(
                    value = order,
                    onValueChange = { order = it },
                    label = { Text("Display Order", color = AdminSecondaryText) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PremiumWhite,
                        unfocusedTextColor = PremiumWhite,
                        focusedBorderColor = PremiumGold,
                        unfocusedBorderColor = Color.White.copy(0.1f)
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isActive, onCheckedChange = { isActive = it }, colors = CheckboxDefaults.colors(checkedColor = PremiumGold))
                    Text("Active", color = PremiumWhite)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val newItem = if (item != null) {
                        item.copy(name = name, value = value, description = description, order = order.toIntOrNull() ?: 0, isActive = isActive)
                    } else {
                        ConfigItem(name = name, value = value, description = description, order = order.toIntOrNull() ?: 0, isActive = isActive)
                    }
                    onSave(newItem)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PremiumGold)
            ) {
                Text("Save", color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SecondaryText)
            }
        }
    )
}
