package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.viewmodel.ConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAppSettingsScreen(
    onBack: () -> Unit,
    viewModel: com.sangeetsetu.app.viewmodel.AdminViewModel = hiltViewModel()
) {
    val settings by viewModel.systemSettings.collectAsStateWithLifecycle()
    val appInfoViewModel: com.sangeetsetu.app.viewmodel.ConfigViewModel = hiltViewModel()
    val appInfo by appInfoViewModel.appInfo.collectAsStateWithLifecycle()
    val isLoadingAdmin by viewModel.isLoading.collectAsStateWithLifecycle()
    val isLoadingConfig by appInfoViewModel.isLoading.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val isLoading = isLoadingAdmin || isLoadingConfig

    var upiId by remember(settings) { mutableStateOf(settings.upiId) }
    var latestVersion by remember(settings) { mutableStateOf(settings.latestVersionCode.toString()) }
    var minVersion by remember(settings) { mutableStateOf(settings.minimumVersionCode.toString()) }
    var updateUrl by remember(settings) { mutableStateOf(settings.updateUrl) }
    var isMandatory by remember(settings) { mutableStateOf(settings.isUpdateMandatory) }
    var maintenanceMode by remember(settings) { mutableStateOf(settings.maintenanceMode) }
    var quickCategories by remember(settings) { mutableStateOf(settings.features["quick_categories"] ?: false) }
    var chatPrivacyFilter by remember(settings) { mutableStateOf(settings.features["chat_privacy_filter"] ?: true) }

    var referralOffer by remember(appInfo) { mutableStateOf(appInfo.referralOffer) }
    var terms by remember(appInfo) { mutableStateOf(appInfo.termsAndConditions) }
    var privacy by remember(appInfo) { mutableStateOf(appInfo.privacyPolicy) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Global Config", color = PremiumWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground),
                actions = {
                    Button(
                        onClick = {
                            viewModel.saveSystemSettings(settings.copy(
                                upiId = upiId,
                                latestVersionCode = latestVersion.toIntOrNull() ?: 1,
                                minimumVersionCode = minVersion.toIntOrNull() ?: 1,
                                updateUrl = updateUrl,
                                isUpdateMandatory = isMandatory,
                                maintenanceMode = maintenanceMode,
                                features = settings.features.toMutableMap().apply {
                                    this["quick_categories"] = quickCategories
                                    this["chat_privacy_filter"] = chatPrivacyFilter
                                }
                            ), onSuccess = {
                                appInfoViewModel.updateAppInfo(appInfo.copy(
                                    referralOffer = referralOffer,
                                    termsAndConditions = terms,
                                    privacyPolicy = privacy
                                ), onSuccess = {
                                    android.widget.Toast.makeText(context, "All settings synchronized successfully", android.widget.Toast.LENGTH_SHORT).show()
                                }, onError = {
                                    android.widget.Toast.makeText(context, "Error updating app info: $it", android.widget.Toast.LENGTH_LONG).show()
                                })
                            }, onError = {
                                android.widget.Toast.makeText(context, "Error updating system settings: $it", android.widget.Toast.LENGTH_LONG).show()
                            })
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumGold),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = AppBackground, modifier = Modifier.size(20.dp))
                        } else {
                            Text("Sync All", color = AppBackground)
                        }
                    }
                }
            )
        },
        containerColor = AppBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("App Maintenance", style = MaterialTheme.typography.titleLarge, color = PremiumGold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Maintenance Mode", color = PremiumWhite, modifier = Modifier.weight(1f))
                Switch(checked = maintenanceMode, onCheckedChange = { maintenanceMode = it }, colors = SwitchDefaults.colors(checkedThumbColor = PremiumGold))
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Show Quick Category Chips", color = PremiumWhite, modifier = Modifier.weight(1f))
                Switch(checked = quickCategories, onCheckedChange = { quickCategories = it }, colors = SwitchDefaults.colors(checkedThumbColor = PremiumGold))
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Enable Chat Privacy Filter", color = PremiumWhite, modifier = Modifier.weight(1f))
                Switch(checked = chatPrivacyFilter, onCheckedChange = { chatPrivacyFilter = it }, colors = SwitchDefaults.colors(checkedThumbColor = PremiumGold))
            }

            Text("Dynamic APK Update", style = MaterialTheme.typography.titleLarge, color = PremiumGold)
            OutlinedTextField(value = latestVersion, onValueChange = { latestVersion = it }, label = { Text("Latest Version Code") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = minVersion, onValueChange = { minVersion = it }, label = { Text("Min Required Version Code") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = updateUrl, onValueChange = { updateUrl = it }, label = { Text("Update Download URL") }, modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Force Mandatory Update", color = PremiumWhite, modifier = Modifier.weight(1f))
                Switch(checked = isMandatory, onCheckedChange = { isMandatory = it }, colors = SwitchDefaults.colors(checkedThumbColor = PremiumGold))
            }

            Text("Payment Settings", style = MaterialTheme.typography.titleLarge, color = PremiumGold)
            OutlinedTextField(value = upiId, onValueChange = { upiId = it }, label = { Text("Admin UPI ID (for subscriptions)") }, modifier = Modifier.fillMaxWidth())
            
            Text("General Settings", style = MaterialTheme.typography.titleLarge, color = PremiumGold)
            OutlinedTextField(value = referralOffer, onValueChange = { referralOffer = it }, label = { Text("Referral Reward Text") }, modifier = Modifier.fillMaxWidth())

            Text("Legal Documents", style = MaterialTheme.typography.titleLarge, color = PremiumGold)
            OutlinedTextField(value = terms, onValueChange = { terms = it }, label = { Text("Terms & Conditions") }, modifier = Modifier.fillMaxWidth(), minLines = 5)
            OutlinedTextField(value = privacy, onValueChange = { privacy = it }, label = { Text("Privacy Policy") }, modifier = Modifier.fillMaxWidth(), minLines = 5)
        }
    }
}
