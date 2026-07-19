package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressesScreen(
    onBack: () -> Unit,
    viewModel: UserViewModel = hiltViewModel()
) {
    val user by viewModel.currentUserData.collectAsState()
    val addresses = user?.addresses ?: emptyList()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var newAddress by remember { mutableStateOf("") }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = CardBackground,
            title = { Text("Add New Address", color = PremiumGold, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newAddress,
                    onValueChange = { newAddress = it },
                    label = { Text("Address Details") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = PremiumGold
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newAddress.isNotBlank()) {
                            viewModel.addAddress(newAddress)
                            newAddress = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumGold)
                ) {
                    Text("Add", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = SecondaryText)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Addresses", color = PremiumWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }, 
                containerColor = PremiumGold,
                shape = CircleShape,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Icon(Icons.Default.Add, null, tint = Color.Black)
            }
        },
        containerColor = AppBackground
    ) { padding ->
        if (addresses.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.LocationOn, null, tint = SecondaryText.copy(alpha = 0.2f), modifier = Modifier.size(80.dp))
                    Text("No addresses saved", color = SecondaryText)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(addresses) { address ->
                    AddressCard(address, onDelete = { viewModel.removeAddress(address) })
                }
            }
        }
    }
}

@Composable
fun AddressCard(address: String, onDelete: () -> Unit) {
    Surface(
        color = CardBackground,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.LocationOn, null, tint = PremiumGold)
            Spacer(modifier = Modifier.width(16.dp))
            Text(address, color = Color.White, fontSize = 14.sp, fontFamily = PoppinsFamily, modifier = Modifier.weight(1f))
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.6f))
            }
        }
    }
}
