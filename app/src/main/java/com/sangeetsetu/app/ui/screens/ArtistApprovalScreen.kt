package com.sangeetsetu.app.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.sangeetsetu.app.model.User
import com.sangeetsetu.app.ui.theme.BorderColor
import com.sangeetsetu.app.ui.theme.PremiumGold
import com.sangeetsetu.app.ui.theme.SecondaryText
import com.sangeetsetu.app.ui.theme.AppBackground
import com.sangeetsetu.app.ui.theme.CardBackground
import com.sangeetsetu.app.ui.theme.PremiumWhite
import com.google.firebase.firestore.FirebaseFirestore

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.sangeetsetu.app.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistApprovalScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val artists by viewModel.pendingArtists.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Artist Approvals (Verified)", color = PremiumWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
            )
        },
        containerColor = AppBackground
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (isLoading && artists.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PremiumGold)
            } else if (artists.isEmpty()) {
                Text("No pending approvals on server", modifier = Modifier.align(Alignment.Center), color = SecondaryText)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(artists, key = { it.uid }) { artist ->
                        ArtistApprovalItem(
                            artist = artist,
                            onApprove = {
                                viewModel.approveArtist(artist.uid) {
                                    Toast.makeText(context, "Artist Approved & Verified on Server", Toast.LENGTH_SHORT).show()
                                    viewModel.fetchPendingArtists()
                                }
                            },
                            onReject = {
                                viewModel.rejectArtist(artist.uid) {
                                    Toast.makeText(context, "Artist Rejected on Server", Toast.LENGTH_SHORT).show()
                                    viewModel.fetchPendingArtists()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArtistApprovalItem(artist: User, onApprove: () -> Unit, onReject: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = CardBackground,
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(50.dp).background(PremiumGold.copy(0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(artist.name.take(1), color = PremiumGold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(artist.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PremiumWhite)
                    Text(artist.category, color = PremiumGold, fontSize = 14.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text("Location: ${artist.city}, ${artist.state}", fontSize = 12.sp, color = SecondaryText)
            Text("Experience: ${artist.experience} Years", fontSize = 12.sp, color = SecondaryText)
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                ) {
                    Text("Reject", color = Color.Red)
                }
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Approve", color = AppBackground, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
