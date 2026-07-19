package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sangeetsetu.app.navigation.Screen
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.viewmodel.AdminViewModel
import com.sangeetsetu.app.viewmodel.BookingViewModel

/**
 * Administrative screen for managing pending system requests.
 * 
 * Following the removal of manual artist approval, this screen now focuses
 * primarily on managing booking requests and other operational approvals.
 *
 * @param onBack Callback to return to the previous screen.
 * @param onNavigate Callback to navigate to other admin sections.
 * @param adminViewModel ViewModel for general admin operations (logging, etc).
 * @param bookingViewModel ViewModel for managing booking states.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRequestsScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    adminViewModel: AdminViewModel = hiltViewModel(),
    bookingViewModel: BookingViewModel = hiltViewModel()
) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        bookingViewModel.fetchAllBookings()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Requests", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PremiumNavyDark)
            )
        },
        bottomBar = {
            AdminBottomNav(currentRoute = Screen.AdminRequests.route, onNavigate = onNavigate)
        },
        containerColor = PremiumNavy
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                "Booking Requests",
                modifier = Modifier.padding(16.dp),
                color = PremiumGold,
                fontWeight = FontWeight.Bold
            )
            BookingRequestsContent(bookingViewModel)
        }
    }
}

@Composable
fun BookingRequestsContent(viewModel: BookingViewModel) {
    val bookings by viewModel.bookings.collectAsState()
    val pendingBookings = remember(bookings) { bookings.filter { it.status == com.sangeetsetu.app.model.BookingStatus.PENDING } }

    if (pendingBookings.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("No pending booking requests", color = PremiumGray)
        }
    } else {
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(pendingBookings, key = { it.id }) { booking ->
                AdminBookingItem(
                    booking = booking,
                    onStatusChange = { newStatus ->
                        viewModel.updateBookingStatus(booking.id, newStatus)
                    }
                )
            }
        }
    }
}
