package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sangeetsetu.app.model.Booking
import com.sangeetsetu.app.model.BookingStatus
import com.sangeetsetu.app.ui.theme.BorderColor
import com.sangeetsetu.app.ui.theme.PremiumGold
import com.sangeetsetu.app.ui.theme.SecondaryText
import com.sangeetsetu.app.ui.theme.AppBackground
import com.sangeetsetu.app.ui.theme.CardBackground
import com.sangeetsetu.app.ui.theme.PremiumWhite
import com.sangeetsetu.app.viewmodel.ArtistDashboardViewModel

/**
 * The main entry point for artists to manage their profile and bookings.
 * 
 * Following the removal of manual administrative approval, artists land here 
 * immediately after completing their professional registration.
 *
 * This screen provides:
 * 1. Overview of artist performance statistics (Rating, Bookings count, Verification status).
 * 2. List of recent booking requests with options to Accept or Reject them.
 * 3. Navigation for logging out and managing the artist session.
 *
 * @param onLogout Callback triggered when the artist signs out.
 * @param viewModel ViewModel providing the dashboard's state and business logic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDashboardScreen(
    onLogout: () -> Unit,
    viewModel: ArtistDashboardViewModel = hiltViewModel()
) {
    val profile by viewModel.artistProfile.collectAsState()
    val bookings by viewModel.artistBookings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Artist Dashboard", color = PremiumWhite) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, null, tint = PremiumWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
            )
        },
        containerColor = AppBackground
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PremiumGold)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                item {
                    ArtistStatsSection(profile, bookings.size)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Recent Bookings",
                        color = PremiumWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (bookings.isEmpty()) {
                    item {
                        Text("No bookings yet", color = SecondaryText, modifier = Modifier.padding(16.dp))
                    }
                } else {
                    items(bookings) { booking ->
                        ArtistBookingCard(booking) { newStatus ->
                            viewModel.updateBookingStatus(booking.id, newStatus)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArtistStatsSection(profile: com.sangeetsetu.app.model.User?, totalBookings: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, tint = PremiumGold, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(profile?.name ?: "Artist", color = PremiumWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(profile?.category ?: "Category", color = PremiumGold, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem("Rating", profile?.rating.toString())
                StatItem("Bookings", totalBookings.toString())
                StatItem("Verified", if (profile?.verificationStatus == "VERIFIED") "Yes" else "No")
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = SecondaryText, fontSize = 12.sp)
        Text(value, color = PremiumWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ArtistBookingCard(booking: Booking, onStatusChange: (BookingStatus) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(booking.userName, color = PremiumWhite, fontWeight = FontWeight.Bold)
                StatusBadge(booking.status)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("${booking.eventDate} | ${booking.eventTime}", color = SecondaryText, fontSize = 14.sp)
            Text(booking.location, color = SecondaryText, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            
            if (booking.status == BookingStatus.PENDING) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onStatusChange(BookingStatus.ACCEPTED) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green.copy(alpha = 0.6f))
                    ) {
                        Text("Accept", color = Color.White)
                    }
                    Button(
                        onClick = { onStatusChange(BookingStatus.CANCELLED) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.6f))
                    ) {
                        Text("Reject", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: BookingStatus) {
    val color = when(status) {
        BookingStatus.PENDING -> Color.Yellow
        BookingStatus.ACCEPTED -> Color.Green
        BookingStatus.CANCELLED -> Color.Red
        BookingStatus.COMPLETED -> Color.Cyan
        else -> Color.Gray
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            status.name,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
