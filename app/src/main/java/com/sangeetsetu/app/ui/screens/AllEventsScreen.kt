package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
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
import com.sangeetsetu.app.ui.components.LiveEventCard
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.viewmodel.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllEventsScreen(
    onBack: () -> Unit,
    onEventClick: (String) -> Unit,
    onBookClick: (String) -> Unit,
    viewModel: EventViewModel = hiltViewModel()
) {
    val events by viewModel.events.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("सभी लाइव इवेंट्स", color = PremiumWhite, fontWeight = FontWeight.Bold, fontFamily = NotoSansDevanagariFamily) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PremiumNavyDark)
            )
        },
        containerColor = PremiumNavy
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PremiumGold)
            } else if (error != null) {
                Text(error!!, color = Color.Red, modifier = Modifier.align(Alignment.Center))
            } else if (events.isEmpty()) {
                Text("कोई इवेंट उपलब्ध नहीं है", color = PremiumGray, modifier = Modifier.align(Alignment.Center), fontFamily = NotoSansDevanagariFamily)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(events) { event ->
                        LiveEventCard(
                            event = event,
                            onEventClick = { onEventClick(event.id) },
                            onBookClick = { onBookClick(event.artistId) }
                        )
                    }
                }
            }
        }
    }
}
