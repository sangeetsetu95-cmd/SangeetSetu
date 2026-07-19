package com.sangeetsetu.app.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sangeetsetu.app.repository.StorageRepository
import com.sangeetsetu.app.ui.components.PremiumToolbar
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.viewmodel.AdminViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageContentScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val contentList by viewModel.appContent.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    var isUploading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selectedUri ->
            scope.launch {
                try {
                    isUploading = true
                    val fileName = "content_${System.currentTimeMillis()}"
                    
                    StorageRepository.uploadImage(selectedUri, "app_content", fileName)
                        .onSuccess { resource ->
                            val data = mapOf(
                                "url" to resource.url,
                                "cloudinaryPublicId" to resource.publicId,
                                "type" to if (selectedUri.toString().contains("video")) "video" else "audio",
                                "name" to "Media ${System.currentTimeMillis()}",
                                "timestamp" to System.currentTimeMillis()
                            )
                            viewModel.addAppContent(data) {
                                isUploading = false
                                Toast.makeText(context, "Media Uploaded Successfully!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .onFailure { e ->
                            isUploading = false
                            Toast.makeText(context, "Upload Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } catch (e: Exception) {
                    isUploading = false
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            PremiumToolbar(
                title = "Content Management",
                onBack = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { launcher.launch("*/*") }, 
                containerColor = PremiumGold,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.navigationBarsPadding()
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        color = Color.Black, 
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.CloudUpload, null, tint = Color.Black)
                }
            }
        },
        containerColor = AppBackground
    ) { paddingValues ->
        if (isLoading && contentList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PremiumGold)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "Uploaded Media",
                        style = MaterialTheme.typography.titleLarge,
                        color = PremiumGold,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PoppinsFamily,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                items(contentList) { item ->
                    ContentItem(
                        item = item,
                        onDelete = {
                            viewModel.deleteAppContent(item["id"] as String) {
                                Toast.makeText(context, "Media Deleted", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun ContentItem(item: Map<String, Any>, onDelete: () -> Unit) {
    val type = item["type"] as? String ?: "audio"
    val name = item["name"] as? String ?: "Unnamed Media"

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = CardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = PremiumGold.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (type == "video") Icons.Default.VideoLibrary else Icons.Default.MusicNote,
                        null,
                        tint = PremiumGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    name, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 16.sp, 
                    color = Color.White,
                    fontFamily = PoppinsFamily
                )
                Text(
                    type.replaceFirstChar { it.uppercase() }, 
                    color = PremiumGray, 
                    fontSize = 12.sp,
                    fontFamily = PoppinsFamily
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete, 
                    null, 
                    tint = Color.Red.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
