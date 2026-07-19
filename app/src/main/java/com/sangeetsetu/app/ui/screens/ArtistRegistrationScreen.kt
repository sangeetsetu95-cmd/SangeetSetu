package com.sangeetsetu.app.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.sangeetsetu.app.R
import com.sangeetsetu.app.ui.components.LocationSelector
import com.sangeetsetu.app.ui.theme.AppBackground
import com.sangeetsetu.app.ui.theme.BorderColor
import com.sangeetsetu.app.ui.theme.CardBackground
import com.sangeetsetu.app.ui.theme.PremiumGold
import com.sangeetsetu.app.ui.theme.PremiumWhite
import com.sangeetsetu.app.ui.theme.SecondaryText
import com.sangeetsetu.app.viewmodel.ArtistRegistrationViewModel
import com.sangeetsetu.app.viewmodel.RegistrationForm
import com.sangeetsetu.app.viewmodel.RegistrationStep

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ArtistRegistrationScreen(
    onBack: () -> Unit,
    onComplete: () -> Unit,
    viewModel: ArtistRegistrationViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentStep by viewModel.currentStep.collectAsStateWithLifecycle()
    val form by viewModel.form.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val specialities by viewModel.specialities.collectAsStateWithLifecycle()
    val states by viewModel.states.collectAsStateWithLifecycle()
    val districts by viewModel.districts.collectAsStateWithLifecycle()
    val languages by viewModel.languages.collectAsStateWithLifecycle()
    val experienceLevels by viewModel.experienceLevels.collectAsStateWithLifecycle()

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.artist_registration), color = PremiumWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep == RegistrationStep.BASIC_INFO) onBack()
                        else viewModel.previousStep()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
            )
        },
        containerColor = AppBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            RegistrationProgressIndicator(currentStep)

            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState.ordinal > initialState.ordinal) {
                            slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                        } else {
                            slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                        }
                    },
                    label = "stepTransition"
                ) { step ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        when (step) {
                            RegistrationStep.BASIC_INFO -> BasicInfoStep(form, viewModel::updateForm)
                            RegistrationStep.ARTIST_DETAILS -> ArtistDetailsStep(
                                form = form,
                                categories = categories,
                                specialities = specialities,
                                states = states,
                                districts = districts,
                                languages = languages,
                                experienceLevels = experienceLevels,
                                onUpdate = viewModel::updateForm
                            )
                            RegistrationStep.DOCUMENTS -> DocumentsStep(form, viewModel::updateForm)
                            RegistrationStep.REVIEW -> ReviewStep(form)
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PremiumGold)
                    }
                }
            }

            RegistrationBottomBar(
                currentStep = currentStep,
                form = form,
                isLoading = isLoading,
                onNext = {
                    if (currentStep == RegistrationStep.REVIEW) {
                        viewModel.submitRegistration {
                            Toast.makeText(context, "Registration Successful!", Toast.LENGTH_LONG).show()
                            onComplete()
                        }
                    } else {
                        viewModel.nextStep()
                    }
                },
                onBack = {
                    if (currentStep == RegistrationStep.BASIC_INFO) onBack()
                    else viewModel.previousStep()
                }
            )
        }
    }
}

@Composable
fun RegistrationProgressIndicator(currentStep: RegistrationStep) {
    val steps = RegistrationStep.entries
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val isActive = step == currentStep
            val isCompleted = index < currentStep.ordinal
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isActive || isCompleted) PremiumGold else CardBackground)
                        .border(1.dp, if (isActive) PremiumWhite else Color.Transparent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp), tint = AppBackground)
                    } else {
                        Text(
                            text = (index + 1).toString(),
                            color = if (isActive) AppBackground else SecondaryText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(step.titleResId),
                    color = if (isActive) PremiumGold else SecondaryText,
                    fontSize = 10.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                )
            }

            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .padding(horizontal = 4.dp)
                        .background(if (index < currentStep.ordinal) PremiumGold else BorderColor)
                )
            }
        }
    }
}

@Composable
fun BasicInfoStep(form: RegistrationForm, onUpdate: ((RegistrationForm) -> RegistrationForm) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text(stringResource(R.string.basic_info), color = PremiumGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        
        RegistrationTextField(
            value = form.name,
            onValueChange = { newValue -> onUpdate { f -> f.copy(name = newValue) } },
            label = stringResource(R.string.full_name),
            placeholder = "Enter your full name",
            leadingIcon = Icons.Default.Person
        )

        RegistrationTextField(
            value = form.email,
            onValueChange = { newValue -> onUpdate { f -> f.copy(email = newValue) } },
            label = stringResource(R.string.email_address),
            placeholder = "name@example.com",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email
        )

        RegistrationTextField(
            value = form.phone,
            onValueChange = { newValue -> onUpdate { f -> f.copy(phone = newValue) } },
            label = stringResource(R.string.mobile_number),
            placeholder = "+91 XXXXX XXXXX",
            leadingIcon = Icons.Default.Phone,
            keyboardType = KeyboardType.Phone
        )

        RegistrationTextField(
            value = form.whatsapp,
            onValueChange = { newValue -> onUpdate { f -> f.copy(whatsapp = newValue) } },
            label = stringResource(R.string.whatsapp_number),
            placeholder = "+91 XXXXX XXXXX",
            leadingIcon = Icons.Default.Phone,
            keyboardType = KeyboardType.Phone
        )
    }
}

@Composable
fun ArtistDetailsStep(
    form: RegistrationForm,
    categories: List<String>,
    specialities: List<String>,
    states: List<String>,
    districts: List<String>,
    languages: List<String>,
    experienceLevels: List<String>,
    onUpdate: ((RegistrationForm) -> RegistrationForm) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text(stringResource(R.string.artist_details), color = PremiumGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)

        SearchableDropdownField(
            label = stringResource(R.string.category),
            value = form.category,
            options = categories,
            onSelect = { selected -> onUpdate { it.copy(category = selected) } }
        )

        SearchableDropdownField(
            label = stringResource(R.string.speciality),
            value = form.speciality,
            options = specialities,
            onSelect = { selected -> onUpdate { it.copy(speciality = selected) } }
        )

        LocationSelector(
            selectedState = form.state,
            selectedDistrict = form.district,
            states = states,
            districts = districts,
            onStateSelected = { selected -> onUpdate { it.copy(state = selected, district = "") } },
            onDistrictSelected = { selected -> onUpdate { it.copy(district = selected) } }
        )

        SearchableDropdownField(
            label = stringResource(R.string.experience_level),
            value = form.experience,
            options = experienceLevels,
            onSelect = { selected -> onUpdate { it.copy(experience = selected) } }
        )
        
        Column {
            Text(stringResource(R.string.about_you), color = PremiumGold, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = form.aboutMe,
                onValueChange = { if (it.length <= 500) onUpdate { f -> f.copy(aboutMe = it) } },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                placeholder = { Text("Tell us about your artistic journey...", color = SecondaryText) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PremiumWhite,
                    unfocusedTextColor = PremiumWhite,
                    focusedBorderColor = PremiumGold,
                    unfocusedBorderColor = BorderColor
                ),
                shape = RoundedCornerShape(16.dp)
            )
            Text(
                "${form.aboutMe.length}/500",
                color = if (form.aboutMe.length >= 450) Color.Red else SecondaryText,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun DocumentsStep(form: RegistrationForm, onUpdate: ((RegistrationForm) -> RegistrationForm) -> Unit) {
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onUpdate { it.copy(profilePhotoUri = uri) } }
    }
    val idPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onUpdate { it.copy(idProofUri = uri) } }
    }

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text(stringResource(R.string.documents), color = PremiumGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)

        DocumentUploadCard(
            title = stringResource(R.string.profile_photo),
            description = "High quality photo for your profile",
            imageUri = form.profilePhotoUri,
            remoteUrl = form.photoUrl,
            onClick = { photoPicker.launch("image/*") }
        )

        DocumentUploadCard(
            title = stringResource(R.string.identity_proof),
            description = "Aadhaar, PAN, or Passport",
            imageUri = form.idProofUri,
            remoteUrl = form.idProofUrl,
            onClick = { idPicker.launch("image/*") }
        )
    }
}

@Composable
fun DocumentUploadCard(
    title: String,
    description: String,
    imageUri: Uri?,
    remoteUrl: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = CardBackground,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (imageUri != null || remoteUrl.isNotEmpty()) PremiumGold else BorderColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppBackground),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(model = imageUri, contentDescription = null, contentScale = ContentScale.Crop)
                } else if (remoteUrl.isNotEmpty()) {
                    AsyncImage(model = remoteUrl, contentDescription = null, contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.CloudUpload, null, tint = PremiumGold, modifier = Modifier.size(32.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = PremiumWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(description, color = SecondaryText, fontSize = 12.sp)
            }
            
            if (imageUri != null || remoteUrl.isNotEmpty()) {
                Icon(Icons.Default.CheckCircle, null, tint = Color.Green, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun ReviewStep(form: RegistrationForm) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text(stringResource(R.string.review_submit), color = PremiumGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        
        Surface(
            color = CardBackground,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ReviewItem(stringResource(R.string.full_name), form.name)
                ReviewItem(stringResource(R.string.category), form.category)
                ReviewItem("Location", "${form.district}, ${form.state}")
                ReviewItem(stringResource(R.string.experience_level), form.experience)
            }
        }
        
        Text(
            "By submitting, you agree to our Terms and Conditions. Your application will be reviewed by our admin team within 24-48 hours.",
            color = SecondaryText,
            fontSize = 12.sp
        )
    }
}

@Composable
fun ReviewItem(label: String, value: String) {
    Column {
        Text(label, color = SecondaryText, fontSize = 12.sp)
        Text(value, color = PremiumWhite, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun RegistrationTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(label, color = PremiumGold, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = SecondaryText) },
            leadingIcon = { Icon(leadingIcon, null, tint = PremiumGold) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PremiumWhite,
                unfocusedTextColor = PremiumWhite,
                focusedBorderColor = PremiumGold,
                unfocusedBorderColor = BorderColor,
                focusedContainerColor = CardBackground.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )
    }
}

@Composable
fun SearchableDropdownField(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    
    Column {
        Text(label, color = PremiumGold, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
        Surface(
            onClick = { showDialog = true },
            color = CardBackground.copy(alpha = 0.3f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, if (value.isNotEmpty()) PremiumGold else BorderColor)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = value.ifEmpty { "Select $label" },
                    color = if (value.isEmpty()) SecondaryText else PremiumWhite
                )
                Icon(Icons.Default.ArrowDropDown, null, tint = PremiumGold)
            }
        }
    }

    if (showDialog) {
        SearchableListDialog(
            title = "Select $label",
            list = options,
            onDismiss = { showDialog = false },
            onSelect = {
                onSelect(it)
                showDialog = false
            }
        )
    }
}

@Composable
fun RegistrationBottomBar(
    currentStep: RegistrationStep,
    form: RegistrationForm,
    isLoading: Boolean,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val isValid = isStepValid(currentStep, form)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppBackground,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(24.dp).navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, PremiumGold),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PremiumGold)
            ) {
                Text(stringResource(R.string.back))
            }
            
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PremiumGold,
                    disabledContainerColor = PremiumGold.copy(alpha = 0.3f)
                ),
                enabled = !isLoading && isValid
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = AppBackground, strokeWidth = 2.dp)
                } else {
                    Text(
                        text = if (currentStep == RegistrationStep.REVIEW) stringResource(R.string.submit_application) else stringResource(R.string.next_step),
                        color = if (isValid) AppBackground else SecondaryText,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Reuse SearchableListDialog from LocationSelector or local implementation
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchableListDialog(
    title: String,
    list: List<String>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredList = remember(searchQuery, list) {
        if (searchQuery.isEmpty()) list
        else list.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.7f),
            color = AppBackground,
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, PremiumGold.copy(0.3f))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(title, color = PremiumGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search...", color = SecondaryText) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PremiumWhite,
                        unfocusedTextColor = PremiumWhite,
                        focusedBorderColor = PremiumGold,
                        unfocusedBorderColor = BorderColor
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredList) { item ->
                        Column {
                            Text(
                                text = item,
                                color = PremiumWhite,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(item) }
                                    .padding(vertical = 12.dp),
                                fontSize = 16.sp
                            )
                            HorizontalDivider(color = BorderColor.copy(0.3f))
                        }
                    }
                }
            }
        }
    }
}

fun isStepValid(step: RegistrationStep, form: RegistrationForm): Boolean {
    return when (step) {
        RegistrationStep.BASIC_INFO -> form.name.isNotBlank() && form.phone.length >= 10 && form.email.contains("@")
        RegistrationStep.ARTIST_DETAILS -> form.category.isNotBlank() && form.state.isNotBlank() && form.district.isNotBlank()
        RegistrationStep.DOCUMENTS -> (form.profilePhotoUri != null || form.photoUrl.isNotEmpty()) && (form.idProofUri != null || form.idProofUrl.isNotEmpty())
        RegistrationStep.REVIEW -> true
    }
}
