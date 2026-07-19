package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sangeetsetu.app.model.Event
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.viewmodel.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    eventId: String,
    onBack: () -> Unit,
    onBookClick: (String) -> Unit,
    viewModel: EventViewModel = hiltViewModel()
) {
    var event by remember { mutableStateOf<Event?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(eventId) {
        viewModel.getEventById(eventId) {
            event = it
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("इवेंट विवरण", color = PremiumWhite, fontWeight = FontWeight.Bold, fontFamily = NotoSansDevanagariFamily) },
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
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PremiumGold)
            }
        } else if (event == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("इवेंट नहीं मिला", color = PremiumWhite, fontFamily = NotoSansDevanagariFamily)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                AsyncImage(
                    model = event!!.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )

                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = event!!.title,
                        color = PremiumWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = NotoSansDevanagariFamily
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, null, tint = PremiumGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(event!!.date, color = PremiumWhite, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = PremiumGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(event!!.location, color = PremiumWhite, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("विवरण", color = PremiumGold, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = NotoSansDevanagariFamily)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        event!!.description,
                        color = PremiumGray,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        fontFamily = NotoSansDevanagariFamily
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { onBookClick(event!!.artistId) },
                            colors = ButtonDefaults.buttonColors(containerColor = PremiumGold),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("अभी बुक करें", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = NotoSansDevanagariFamily)
                        }
                    }
                }
            }
        }
    }
}
