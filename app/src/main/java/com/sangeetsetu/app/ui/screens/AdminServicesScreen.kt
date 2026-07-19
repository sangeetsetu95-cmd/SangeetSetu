package com.sangeetsetu.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DesignServices
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetsetu.app.model.Service
import com.sangeetsetu.app.navigation.Screen
import com.sangeetsetu.app.ui.theme.CardBorder
import com.sangeetsetu.app.ui.theme.PremiumGold
import com.sangeetsetu.app.ui.theme.PremiumGray
import com.sangeetsetu.app.ui.theme.PremiumNavy
import com.sangeetsetu.app.ui.theme.PremiumNavyDark
import com.sangeetsetu.app.ui.theme.PremiumNavyLight
import com.sangeetsetu.app.ui.theme.PremiumWhite
import com.sangeetsetu.app.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminServicesScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val services by viewModel.services.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedService by remember { mutableStateOf<Service?>(null) }

    LaunchedEffect(Unit) { viewModel.fetchServices() }

    if (showAddDialog || selectedService != null) {
        ServiceEditDialog(
            service = selectedService,
            onDismiss = { showAddDialog = false; selectedService = null },
            onSave = { service ->
                viewModel.saveService(service) {
                    Toast.makeText(context, "Service synchronized", Toast.LENGTH_SHORT).show()
                    showAddDialog = false
                    selectedService = null
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Services", color = PremiumWhite, fontWeight = FontWeight.Bold) },
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
            AdminBottomNav(currentRoute = Screen.AdminServices.route, onNavigate = onNavigate)
        },
        containerColor = PremiumNavy
    ) { padding ->
        if (isLoading && services.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PremiumGold)
            }
        } else if (services.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.DesignServices, null, tint = PremiumGray.copy(0.2f), modifier = Modifier.size(80.dp))
                    Text("No services listed", color = PremiumGray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(services) { service ->
                    ServiceAdminCard(
                        service = service,
                        onEdit = { selectedService = service },
                        onDelete = {
                            viewModel.deleteService(service.id) {
                                Toast.makeText(context, "Service Deleted", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ServiceAdminCard(service: Service, onEdit: () -> Unit, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        color = PremiumNavyLight,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(service.name, color = PremiumGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(service.description, color = PremiumWhite, fontSize = 12.sp, maxLines = 1)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, null, tint = Color.Red.copy(0.7f))
            }
        }
    }
}

@Composable
fun ServiceEditDialog(service: Service?, onDismiss: () -> Unit, onSave: (Service) -> Unit) {
    var name by remember { mutableStateOf(service?.name ?: "") }
    var desc by remember { mutableStateOf(service?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PremiumNavyDark,
        title = { Text(if (service == null) "New Service" else "Edit Service", color = PremiumGold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Service Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { 
                onSave(service?.copy(name = name, description = desc) 
                    ?: Service(name = name, description = desc))
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
