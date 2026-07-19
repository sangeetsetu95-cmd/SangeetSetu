package com.sangeetsetu.app.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import com.sangeetsetu.app.R
import com.sangeetsetu.app.model.Booking
import com.sangeetsetu.app.ui.components.PremiumButton
import com.sangeetsetu.app.ui.components.PremiumHeader
import com.sangeetsetu.app.ui.components.PremiumTextField
import com.sangeetsetu.app.ui.components.UpiPaymentSheet
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.viewmodel.BookingViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    artistId: String, 
    onBack: () -> Unit,
    viewModel: BookingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    
    val defaultArtistName = stringResource(R.string.artist)
    var artistName by remember { mutableStateOf(defaultArtistName) }
    var artistPhotoUrl by remember { mutableStateOf("") }
    
    var eventDate by remember { mutableStateOf("") }
    var eventTime by remember { mutableStateOf("") }
    var eventType by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var guestCount by remember { mutableStateOf("100") }
    var note by remember { mutableStateOf("") }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(artistId) {
        Log.d("BookingFlow", "BookingScreen received artistId: $artistId")
        if (artistId.isNotEmpty() && artistId != "all") {
            // Try loading by UID first
            db.collection("users").document(artistId).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        Log.d("BookingFlow", "Artist found by UID: ${doc.getString("name")}")
                        artistName = doc.getString("name") ?: defaultArtistName
                        artistPhotoUrl = doc.getString("photoUrl") ?: ""
                    } else {
                        // Try searching by artistId field (SSA...)
                        Log.d("BookingFlow", "UID not found, searching by SSA ID field...")
                        db.collection("users").whereEqualTo("artistId", artistId).limit(1).get()
                            .addOnSuccessListener { query ->
                                if (!query.isEmpty) {
                                    val result = query.documents[0]
                                    Log.d("BookingFlow", "Artist found by SSA ID: ${result.getString("name")}")
                                    artistName = result.getString("name") ?: defaultArtistName
                                    artistPhotoUrl = result.getString("photoUrl") ?: ""
                                } else {
                                    Log.e("BookingFlow", "Artist not found by either UID or SSA ID")
                                    Toast.makeText(context, context.getString(R.string.artist_not_found), Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("BookingFlow", "Firestore fetch failed", e)
                }
        }
    }

    if (artistId.isEmpty() || artistId == "all") {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.select_artist_to_book), color = PremiumWhite)
                Spacer(modifier = Modifier.height(16.dp))
                PremiumButton(text = stringResource(R.string.find_artists), onClick = { onBack() })
            }
        }
        return
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val date = Date(it)
                        val cal = Calendar.getInstance()
                        cal.time = date
                        eventDate = "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.YEAR)}"
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.confirm), color = PremiumGold) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState()
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    eventTime = "${timePickerState.hour}:${timePickerState.minute}"
                    showTimePicker = false
                }) { Text(stringResource(R.string.confirm), color = PremiumGold) }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { onBack() },
            containerColor = CardBackground,
            title = { Text(stringResource(R.string.booking_sent_title), color = PremiumGold, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.booking_sent_msg, artistName, eventDate), color = PremiumWhite) },
            confirmButton = {
                PremiumButton(text = stringResource(R.string.go_to_my_bookings), onClick = { onBack() })
            }
        )
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            PremiumHeader(
                title = stringResource(R.string.booking_details_title),
                onBackClick = onBack
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PremiumGold)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
            // 1. Artist Info Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CardBackground,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(0.05f))
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = artistPhotoUrl.ifEmpty { "https://cdn-icons-png.flaticon.com/512/149/149071.png" },
                        contentDescription = null,
                        modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.05f)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(artistName, color = PremiumWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.fill_details_to_book), color = PremiumGold, fontSize = 14.sp)
                    }
                }
            }

            // 2. Booking Details Form
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CardBackground,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(0.05f))
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Text(stringResource(R.string.event_details_label), color = PremiumWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    
                    BookingFormField(stringResource(R.string.select_date), eventDate, Icons.Default.CalendarMonth) { showDatePicker = true }
                    BookingFormField(stringResource(R.string.select_time), eventTime, Icons.Default.AccessTime) { showTimePicker = true }
                    
                    PremiumTextField(
                        value = eventType,
                        onValueChange = { eventType = it },
                        label = stringResource(R.string.event_type_label),
                        leadingIcon = Icons.Default.Event
                    )

                    PremiumTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = stringResource(R.string.city_location_label),
                        leadingIcon = Icons.Default.LocationOn
                    )

                    PremiumTextField(
                        value = guestCount,
                        onValueChange = { guestCount = it },
                        label = stringResource(R.string.guest_count_label),
                        leadingIcon = Icons.Default.People
                    )

                    PremiumTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = stringResource(R.string.special_requirements_label),
                        leadingIcon = Icons.Default.Notes,
                        modifier = Modifier.height(100.dp)
                    )
                }
            }

            // 3. Confirm Button
            PremiumButton(
                text = stringResource(R.string.confirm_booking_request),
                onClick = {
                    if (eventDate.isEmpty() || eventTime.isEmpty() || city.isEmpty()) {
                        Toast.makeText(context, context.getString(R.string.fill_all_required_fields), Toast.LENGTH_SHORT).show()
                    } else {
                        val booking = Booking(
                            id = UUID.randomUUID().toString(),
                            artistId = artistId,
                            artistName = artistName,
                            eventDate = eventDate,
                            eventTime = eventTime,
                            location = city,
                            status = com.sangeetsetu.app.model.BookingStatus.PENDING,
                            createdAt = System.currentTimeMillis(),
                            note = note
                        )
                        viewModel.createBooking(
                            booking = booking,
                            onSuccess = {
                                showSuccessDialog = true
                            },
                            onError = {
                                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
}

@Composable
fun BookingFormField(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(0.05f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = PremiumGold, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(if (value.isEmpty()) label else value, color = if (value.isEmpty()) SecondaryText else PremiumWhite)
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = { TextButton(onClick = onDismissRequest) { Text("Cancel", color = SecondaryText) } },
        containerColor = CardBackground,
        text = { content() }
    )
}
