package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sangeetsetu.app.model.Booking
import com.sangeetsetu.app.model.BookingStatus
import com.sangeetsetu.app.navigation.Screen
import com.sangeetsetu.app.ui.theme.BorderColor
import com.sangeetsetu.app.ui.theme.PremiumGold
import com.sangeetsetu.app.ui.theme.SecondaryText
import com.sangeetsetu.app.ui.theme.AppBackground
import com.sangeetsetu.app.ui.theme.CardBackground
import com.sangeetsetu.app.ui.theme.PremiumWhite
import com.sangeetsetu.app.util.ReportGenerator
import com.sangeetsetu.app.viewmodel.BookingViewModel

/**
 * Admin screen for managing all bookings in the system.
 * Allows administrators to filter bookings by status, update booking statuses,
 * and generate PDF reports of booking data.
 *
 * Following the removal of manual artist approval, this screen focuses on 
 * transactional management and operational oversight.
 *
 * @param onBack Callback to navigate to the previous screen.
 * @param onNavigate Callback to navigate to other screens via route.
 * @param viewModel ViewModel for managing booking state and actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBookingsScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: BookingViewModel = hiltViewModel()
) {
    val bookings by viewModel.bookings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("All") }
    val tabs = listOf("All", "Pending", "Accepted", "In_Progress", "Completed", "Cancelled")

    LaunchedEffect(error) {
        error?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchAllBookings()
    }

    val filteredBookings = remember(selectedTab, bookings) {
        if (selectedTab == "All") bookings
        else bookings.filter { it.status.name == selectedTab.uppercase() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking Management", color = PremiumWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumGold)
                    }
                },
                actions = {
                    IconButton(onClick = { ReportGenerator.generateBookingPDF(context, filteredBookings) }) {
                        Icon(Icons.Default.PictureAsPdf, null, tint = PremiumGold)
                    }
                    IconButton(onClick = { }) { Icon(Icons.Default.Search, null, tint = PremiumWhite) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
            )
        },
        bottomBar = {
            AdminBottomNav(currentRoute = Screen.AdminBookings.route, onNavigate = onNavigate)
        },
        containerColor = AppBackground
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyRow(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tabs) { tab ->
                    FilterChip(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        label = { Text(tab, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PremiumGold,
                            selectedLabelColor = AppBackground,
                            containerColor = CardBackground,
                            labelColor = SecondaryText
                        ),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = selectedTab == tab, borderColor = BorderColor)
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PremiumGold)
                }
            } else if (filteredBookings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No bookings found in $selectedTab", color = SecondaryText)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredBookings, key = { it.id }) { booking ->
                        AdminBookingItem(
                            booking = booking,
                            onStatusChange = { newStatus ->
                                viewModel.updateBookingStatus(booking.id, newStatus) {
                                    android.widget.Toast.makeText(context, "Status Updated to $newStatus", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual booking item displayed in the admin booking list.
 * Displays user and artist information, event details, and current status.
 *
 * @param booking The booking data model.
 * @param onStatusChange Callback triggered when the booking status is updated by the admin.
 */
@Composable
fun AdminBookingItem(booking: Booking, onStatusChange: (BookingStatus) -> Unit) {
    var showStatusMenu by remember { mutableStateOf(false) }

    Surface(
        color = CardBackground,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(PremiumGold.copy(0.1f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = PremiumGold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(booking.userName, color = PremiumWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Booking with ${booking.artistName}", color = PremiumGold, fontSize = 12.sp)
                }
                Box {
                    StatusBadgeLarge(booking.status, onClick = { showStatusMenu = true })
                    DropdownMenu(
                        expanded = showStatusMenu,
                        onDismissRequest = { showStatusMenu = false },
                        modifier = Modifier.background(CardBackground)
                    ) {
                        BookingStatus.values().forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.name, color = PremiumWhite) },
                                onClick = {
                                    onStatusChange(status)
                                    showStatusMenu = false
                                }
                            )
                        }
                    }
                }
            }
            
            HorizontalDivider(color = Color.White.copy(0.05f), modifier = Modifier.padding(vertical = 12.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, tint = SecondaryText, modifier = Modifier.size(14.dp))
                    Text(" ${booking.eventDate}", color = SecondaryText, fontSize = 12.sp)
                }
            }
            
            Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = SecondaryText, modifier = Modifier.size(14.dp))
                Text(" ${booking.location}", color = SecondaryText, fontSize = 12.sp)
            }
        }
    }
}

/**
 * Large status badge with a clickable surface to change the status.
 * Color-coded based on the [BookingStatus].
 *
 * @param status The current status of the booking.
 * @param onClick Callback to open the status selection menu.
 */
@Composable
fun StatusBadgeLarge(status: BookingStatus, onClick: () -> Unit) {
    val color = when(status) {
        BookingStatus.PENDING -> Color.Yellow
        BookingStatus.ACCEPTED -> Color.Green
        BookingStatus.IN_PROGRESS -> Color.Cyan
        BookingStatus.COMPLETED -> Color.Blue
        BookingStatus.CANCELLED -> Color.Red
    }
    Surface(
        onClick = onClick,
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            status.name,
            color = color,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
