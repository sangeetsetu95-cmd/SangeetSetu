package com.sangeetsetu.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sangeetsetu.app.model.Bhajan
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.viewmodel.AdminViewModel
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBhajansScreen(onBack: () -> Unit, viewModel: AdminViewModel = hiltViewModel()) {
    val db = remember { FirebaseFirestore.getInstance() }
    val context = LocalContext.current
    var bhajans by remember { mutableStateOf<List<Bhajan>>(emptyList()) }
    var title by remember { mutableStateOf("") }
    var artistName by remember { mutableStateOf("") }
    var isLoadingLocal by remember { mutableStateOf(false) }
    var editingBhajan by remember { mutableStateOf<Bhajan?>(null) }

    val fetchBhajans = remember {
        {
            db.collection("bhajans").get()
                .addOnSuccessListener { bhajans = it.toObjects(Bhajan::class.java) }
        }
    }

    LaunchedEffect(Unit) { fetchBhajans() }

    PremiumAdminTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Manage Bhajans", color = PremiumGold, fontWeight = FontWeight.ExtraBold) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumGold) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AdminBackground)
                )
            },
            containerColor = AdminBackground
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                Surface(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = AdminCardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.08f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (editingBhajan == null) "Add New Bhajan" else "Edit Bhajan",
                                fontWeight = FontWeight.Bold,
                                color = PremiumGold,
                                fontSize = 17.sp
                            )
                            if (editingBhajan != null) {
                                IconButton(onClick = {
                                    editingBhajan = null
                                    title = ""
                                    artistName = ""
                                }) {
                                    Icon(Icons.Default.Close, null, tint = ErrorColor, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        OutlinedTextField(
                            value = title, 
                            onValueChange = { title = it }, 
                            label = { Text("Bhajan Title", color = AdminSecondaryText) }, 
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PremiumWhite,
                                unfocusedTextColor = PremiumWhite,
                                focusedBorderColor = PremiumGold,
                                unfocusedBorderColor = Color.White.copy(0.1f),
                                cursorColor = PremiumGold
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = artistName, 
                            onValueChange = { artistName = it }, 
                            label = { Text("Artist Name", color = AdminSecondaryText) }, 
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PremiumWhite,
                                unfocusedTextColor = PremiumWhite,
                                focusedBorderColor = PremiumGold,
                                unfocusedBorderColor = Color.White.copy(0.1f),
                                cursorColor = PremiumGold
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (title.isNotEmpty() && artistName.isNotEmpty() && !isLoadingLocal) Color.Transparent else Color.Gray)
                                .let { 
                                    if (title.isNotEmpty() && artistName.isNotEmpty() && !isLoadingLocal) it.background(GoldenGradient) else it 
                                }
                                .clickable(enabled = title.isNotEmpty() && artistName.isNotEmpty() && !isLoadingLocal) {
                                    isLoadingLocal = true
                                    val isUpdate = editingBhajan != null
                                    val id = editingBhajan?.id ?: db.collection("bhajans").document().id
                                    val bhajan = Bhajan(id = id, title = title, artistName = artistName)
                                    viewModel.addBhajan(bhajan) {
                                        isLoadingLocal = false
                                        title = ""; artistName = ""
                                        editingBhajan = null
                                        fetchBhajans()
                                        Toast.makeText(context, if (isUpdate) "Bhajan Updated" else "Bhajan Added", Toast.LENGTH_SHORT).show()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoadingLocal) CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(26.dp), strokeWidth = 2.5.dp)
                            else Text(if (editingBhajan == null) "ADD BHAJAN" else "UPDATE BHAJAN", fontWeight = FontWeight.ExtraBold, color = Color.Black, letterSpacing = 1.sp)
                        }
                    }
                }

                Text("Total Bhajans: ${bhajans.size}", modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp), color = PremiumWhite, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(bhajans, key = { it.id }) { bhajan ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = AdminCardBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.08f))
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(46.dp).background(PremiumGold.copy(0.12f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.MusicNote, null, tint = PremiumGold, modifier = Modifier.size(26.dp))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(bhajan.title, fontWeight = FontWeight.Bold, color = PremiumWhite, fontSize = 16.sp)
                                    Text(bhajan.artistName, fontSize = 13.sp, color = AdminSecondaryText, fontWeight = FontWeight.Medium)
                                }
                                
                                IconButton(onClick = {
                                    editingBhajan = bhajan
                                    title = bhajan.title
                                    artistName = bhajan.artistName
                                }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Edit, null, tint = PremiumGold, modifier = Modifier.size(20.dp)) }

                                IconButton(onClick = {
                                    viewModel.deleteBhajan(bhajan.id) {
                                        fetchBhajans()
                                        Toast.makeText(context, "Bhajan Deleted", Toast.LENGTH_SHORT).show()
                                    }
                                }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Delete, null, tint = ErrorColor, modifier = Modifier.size(20.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}
