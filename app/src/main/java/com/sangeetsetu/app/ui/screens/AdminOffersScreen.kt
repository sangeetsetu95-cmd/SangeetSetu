package com.sangeetsetu.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.sangeetsetu.app.model.Offer
import com.sangeetsetu.app.navigation.Screen
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOffersScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val offers by viewModel.offers.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedOffer by remember { mutableStateOf<Offer?>(null) }

    LaunchedEffect(Unit) { viewModel.fetchOffers() }

    if (showEditDialog) {
        OfferEditDialog(
            offer = selectedOffer,
            onDismiss = { 
                showEditDialog = false
                selectedOffer = null 
            },
            onSave = { offer ->
                viewModel.saveOffer(offer, 
                    onSuccess = {
                        Toast.makeText(context, "Offer saved", Toast.LENGTH_SHORT).show()
                        showEditDialog = false
                        selectedOffer = null
                    },
                    onError = {
                        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Offers", color = PremiumWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumGold)
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        selectedOffer = null
                        showEditDialog = true 
                    }) {
                        Icon(Icons.Rounded.Add, null, tint = PremiumGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PremiumNavyDark)
            )
        },
        bottomBar = {
            AdminBottomNav(currentRoute = Screen.AdminOffers.route, onNavigate = onNavigate)
        },
        containerColor = PremiumNavy
    ) { padding ->
        if (isLoading && offers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PremiumGold)
            }
        } else if (offers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.LocalOffer, null, tint = PremiumGray.copy(0.2f), modifier = Modifier.size(80.dp))
                    Text("No offers created", color = PremiumGray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(offers) { offer ->
                    OfferAdminCard(
                        offer = offer,
                        onEdit = { 
                            selectedOffer = offer
                            showEditDialog = true
                        },
                        onDelete = {
                            viewModel.deleteOffer(offer.id) {
                                Toast.makeText(context, "Offer Deleted", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OfferAdminCard(offer: Offer, onEdit: () -> Unit, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        color = PremiumNavyLight,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(offer.code, color = PremiumGold, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text(offer.description, color = PremiumWhite, fontSize = 13.sp)
                Text("${offer.discountPercentage}% Discount", color = SuccessColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, null, tint = Color.Red.copy(0.7f))
            }
        }
    }
}

@Composable
fun OfferEditDialog(offer: Offer?, onDismiss: () -> Unit, onSave: (Offer) -> Unit) {
    var code by remember { mutableStateOf(offer?.code ?: "") }
    var desc by remember { mutableStateOf(offer?.description ?: "") }
    var discount by remember { mutableStateOf(offer?.discountPercentage?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PremiumNavyDark,
        title = { Text(if (offer == null) "New Offer" else "Edit Offer", color = PremiumGold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = code, 
                    onValueChange = { code = it }, 
                    label = { Text("Promo Code") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = desc, 
                    onValueChange = { desc = it }, 
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = discount, 
                    onValueChange = { discount = it }, 
                    label = { Text("Discount %") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val newOffer = offer?.copy(
                        code = code, 
                        description = desc, 
                        discountPercentage = discount.toIntOrNull() ?: 0
                    ) ?: Offer(
                        code = code, 
                        description = desc, 
                        discountPercentage = discount.toIntOrNull() ?: 0
                    )
                    onSave(newOffer)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PremiumGold)
            ) { Text("Save", color = PremiumNavy) }
        },
        dismissButton = { 
            TextButton(onClick = onDismiss) { Text("Cancel", color = PremiumGray) } 
        }
    )
}
