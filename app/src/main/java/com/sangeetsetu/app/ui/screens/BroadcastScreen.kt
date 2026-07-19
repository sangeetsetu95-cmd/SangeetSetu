package com.sangeetsetu.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sangeetsetu.app.model.Announcement
import com.sangeetsetu.app.model.Category
import com.sangeetsetu.app.ui.theme.AppBackground
import com.sangeetsetu.app.ui.theme.CardBorder
import com.sangeetsetu.app.ui.theme.PremiumGold
import com.sangeetsetu.app.ui.theme.PremiumGray
import com.sangeetsetu.app.ui.theme.PremiumNavy
import com.sangeetsetu.app.ui.theme.PremiumNavyDark
import com.sangeetsetu.app.ui.theme.PremiumNavyLight
import com.sangeetsetu.app.ui.theme.PremiumWhite
import com.sangeetsetu.app.util.formatTimeAgo
import com.sangeetsetu.app.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BroadcastScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var actionLink by remember { mutableStateOf("") }
    var actionLabel by remember { mutableStateOf("") }
    var expiryDays by remember { mutableStateOf(30) }
    var targetType by remember { mutableStateOf("ALL") }
    var selectedCategories by remember { mutableStateOf(setOf<String>()) }
    
    val isSending by viewModel.isLoading.collectAsStateWithLifecycle()
    val announcements by viewModel.announcements.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    var showTargetDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    LaunchedEffect(error) {
        error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    if (showTargetDialog) {
        TargetTypeSelectionDialog(
            current = targetType,
            onDismiss = { showTargetDialog = false },
            onSelect = { 
                targetType = it
                showTargetDialog = false
                if (it == "CATEGORY") showCategoryDialog = true
            }
        )
    }

    if (showCategoryDialog) {
        CategorySelectionDialog(
            categories = categories,
            selected = selectedCategories,
            onDismiss = { showCategoryDialog = false },
            onSelectionChange = { selectedCategories = it }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Professional Broadcast", color = PremiumWhite, fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PremiumNavyLight),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Compose Announcement", color = PremiumGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Target Selector
                        OutlinedCard(
                            onClick = { showTargetDialog = true },
                            colors = CardDefaults.outlinedCardColors(containerColor = AppBackground),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Group, null, tint = PremiumGold)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Target Audience", color = PremiumGray, fontSize = 12.sp)
                                    Text(targetType, color = PremiumWhite, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.Edit, null, tint = PremiumGray, modifier = Modifier.size(16.dp))
                            }
                        }

                        AnimatedVisibility(visible = targetType == "CATEGORY") {
                            TextButton(onClick = { showCategoryDialog = true }) {
                                Text("Selected Categories: ${selectedCategories.joinToString()}", color = PremiumGold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title", color = PremiumGray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = PremiumWhite, unfocusedTextColor = PremiumWhite, focusedBorderColor = PremiumGold, unfocusedBorderColor = CardBorder)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = message,
                            onValueChange = { message = it },
                            label = { Text("Message Body", color = PremiumGray) },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = PremiumWhite, unfocusedTextColor = PremiumWhite, focusedBorderColor = PremiumGold, unfocusedBorderColor = CardBorder)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = imageUrl,
                            onValueChange = { imageUrl = it },
                            label = { Text("Image URL (Optional)", color = PremiumGray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = PremiumWhite, unfocusedTextColor = PremiumWhite, focusedBorderColor = PremiumGold, unfocusedBorderColor = CardBorder)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = actionLink,
                            onValueChange = { actionLink = it },
                            label = { Text("Action Link / Button URL (Optional)", color = PremiumGray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = PremiumWhite, unfocusedTextColor = PremiumWhite, focusedBorderColor = PremiumGold, unfocusedBorderColor = CardBorder)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = actionLabel,
                            onValueChange = { actionLabel = it },
                            label = { Text("Button Label (e.g. Apply Now)", color = PremiumGray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = PremiumWhite, unfocusedTextColor = PremiumWhite, focusedBorderColor = PremiumGold, unfocusedBorderColor = CardBorder)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Auto Expiry: $expiryDays Days", color = PremiumGray, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Slider(
                                value = expiryDays.toFloat(),
                                onValueChange = { expiryDays = it.toInt() },
                                valueRange = 1f..365f,
                                modifier = Modifier.weight(2f),
                                colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = PremiumGold, activeTrackColor = PremiumGold)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                viewModel.sendAnnouncement(title, message, selectedCategories.toList(), imageUrl.takeIf { it.isNotEmpty() }, actionLink.takeIf { it.isNotEmpty() }, targetType, actionLabel.takeIf { it.isNotEmpty() }, expiryDays = expiryDays) {
                                    Toast.makeText(context, "Broadcast Published Successfully!", Toast.LENGTH_SHORT).show()
                                    title = ""; message = ""; imageUrl = ""; actionLink = ""; actionLabel = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = title.isNotEmpty() && message.isNotEmpty() && !isSending,
                            colors = ButtonDefaults.buttonColors(containerColor = PremiumGold),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isSending) CircularProgressIndicator(color = PremiumNavy, modifier = Modifier.size(24.dp))
                            else {
                                Icon(Icons.Default.Send, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Publish Broadcast", color = PremiumNavy, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Broadcast History", color = PremiumGray, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { viewModel.deleteAllAnnouncements {} }) {
                        Text("Clear History", color = Color.Red.copy(0.7f), fontSize = 12.sp)
                    }
                }
            }

            items(announcements) { announcement ->
                AnnouncementAdminItem(announcement) {
                    viewModel.deleteAnnouncement(announcement.id) {
                        Toast.makeText(context, "Broadcast Deleted", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

@Composable
fun TargetTypeSelectionDialog(current: String, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    val options = listOf("ALL", "USERS", "ARTISTS", "VIP_USERS", "VERIFIED_ARTISTS", "CATEGORY", "LOCATION")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Target Audience", color = PremiumGold) },
        text = {
            LazyColumn {
                items(options) { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth().selectable(selected = (option == current), onClick = { onSelect(option) }).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = (option == current), onClick = null)
                        Text(option, color = PremiumWhite, modifier = Modifier.padding(start = 16.dp))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        containerColor = PremiumNavyLight
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectionDialog(
    categories: List<Category>,
    selected: Set<String>,
    onDismiss: () -> Unit,
    onSelectionChange: (Set<String>) -> Unit
) {
    var tempSelected by remember { mutableStateOf(selected) }
    
    val staticOptions = listOf("All Users", "All Artists")
    val dynamicCategories = categories.map { it.name }.sorted()
    val allOptions = staticOptions + dynamicCategories

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(24.dp),
            color = PremiumNavyLight,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recipient Categories", color = PremiumGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = PremiumWhite)
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TextButton(onClick = { tempSelected = allOptions.toSet() }) {
                        Text("Select All", color = PremiumGold)
                    }
                    TextButton(onClick = { tempSelected = emptySet() }) {
                        Text("Clear All", color = PremiumGray)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        Text("Primary Groups", color = PremiumGray, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
                    }
                    items(staticOptions) { option ->
                        CategorySelectItem(
                            name = option,
                            isSelected = tempSelected.contains(option),
                            onToggle = {
                                tempSelected = if (tempSelected.contains(option)) {
                                    tempSelected - option
                                } else {
                                    tempSelected + option
                                }
                            }
                        )
                    }
                    
                    item {
                        HorizontalDivider(color = CardBorder, modifier = Modifier.padding(vertical = 12.dp))
                        Text("Artist Categories", color = PremiumGray, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
                    }
                    
                    items(dynamicCategories) { category ->
                        CategorySelectItem(
                            name = category,
                            isSelected = tempSelected.contains(category),
                            onToggle = {
                                tempSelected = if (tempSelected.contains(category)) {
                                    tempSelected - category
                                } else {
                                    tempSelected + category
                                }
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Button(
                    onClick = {
                        onSelectionChange(tempSelected)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply Selection (${tempSelected.size})", color = PremiumNavy, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CategorySelectItem(name: String, isSelected: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .toggleable(
                value = isSelected,
                onValueChange = { onToggle() },
                role = Role.Checkbox
            )
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(
                checkedColor = PremiumGold,
                uncheckedColor = PremiumGray,
                checkmarkColor = PremiumNavy
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(name, color = if (isSelected) PremiumWhite else PremiumGray, fontSize = 15.sp)
    }
}

@Composable
fun AnnouncementAdminItem(announcement: Announcement, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PremiumNavyLight),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder.copy(0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(announcement.title, color = PremiumGold, fontWeight = FontWeight.Bold)
                    Text(announcement.content, color = PremiumWhite, fontSize = 13.sp, maxLines = 2)
                    Text("To: ${announcement.targetType}", color = PremiumGray, fontSize = 10.sp)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = Color.Red.copy(0.6f), modifier = Modifier.size(20.dp))
                }
            }
            
            if (!announcement.imageUrl.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = announcement.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatTimeAgo(announcement.createdAt), color = PremiumGray, fontSize = 10.sp)
                Row {
                    Icon(Icons.Default.Visibility, null, tint = PremiumGray, modifier = Modifier.size(12.dp))
                    Text(" ${announcement.readCount} Reads", color = PremiumGray, fontSize = 10.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Send, null, tint = PremiumGray, modifier = Modifier.size(12.dp))
                    Text(" ${announcement.sentCount} Sent", color = PremiumGray, fontSize = 10.sp)
                }
            }
        }
    }
}
