package com.sangeetsetu.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.sangeetsetu.app.model.User
import com.sangeetsetu.app.navigation.Screen
import com.sangeetsetu.app.ui.components.AdminMessageDialog
import com.sangeetsetu.app.ui.components.LocationSelector
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserListScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val users by viewModel.users.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedUserForEdit by remember { mutableStateOf<User?>(null) }
    var userToDelete by remember { mutableStateOf<User?>(null) }
    var userToChat by remember { mutableStateOf<User?>(null) }
    var showAddUserDialog by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedTypeFilter by remember { mutableStateOf("All") }

    val filteredUsers = remember(searchQuery, users, selectedTypeFilter) {
        users.filter { user ->
            val matchesSearch = if (searchQuery.isEmpty()) true
            else (user.name ?: "").contains(searchQuery, ignoreCase = true) ||
                    (user.phone ?: "").contains(searchQuery) ||
                    (user.email ?: "").contains(searchQuery, ignoreCase = true) ||
                    (user.district ?: "").contains(searchQuery, ignoreCase = true) ||
                    (user.state ?: "").contains(searchQuery, ignoreCase = true)
            
            val matchesType = if (selectedTypeFilter == "All") true
            else (user.userType ?: "User").equals(selectedTypeFilter, ignoreCase = true)
            
            matchesSearch && matchesType
        }
    }

    PremiumAdminTheme {
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                containerColor = AdminCardBackground,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp)) {
                    Text("Filter by User Type", color = PremiumGold, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(20.dp))
                    listOf("All", "User", "Artist", "Organizer").forEach { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    selectedTypeFilter = type
                                    showFilterSheet = false
                                }
                                .padding(vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedTypeFilter == type,
                                onClick = { 
                                    selectedTypeFilter = type
                                    showFilterSheet = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = PremiumGold, unselectedColor = AdminSecondaryText)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(type, color = if (selectedTypeFilter == type) PremiumGold else PremiumWhite, fontSize = 16.sp, fontWeight = if (selectedTypeFilter == type) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
        }

        if (selectedUserForEdit != null) {
            EditUserDialog(
                user = selectedUserForEdit!!,
                onDismiss = { selectedUserForEdit = null },
                onSave = { updatedUser ->
                    viewModel.updateUserProfile(updatedUser) {
                        Toast.makeText(context, "User profile updated", Toast.LENGTH_SHORT).show()
                        selectedUserForEdit = null
                    }
                }
            )
        }

        if (showAddUserDialog) {
            AddUserDialog(
                onDismiss = { showAddUserDialog = false },
                onAdd = { newUser: User ->
                    viewModel.addUser(newUser) {
                        Toast.makeText(context, "User added successfully", Toast.LENGTH_SHORT).show()
                        showAddUserDialog = false
                    }
                }
            )
        }

        if (userToDelete != null) {
            AlertDialog(
                onDismissRequest = { userToDelete = null },
                containerColor = AdminCardBackground,
                title = { Text("Terminate Account?", color = ErrorColor, fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to delete ${userToDelete?.name}'s account?", color = PremiumWhite) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteUser(userToDelete!!.uid) {
                                Toast.makeText(context, "Account Terminated", Toast.LENGTH_SHORT).show()
                                userToDelete = null
                            }
                        }
                    ) {
                        Text("DELETE", color = ErrorColor, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { userToDelete = null }) {
                        Text("CANCEL", color = AdminSecondaryText)
                    }
                }
            )
        }

        if (userToChat != null) {
            AdminMessageDialog(
                recipientName = userToChat!!.name,
                onDismiss = { userToChat = null },
                onSend = { message: String ->
                    viewModel.sendAdminMessage(userToChat!!.uid, message) {
                        Toast.makeText(context, "Message sent to ${userToChat!!.name}", Toast.LENGTH_SHORT).show()
                        userToChat = null
                    }
                }
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Column {
                            Text("User Management", color = PremiumGold, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                            Text("Total: ${users.size} active users", color = AdminSecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumGold)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showFilterSheet = true }) { Icon(Icons.Rounded.FilterList, null, tint = PremiumGold) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AdminBackground)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddUserDialog = true },
                    containerColor = PremiumGold,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.Black)
                }
            },
            bottomBar = {
                AdminBottomNav(currentRoute = Screen.AdminUserList.route, onNavigate = onNavigate)
            },
            containerColor = AdminBackground
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AdminBackground
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        placeholder = { Text("Search users...", color = AdminSecondaryText, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = PremiumGold) },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = AdminCardBackground,
                            unfocusedContainerColor = AdminCardBackground,
                            focusedBorderColor = PremiumGold,
                            unfocusedBorderColor = Color.White.copy(0.1f),
                            focusedTextColor = PremiumWhite,
                            unfocusedTextColor = PremiumWhite,
                            cursorColor = PremiumGold
                        )
                    )
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PremiumGold)
                    }
                } else if (filteredUsers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Person, null, tint = AdminSecondaryText.copy(0.2f), modifier = Modifier.size(80.dp))
                            Text("No users found matching '$searchQuery'", color = AdminSecondaryText)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredUsers, key = { it.uid }) { user ->
                            LuxuryUserItem(
                                user = user,
                                onEdit = { selectedUserForEdit = user },
                                onDelete = { userToDelete = user },
                                onChat = { userToChat = user },
                                onStatusToggle = {
                                    val newStatus = if (user.status == "suspended") "active" else "suspended"
                                    viewModel.updateUserStatus(user.uid, newStatus) {
                                        Toast.makeText(context, "Status updated to $newStatus", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddUserDialog(
    onDismiss: () -> Unit,
    onAdd: (User) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var userType by remember { mutableStateOf("User") }
    var state by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }

    val userTypes = listOf("User", "Artist", "Organizer")
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AdminCardBackground,
        title = { Text("Add New User", color = PremiumGold, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                EditField("Full Name", name) { name = it }
                EditField("Email Address", email) { email = it }
                EditField("Phone Number", phone) { phone = it }
                
                // User Type Selection
                Column {
                    Text("User Type", color = AdminSecondaryText, fontSize = 12.sp, modifier = Modifier.padding(bottom = 6.dp))
                    Box {
                        OutlinedTextField(
                            value = userType,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { expanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, null, tint = PremiumGold)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PremiumGold,
                                unfocusedBorderColor = Color.White.copy(0.1f),
                                focusedTextColor = PremiumWhite,
                                unfocusedTextColor = PremiumWhite
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(AdminCardBackground)
                        ) {
                            userTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type, color = PremiumWhite) },
                                    onClick = {
                                        userType = type
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                LocationSelector(
                    selectedState = state,
                    selectedDistrict = district,
                    onStateSelected = { state = it },
                    onDistrictSelected = { district = it }
                )
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (name.isNotBlank() && email.isNotBlank()) Color.Transparent else Color.Gray)
                    .let { if (name.isNotBlank() && email.isNotBlank()) it.background(GoldenGradient) else it }
                    .clickable(enabled = name.isNotBlank() && email.isNotBlank()) { 
                        onAdd(User(
                            name = name, 
                            email = email, 
                            phone = phone, 
                            userType = userType,
                            state = state, 
                            district = district,
                            status = "active"
                        )) 
                    }
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Add User", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AdminSecondaryText)
            }
        }
    )
}

@Composable
fun LuxuryUserItem(
    user: User, 
    onEdit: () -> Unit, 
    onDelete: () -> Unit,
    onChat: () -> Unit = {},
    onStatusToggle: () -> Unit
) {
    val isSuspended = user.status == "suspended"
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = AdminCardBackground,
        border = BorderStroke(1.dp, if (isSuspended) ErrorColor.copy(0.4f) else Color.White.copy(0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(54.dp),
                    shape = CircleShape,
                    color = PremiumGold.copy(0.12f),
                    border = BorderStroke(1.5.dp, PremiumGold.copy(0.4f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text((user.name ?: "U").take(1).uppercase(), color = PremiumGold, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(user.name, color = PremiumWhite, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        if (user.role == "admin") {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(color = PremiumGold, shape = RoundedCornerShape(6.dp)) {
                                Text(" ADMIN ", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val statusText = if (user.isOnline) "🟢 Online" else "🔴 ${com.sangeetsetu.app.util.formatLastSeen(user.lastSeen)}"
                        Text(statusText, color = if (user.isOnline) SuccessColor else AdminSecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(user.userType, color = PremiumGold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    if (user.district.isNotEmpty()) {
                        Text("${user.district}, ${user.state}", color = AdminSecondaryText, fontSize = 11.sp)
                    }
                }

                StatusBadge(if (isSuspended) "Suspended" else "Active", if (isSuspended) ErrorColor else SuccessColor)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                UserInfoSmall(Icons.Rounded.Phone, user.phone, Modifier.weight(1f))
                UserInfoSmall(Icons.Rounded.Email, user.email, Modifier.weight(1.2f))
            }
            
            HorizontalDivider(color = Color.White.copy(0.08f), modifier = Modifier.padding(vertical = 12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onStatusToggle) {
                    Text(if (isSuspended) "Activate" else "Suspend", color = if (isSuspended) SuccessColor else WarningColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Rounded.Edit, null, tint = PremiumGold, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onChat, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Chat, null, tint = PremiumGold, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Rounded.Delete, null, tint = ErrorColor, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun StatusBadge(text: String, color: Color) {
    Surface(
        color = color.copy(0.12f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(0.4f))
    ) {
        Text(
            text, 
            color = color, 
            fontSize = 10.sp, 
            fontWeight = FontWeight.ExtraBold, 
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun UserInfoSmall(icon: ImageVector, text: String, modifier: Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = AdminSecondaryText, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, color = AdminSecondaryText, fontSize = 12.sp, maxLines = 1)
    }
}

@Composable
fun EditUserDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: (User) -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var email by remember { mutableStateOf(user.email) }
    var phone by remember { mutableStateOf(user.phone) }
    var state by remember { mutableStateOf(user.state) }
    var district by remember { mutableStateOf(user.district) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AdminCardBackground,
        title = { Text("Edit User Profile", color = PremiumGold, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                EditField("Full Name", name) { name = it }
                EditField("Email Address", email) { email = it }
                EditField("Phone Number", phone) { phone = it }
                
                LocationSelector(
                    selectedState = state,
                    selectedDistrict = district,
                    onStateSelected = { state = it },
                    onDistrictSelected = { district = it }
                )
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GoldenGradient)
                    .clickable { onSave(user.copy(name = name, email = email, phone = phone, state = state, district = district)) }
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AdminSecondaryText)
            }
        }
    )
}

@Composable
fun EditField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(label, color = AdminSecondaryText, fontSize = 12.sp, modifier = Modifier.padding(bottom = 6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(color = PremiumWhite, fontSize = 14.sp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PremiumWhite,
                unfocusedTextColor = PremiumWhite,
                focusedBorderColor = PremiumGold,
                unfocusedBorderColor = Color.White.copy(0.1f),
                cursorColor = PremiumGold
            )
        )
    }
}
