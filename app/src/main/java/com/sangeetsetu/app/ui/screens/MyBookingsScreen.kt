package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetsetu.app.model.Booking
import com.sangeetsetu.app.model.BookingStatus
import com.sangeetsetu.app.ui.components.PremiumHeader
import com.sangeetsetu.app.ui.components.PremiumStatusBadge
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.viewmodel.BookingViewModel

@Composable
fun MyBookingsScreen(
    onBack: () -> Unit,
    onHome: () -> Unit,
    onCategories: () -> Unit,
    onChat: () -> Unit,
    onProfile: () -> Unit,
    onArtistClick: (String) -> Unit,
    viewModel: BookingViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pending", "Confirmed", "Completed", "Cancelled")
    val bookings by viewModel.bookings.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    var selectedBookingForDetails by remember { mutableStateOf<Booking?>(null) }

    val filteredBookings = when(selectedTab) {
        0 -> bookings.filter { it.status == BookingStatus.PENDING }
        1 -> bookings.filter { it.status == BookingStatus.ACCEPTED || it.status == BookingStatus.IN_PROGRESS }
        2 -> bookings.filter { it.status == BookingStatus.COMPLETED }
        else -> bookings.filter { it.status == BookingStatus.CANCELLED }
    }

    if (selectedBookingForDetails != null) {
        BookingDetailsDialog(
            booking = selectedBookingForDetails!!,
            onDismiss = { selectedBookingForDetails = null },
            onArtistClick = { 
                selectedBookingForDetails = null
                onArtistClick(it)
            }
        )
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            PremiumHeader(
                title = "My Bookings",
                onBackClick = onBack,
                actions = {
                    IconButton(onClick = { viewModel.refreshBookings() }) {
                        Icon(Icons.Default.Refresh, "Refresh", tint = PremiumGold)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            SangeetSetuDesign.PremiumBackgroundDecoration()

            Column(modifier = Modifier.fillMaxSize()) {
                // Custom Premium Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardBackground)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .padding(4.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) PremiumGold else Color.Transparent)
                                .clickable { selectedTab = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) Color.Black else SecondaryText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                if (isLoading && bookings.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PremiumGold)
                    }
                } else if (filteredBookings.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CalendarMonth, null, tint = PremiumGold.copy(alpha = 0.1f), modifier = Modifier.size(80.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No ${tabs[selectedTab]} bookings found", color = SecondaryText, fontSize = 16.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        items(filteredBookings, key = { it.id }) { booking ->
                            PremiumBookingCard(booking) {
                                selectedBookingForDetails = booking
                            }
                        }
                        item { Spacer(modifier = Modifier.height(100.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingDetailsDialog(
    booking: Booking,
    onDismiss: () -> Unit,
    onArtistClick: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Booking Details", color = PremiumGold, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                DetailItem("Artist", booking.artistName)
                DetailItem("Event Type", booking.eventType)
                DetailItem("Date", booking.eventDate)
                DetailItem("Time", booking.eventTime)
                DetailItem("Location", booking.location)
                DetailItem("Status", booking.status.name)
                if (booking.note.isNotEmpty()) {
                    DetailItem("Note", booking.note)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onArtistClick(booking.artistId) },
                colors = ButtonDefaults.buttonColors(containerColor = PremiumGold, contentColor = Color.Black)
            ) {
                Text("View Artist Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = PremiumWhite)
            }
        },
        containerColor = Color(0xFF162D4D),
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = SecondaryText, fontSize = 14.sp)
        Text(value, color = PremiumWhite, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
fun PremiumBookingCard(booking: Booking, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = CardBackground,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PremiumGold.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = PremiumGold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(booking.artistName, color = PremiumWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(booking.eventType.ifEmpty { "Musical Performance" }, color = SecondaryText, fontSize = 12.sp)
                }
                PremiumStatusBadge(booking.status)
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = Color.White.copy(alpha = 0.05f)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, tint = PremiumGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(booking.eventDate, color = PremiumWhite, fontSize = 13.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = SecondaryText, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(booking.location, color = SecondaryText, fontSize = 12.sp, maxLines = 1)
            }
        }
    }
}
