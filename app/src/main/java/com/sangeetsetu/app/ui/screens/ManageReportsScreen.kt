package com.sangeetsetu.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sangeetsetu.app.ui.components.PremiumToolbar
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageReportsScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val context = LocalContext.current
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            PremiumToolbar(
                title = "User Reports",
                onBack = onBack
            )
        },
        containerColor = AppBackground
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading && reports.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PremiumGold)
                }
            } else if (reports.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No active reports found", 
                        color = PremiumGray, 
                        fontFamily = PoppinsFamily,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "Verified Community Reports",
                            style = MaterialTheme.typography.titleMedium,
                            color = PremiumGold,
                            fontFamily = PoppinsFamily,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                        )
                    }
                    
                    items(reports, key = { it["id"] as? String ?: it.hashCode() }) { report ->
                        ReportItem(
                            report = report,
                            onDelete = {
                                val id = report["id"] as? String ?: ""
                                if (id.isNotEmpty()) {
                                    viewModel.deleteReport(id) {
                                        Toast.makeText(context, "Report Removed", Toast.LENGTH_SHORT).show()
                                    }
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
fun ReportItem(report: Map<String, Any>, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = CardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color.Red.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Report, null, tint = Color.Red, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Reported by: ${report["userName"] ?: "Anonymous"}", 
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = PoppinsFamily,
                        fontSize = 16.sp
                    )
                    Text(
                        "ID: ${report["id"]?.toString()?.take(8) ?: "N/A"}", 
                        color = PremiumGray, 
                        fontSize = 10.sp,
                        fontFamily = PoppinsFamily
                    )
                }
                IconButton(onClick = onDelete) { 
                    Icon(Icons.Default.Delete, null, tint = PremiumGray.copy(alpha = 0.5f)) 
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Reason for Report", 
                fontSize = 12.sp,
                color = PremiumGold,
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Medium
            )
            Text(
                report["reason"]?.toString() ?: "No specific reason provided.", 
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f),
                fontFamily = PoppinsFamily,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                "Against User UID: ${report["targetId"] ?: "Unknown"}", 
                fontSize = 11.sp,
                color = PremiumGray,
                fontFamily = PoppinsFamily
            )
        }
    }
}
