package com.sangeetsetu.app.ui.screens

import android.net.Uri
import kotlin.math.cos
import kotlin.math.sin
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.ui.res.stringResource
import com.sangeetsetu.app.R
import com.sangeetsetu.app.model.AppUISettings
import com.sangeetsetu.app.model.Category
import com.sangeetsetu.app.model.District
import com.sangeetsetu.app.model.FormField
import com.sangeetsetu.app.model.FormFieldType
import com.sangeetsetu.app.model.Instrument
import com.sangeetsetu.app.model.State as AppState
import com.sangeetsetu.app.ui.theme.LocalStrings
import com.sangeetsetu.app.ui.theme.NotoSansDevanagariFamily
import com.sangeetsetu.app.viewmodel.RegistrationViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicRegistrationScreen(
    viewModel: RegistrationViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val formFields by viewModel.formFields.collectAsState()
    val uiSettings by viewModel.uiSettings.collectAsState()
    val regSettings by viewModel.registrationSettings.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val errors by viewModel.errors.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isFormValid by viewModel.isFormValid.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val instruments by viewModel.instruments.collectAsState()
    val states by viewModel.states.collectAsState()
    val districts by viewModel.districts.collectAsState()
    val strings = LocalStrings.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    val gold = Color(android.graphics.Color.parseColor(uiSettings.primaryColor))
    val bg = Color(android.graphics.Color.parseColor(uiSettings.backgroundColor))

    LaunchedEffect(Unit) {
        viewModel.registrationResult.collectLatest { result ->
            result.onSuccess {
                showSuccessDialog = true
            }.onFailure {
                snackbarHostState.showSnackbar(it.message ?: "Submission failed")
            }
        }
    }

    if (showSuccessDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { onNavigateBack() },
            containerColor = Color(0xFF1E1E24),
            title = { Text(strings.registrationTitle, color = gold, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.registration_success_msg), color = Color.White) },
            confirmButton = {
                Button(
                    onClick = { onNavigateBack() },
                    colors = ButtonDefaults.buttonColors(containerColor = gold)
                ) {
                    Text(stringResource(R.string.ok), color = Color.Black)
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(bg)) {
        // Premium Background Texture
        RegistrationMandalaBackground()
        
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(uiSettings.headerTitle, color = gold, fontWeight = FontWeight.Black, fontFamily = NotoSansDevanagariFamily)
                            Text(uiSettings.headerSubtitle, color = Color.White.copy(0.7f), fontSize = 10.sp, fontFamily = NotoSansDevanagariFamily)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = gold)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            if (isLoading && formFields.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = gold)
                }
            } else if (!isLoading && formFields.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Unable to load registration form", color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.onFieldChange("retry", System.currentTimeMillis()) },
                            colors = ButtonDefaults.buttonColors(containerColor = gold)
                        ) {
                            Text("Retry", color = Color.Black)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        RegistrationHeader(uiSettings, strings)
                    }

                    val selectedCategory = formState["category"] as? String ?: ""

                    items(formFields.sortedBy { it.displayOrder }) { field ->
                        val isApplicable = field.categoryFilter.isEmpty() || field.categoryFilter.contains(selectedCategory)
                        
                        if (isApplicable) {
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                DynamicField(
                                    field = field,
                                    value = formState[field.id] ?: "",
                                    error = errors[field.id],
                                    categories = categories,
                                    instruments = instruments,
                                    states = states,
                                    districts = districts,
                                    onValueChange = { viewModel.onFieldChange(field.id, it) },
                                    onUpload = { uri, type -> viewModel.uploadFile(field.id, uri, type) },
                                    uiSettings = uiSettings,
                                    strings = strings
                                )
                            }
                        }
                    }

                    item {
                        TermsAndConditionsSection(regSettings.termsAndConditions, gold, strings)
                    }

                    item {
                        Button(
                            onClick = { viewModel.submitRegistration() },
                            modifier = Modifier.fillMaxWidth().height(56.dp).shadow(8.dp, RoundedCornerShape(12.dp)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = gold,
                                disabledContainerColor = gold.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading && isFormValid
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                            } else {
                                Text(uiSettings.buttonColor.ifEmpty { strings.submitRegistration }, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(40.dp)) }
                }
            }
        }
    }
}

@Composable
fun RegistrationHeader(uiSettings: AppUISettings, strings: com.sangeetsetu.app.ui.theme.LanguageStrings) {
    val gold = Color(android.graphics.Color.parseColor(uiSettings.primaryColor))
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        if (uiSettings.appLogo.isNotEmpty()) {
            AsyncImage(
                model = uiSettings.appLogo,
                contentDescription = "Logo",
                modifier = Modifier.size(80.dp).clip(CircleShape).border(2.dp, gold, CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(Icons.Default.MusicNote, null, tint = gold, modifier = Modifier.size(60.dp).background(gold.copy(0.1f), CircleShape).padding(12.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(strings.registrationTitle, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = NotoSansDevanagariFamily)
        Text(strings.registrationSubtitle, color = Color.White.copy(0.6f), fontSize = 14.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(modifier = Modifier.width(60.dp), thickness = 2.dp, color = gold)
    }
}

@Composable
fun DynamicField(
    field: FormField,
    value: Any,
    error: String?,
    categories: List<Category>,
    instruments: List<Instrument>,
    states: List<AppState>,
    districts: List<District>,
    onValueChange: (Any) -> Unit,
    onUpload: (Uri, FormFieldType) -> Unit,
    uiSettings: AppUISettings,
    strings: com.sangeetsetu.app.ui.theme.LanguageStrings
) {
    if (!field.isEnabled) return
    val gold = Color(android.graphics.Color.parseColor(uiSettings.primaryColor))

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = field.label,
                color = Color.White.copy(0.9f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                fontFamily = NotoSansDevanagariFamily
            )
            if (field.isRequired) {
                Text(" *", color = Color.Red, fontSize = 14.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        when (field.type) {
            FormFieldType.TEXT, FormFieldType.EMAIL, FormFieldType.MOBILE, FormFieldType.NUMBER -> {
                PremiumTextField(
                    value = value as? String ?: "",
                    onValueChange = { onValueChange(it) },
                    placeholder = field.placeholder,
                    error = error,
                    gold = gold,
                    maxLength = field.maxLength,
                    keyboardType = when(field.type) {
                        FormFieldType.NUMBER -> androidx.compose.ui.text.input.KeyboardType.Number
                        FormFieldType.MOBILE -> androidx.compose.ui.text.input.KeyboardType.Phone
                        FormFieldType.EMAIL -> androidx.compose.ui.text.input.KeyboardType.Email
                        else -> androidx.compose.ui.text.input.KeyboardType.Text
                    }
                )
            }
            FormFieldType.MULTILINE_TEXT -> {
                PremiumTextField(
                    value = value as? String ?: "",
                    onValueChange = { onValueChange(it) },
                    placeholder = field.placeholder,
                    error = error,
                    gold = gold,
                    singleLine = false,
                    maxLength = field.maxLength,
                    modifier = Modifier.height(120.dp)
                )
            }
            FormFieldType.DROPDOWN -> {
                val isCategory = field.id == "category"
                val options = if (isCategory) categories.map { it.name } else field.options
                val displayValue = if (isCategory) {
                    categories.find { it.id == value }?.name ?: value.toString()
                } else value as? String ?: ""
                
                PremiumSearchableDropdown(
                    value = displayValue,
                    options = options,
                    placeholder = field.placeholder,
                    onSelect = { selectedName -> 
                        val valToSave = if (isCategory) {
                            categories.find { it.name == selectedName }?.id ?: selectedName
                        } else selectedName
                        onValueChange(valToSave)
                    },
                    gold = gold,
                    strings = strings
                )
            }
            FormFieldType.STATE -> {
                PremiumSearchableDropdown(
                    value = value as? String ?: "",
                    options = states.map { it.name },
                    placeholder = field.placeholder.ifEmpty { strings.selectState },
                    onSelect = { onValueChange(it) },
                    gold = gold,
                    strings = strings
                )
            }
            FormFieldType.DISTRICT -> {
                PremiumSearchableDropdown(
                    value = value as? String ?: "",
                    options = districts.map { it.name },
                    placeholder = field.placeholder.ifEmpty { strings.selectDistrict },
                    onSelect = { onValueChange(it) },
                    gold = gold,
                    strings = strings
                )
            }
            FormFieldType.MULTI_SELECT -> {
                val options = if (field.id == "instruments") instruments.map { it.name } else field.options
                PremiumMultiSearchableDropdown(
                    selected = (value as? List<String>) ?: emptyList(),
                    options = options,
                    placeholder = field.placeholder,
                    onToggle = { option ->
                        val current = (value as? List<String>) ?: emptyList()
                        onValueChange(if (current.contains(option)) current - option else current + option)
                    },
                    gold = gold,
                    strings = strings
                )
            }
            FormFieldType.PHOTO_UPLOAD, FormFieldType.VIDEO_UPLOAD, FormFieldType.AUDIO_UPLOAD, FormFieldType.FILE_UPLOAD -> {
                PremiumFileUpload(
                    url = value as? String ?: "",
                    type = field.type,
                    onUpload = { onUpload(it, field.type) },
                    gold = gold,
                    strings = strings
                )
            }
            FormFieldType.MULTIPLE_PHOTOS -> {
                PremiumMultiPhotoUpload(
                    urls = (value as? List<String>) ?: emptyList(),
                    onUpload = { onUpload(it, field.type) },
                    onRemove = { url -> onValueChange(((value as? List<String>) ?: emptyList()) - url) },
                    gold = gold,
                    strings = strings
                )
            }
            else -> {}
        }
        
        if (error != null) {
            Text(error, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    gold: Color = Color.Yellow,
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
    singleLine: Boolean = true,
    maxLength: Int = 0
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = { if (maxLength == 0 || it.length <= maxLength) onValueChange(it) },
            placeholder = { Text(placeholder, color = Color.White.copy(0.3f), fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth(),
            isError = error != null,
            singleLine = singleLine,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = gold,
                unfocusedBorderColor = Color.White.copy(0.1f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.White.copy(0.05f),
                unfocusedContainerColor = Color.White.copy(0.05f),
                errorBorderColor = Color.Red
            ),
            shape = RoundedCornerShape(12.dp)
        )
        if (maxLength > 0) {
            Text(
                text = "${value.length}/$maxLength",
                color = if (value.length >= maxLength) Color.Red else Color.White.copy(0.5f),
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.End).padding(top = 4.dp, end = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumSearchableDropdown(
    value: String,
    options: List<String>,
    placeholder: String,
    onSelect: (String) -> Unit,
    gold: Color,
    strings: com.sangeetsetu.app.ui.theme.LanguageStrings
) {
    var expanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    val filteredOptions = options.filter { it.contains(searchText, ignoreCase = true) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text(placeholder, color = Color.White.copy(0.3f), fontSize = 14.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = gold,
                unfocusedBorderColor = Color.White.copy(0.1f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.White.copy(0.05f),
                unfocusedContainerColor = Color.White.copy(0.05f),
                focusedTrailingIconColor = gold,
                unfocusedTrailingIconColor = Color.White.copy(0.5f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { 
                expanded = false
                searchText = ""
            },
            modifier = Modifier.background(Color(0xFF1E1E24))
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text(strings.searchOptions, color = Color.White.copy(0.3f), fontSize = 14.sp) },
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = gold,
                    unfocusedBorderColor = Color.White.copy(0.1f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )
            
            if (filteredOptions.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No options found", color = Color.White.copy(0.5f)) },
                    onClick = { },
                    enabled = false
                )
            }

            filteredOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = Color.White) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                        searchText = ""
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumMultiSearchableDropdown(
    selected: List<String>,
    options: List<String>,
    placeholder: String,
    onToggle: (String) -> Unit,
    gold: Color,
    strings: com.sangeetsetu.app.ui.theme.LanguageStrings
) {
    var expanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    val filteredOptions = options.filter { it.contains(searchText, ignoreCase = true) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = if (selected.isEmpty()) "" else selected.joinToString(", "),
            onValueChange = {},
            readOnly = true,
            placeholder = { Text(placeholder, color = Color.White.copy(0.3f), fontSize = 14.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = gold,
                unfocusedBorderColor = Color.White.copy(0.1f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.White.copy(0.05f),
                unfocusedContainerColor = Color.White.copy(0.05f),
                focusedTrailingIconColor = gold,
                unfocusedTrailingIconColor = Color.White.copy(0.5f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { 
                expanded = false
                searchText = ""
            },
            modifier = Modifier.background(Color(0xFF1E1E24))
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text(strings.searchOptions, color = Color.White.copy(0.3f), fontSize = 14.sp) },
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = gold,
                    unfocusedBorderColor = Color.White.copy(0.1f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )
            
            filteredOptions.forEach { option ->
                val isSelected = selected.contains(option)
                DropdownMenuItem(
                    text = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null,
                                colors = CheckboxDefaults.colors(checkedColor = gold)
                            )
                            Text(option, color = Color.White, modifier = Modifier.padding(start = 8.dp))
                        }
                    },
                    onClick = { onToggle(option) },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
fun PremiumFileUpload(
    url: String,
    type: FormFieldType,
    onUpload: (Uri) -> Unit,
    gold: Color,
    strings: com.sangeetsetu.app.ui.theme.LanguageStrings
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onUpload(it) }
    }

    val mimeType = when(type) {
        FormFieldType.VIDEO_UPLOAD -> "video/*"
        FormFieldType.AUDIO_UPLOAD -> "audio/*"
        FormFieldType.FILE_UPLOAD -> "*/*"
        else -> "image/*"
    }

    Surface(
        onClick = { launcher.launch(mimeType) },
        modifier = Modifier.fillMaxWidth().height(100.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(0.05f),
        border = BorderStroke(1.dp, Color.White.copy(0.1f))
    ) {
        if (url.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (type) {
                    FormFieldType.VIDEO_UPLOAD -> {
                        Icon(Icons.Default.PlayCircle, null, tint = gold, modifier = Modifier.size(48.dp).align(Alignment.Center))
                    }
                    FormFieldType.AUDIO_UPLOAD -> {
                        Icon(Icons.Default.Audiotrack, null, tint = gold, modifier = Modifier.size(48.dp).align(Alignment.Center))
                    }
                    FormFieldType.FILE_UPLOAD -> {
                        Icon(Icons.Default.CloudUpload, null, tint = gold, modifier = Modifier.size(48.dp).align(Alignment.Center))
                    }
                    else -> {
                        AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                }
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(24.dp),
                    shape = CircleShape,
                    color = Color.Black.copy(0.6f),
                    onClick = { launcher.launch(mimeType) }
                ) {
                    Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(14.dp).padding(4.dp))
                }
            }
        } else {
            val (icon, label) = when(type) {
                FormFieldType.VIDEO_UPLOAD -> Icons.Default.VideoCall to strings.uploadVideo
                FormFieldType.AUDIO_UPLOAD -> Icons.Default.Audiotrack to strings.uploadAudio
                FormFieldType.FILE_UPLOAD -> Icons.Default.CloudUpload to strings.uploadFile
                else -> Icons.Default.AddPhotoAlternate to strings.uploadPhoto
            }
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, null, tint = gold, modifier = Modifier.size(32.dp))
                Text(label, color = Color.White.copy(0.6f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun PremiumMultiPhotoUpload(
    urls: List<String>,
    onUpload: (Uri) -> Unit,
    onRemove: (String) -> Unit,
    gold: Color,
    strings: com.sangeetsetu.app.ui.theme.LanguageStrings
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onUpload(it) }
    }

    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        urls.forEach { url ->
            Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp))) {
                AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                IconButton(
                    onClick = { onRemove(url) },
                    modifier = Modifier.align(Alignment.TopEnd).size(24.dp).background(Color.Black.copy(0.6f))
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
        
        Surface(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(0.05f),
            border = BorderStroke(1.dp, Color.White.copy(0.1f))
        ) {
            Icon(Icons.Default.Add, null, tint = gold, modifier = Modifier.padding(24.dp))
        }
    }
}

@Composable
fun TermsAndConditionsSection(terms: String, gold: Color, strings: com.sangeetsetu.app.ui.theme.LanguageStrings) {
    var checked by remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = checked,
            onCheckedChange = { checked = it },
            colors = CheckboxDefaults.colors(checkedColor = gold, uncheckedColor = Color.White.copy(0.3f))
        )
        Text(
            text = terms.ifEmpty { strings.termsAgreement },
            color = Color.White.copy(0.7f),
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun RegistrationMandalaBackground() {
    val gold = Color(0xFFD4AF37)
    Canvas(modifier = Modifier.fillMaxSize().alpha(0.06f)) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val maxRadius = size.width * 1.5f
        
        for (i in 1..8) {
            drawCircle(
                color = gold,
                radius = i * 200f,
                center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
            )
        }
        
        for (angle in 0 until 360 step 15) {
            val rad = Math.toRadians(angle.toDouble()).toFloat()
            val endX = centerX + maxRadius * cos(rad)
            val endY = centerY + maxRadius * sin(rad)
            drawLine(
                color = gold,
                start = androidx.compose.ui.geometry.Offset(centerX, centerY),
                end = androidx.compose.ui.geometry.Offset(endX, endY),
                strokeWidth = 1f
            )
        }
    }
}
