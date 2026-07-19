package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sangeetsetu.app.model.AdminLog
import com.sangeetsetu.app.ui.theme.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLogsScreen(onBack: () -> Unit) {
    val db = remember { FirebaseFirestore.getInstance() }
    var logs by remember { mutableStateOf<List<AdminLog>>(emptyList()) }
    var isLoading by remember { mutableStateOf(value = true) }

    LaunchedEffect(Unit) {
        db.collection("admin_logs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener { result ->
                logs = result.toObjects(AdminLog::class.java)
                isLoading = false
            }
            .addOnFailureListener { isLoading = false }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity Logs", color = PremiumWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PremiumNavyDark),
            )
        },
        containerColor = PremiumNavy
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PremiumGold)
            }
        } else if (logs.isEmpty()) {
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No activity logs found", color = PremiumGray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(logs) { log ->
                    LogItem(log)
                }
            }
        }
    }
}

@Composable
fun LogItem(log: AdminLog) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val dateString = remember(log.timestamp) { sdf.format(Date(log.timestamp)) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = PremiumNavyLight,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(PremiumGold.copy(0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.History, null, tint = PremiumGold, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(log.action, color = PremiumWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                if (log.targetId.isNotEmpty()) {
                    Text("ID: ${log.targetId}", fontSize = 11.sp, color = PremiumGray)
                }
                Text(dateString, fontSize = 11.sp, color = PremiumGray.copy(0.7f))
            }
        }
    }
}
