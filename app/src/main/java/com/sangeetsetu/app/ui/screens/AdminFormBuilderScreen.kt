package com.sangeetsetu.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sangeetsetu.app.model.*
import com.sangeetsetu.app.viewmodel.AdminFormViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFormBuilderScreen(
    viewModel: AdminFormViewModel = viewModel(),
    onPreview: () -> Unit = {}
) {
    val fields by viewModel.fields.collectAsState()
    val uiSettings by viewModel.uiSettings.collectAsState()
    val regSettings by viewModel.regSettings.collectAsState()
    val categories by viewModel.categories.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    var showFieldDialog by remember { mutableStateOf<FormField?>(null) }
    val tabs = listOf("Form Builder", "Reg Settings", "UI Design")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registration Manager", color = Color.White) },
                actions = {
                    Button(
                        onClick = onPreview,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Visibility, null, tint = Color.Black)
                        Spacer(Modifier.width(4.dp))
                        Text("Preview", color = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1A1F))
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showFieldDialog = FormField(displayOrder = fields.size) },
                    containerColor = Color(0xFFD4AF37),
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.Black)
                }
            }
        },
        containerColor = Color(0xFF0B0B0F)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1A1A1F),
                contentColor = Color(0xFFD4AF37)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> FormBuilderTab(fields, viewModel, onEdit = { showFieldDialog = it })
                    1 -> RegistrationSettingsTab(regSettings, viewModel)
                    2 -> UISettingsTab(uiSettings, viewModel)
                }
            }
        }
    }

    if (showFieldDialog != null) {
        FieldConfigDialog(
            field = showFieldDialog!!,
            categories = categories,
            onDismiss = { showFieldDialog = null },
            onSave = { 
                if (showFieldDialog!!.id.isEmpty()) viewModel.addField(it) else viewModel.updateField(it)
                showFieldDialog = null 
            }
        )
    }
}

@Composable
fun FormBuilderTab(
    fields: List<FormField>,
    viewModel: AdminFormViewModel,
    onEdit: (FormField) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(fields.sortedBy { it.displayOrder }) { index, field ->
            AdminFieldItem(
                field = field,
                onEdit = { onEdit(field) },
                onDelete = { viewModel.deleteField(field.id) },
                onMoveUp = { if (index > 0) viewModel.reorderFields(index, index - 1) },
                onMoveDown = { if (index < fields.size - 1) viewModel.reorderFields(index, index + 1) }
            )
        }
    }
}

@Composable
fun AdminFieldItem(
    field: FormField,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (field.isEnabled) Color.White.copy(0.1f) else Color.Red.copy(0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(field.label, color = Color.White, fontWeight = FontWeight.Bold)
                Text("${field.type.name} • ${if (field.isRequired) "Required" else "Optional"}", color = Color.White.copy(0.6f), fontSize = 12.sp)
                if (field.categoryFilter.isNotEmpty()) {
                    Text("Filters: ${field.categoryFilter.joinToString()}", color = Color(0xFFD4AF37), fontSize = 10.sp)
                }
            }
            
            Row {
                IconButton(onClick = onMoveUp) { Icon(Icons.Default.ArrowUpward, null, tint = Color.White) }
                IconButton(onClick = onMoveDown) { Icon(Icons.Default.ArrowDownward, null, tint = Color.White) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
            }
        }
    }
}

@Composable
fun FieldConfigDialog(
    field: FormField,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (FormField) -> Unit
) {
    var label by remember { mutableStateOf(field.label) }
    var placeholder by remember { mutableStateOf(field.placeholder) }
    var type by remember { mutableStateOf(field.type) }
    var isRequired by remember { mutableStateOf(field.isRequired) }
    var isEnabled by remember { mutableStateOf(field.isEnabled) }
    var optionsText by remember { mutableStateOf(field.options.joinToString(", ")) }
    var selectedCats by remember { mutableStateOf(field.categoryFilter.toSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (field.id.isEmpty()) "Add Field" else "Edit Field") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.heightIn(max = 400.dp)) {
                item {
                    OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Field Label") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = placeholder, onValueChange = { placeholder = it }, label = { Text("Placeholder") }, modifier = Modifier.fillMaxWidth())
                }
                
                item {
                    Text("Field Type")
                    FormFieldType.values().forEach { t ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { type = t }) {
                            RadioButton(selected = type == t, onClick = { type = t })
                            Text(t.name)
                        }
                    }
                }

                if (type == FormFieldType.DROPDOWN || type == FormFieldType.MULTI_SELECT || type == FormFieldType.RADIO_BUTTON) {
                    item {
                        OutlinedTextField(
                            value = optionsText,
                            onValueChange = { optionsText = it },
                            label = { Text("Options (comma separated)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isRequired, onCheckedChange = { isRequired = it })
                        Text("Is Required")
                        Spacer(Modifier.width(20.dp))
                        Checkbox(checked = isEnabled, onCheckedChange = { isEnabled = it })
                        Text("Is Enabled")
                    }
                }

                item {
                    Text("Show only for Categories:")
                    categories.forEach { cat ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = selectedCats.contains(cat.name),
                                onCheckedChange = { 
                                    selectedCats = if (it) selectedCats + cat.name else selectedCats - cat.name 
                                }
                            )
                            Text(cat.name)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                onSave(field.copy(
                    label = label,
                    placeholder = placeholder,
                    type = type,
                    isRequired = isRequired,
                    isEnabled = isEnabled,
                    options = optionsText.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    categoryFilter = selectedCats.toList()
                )) 
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun RegistrationSettingsTab(settings: RegistrationSettings, viewModel: AdminFormViewModel) {
    var s by remember { mutableStateOf(settings) }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = s.otpEnabled, onCheckedChange = { s = s.copy(otpEnabled = it) })
            Text("Enable OTP Verification", color = Color.White, modifier = Modifier.padding(start = 12.dp))
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = s.autoApproval, onCheckedChange = { s = s.copy(autoApproval = it) })
            Text("Auto Approval of Artists", color = Color.White, modifier = Modifier.padding(start = 12.dp))
        }

        OutlinedTextField(
            value = s.termsAndConditions,
            onValueChange = { s = s.copy(termsAndConditions = it) },
            label = { Text("Terms & Conditions") },
            modifier = Modifier.fillMaxWidth().height(150.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        Button(
            onClick = { viewModel.saveRegSettings(s) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
        ) {
            Text("Save Registration Settings", color = Color.Black)
        }
    }
}

@Composable
fun UISettingsTab(settings: AppUISettings, viewModel: AdminFormViewModel) {
    var s by remember { mutableStateOf(settings) }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(value = s.headerTitle, onValueChange = { s = s.copy(headerTitle = it) }, label = { Text("App Name / Header Title") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
        OutlinedTextField(value = s.headerSubtitle, onValueChange = { s = s.copy(headerSubtitle = it) }, label = { Text("Subtitle") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
        
        OutlinedTextField(value = s.primaryColor, onValueChange = { s = s.copy(primaryColor = it) }, label = { Text("Primary Color (Hex)") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
        OutlinedTextField(value = s.backgroundColor, onValueChange = { s = s.copy(backgroundColor = it) }, label = { Text("Background Color (Hex)") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = s.showHeroBanner, onCheckedChange = { s = s.copy(showHeroBanner = it) })
            Text("Show Hero Banner", color = Color.White, modifier = Modifier.padding(start = 12.dp))
        }

        Button(
            onClick = { viewModel.saveUISettings(s) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
        ) {
            Text("Save UI Settings", color = Color.Black)
        }
    }
}
