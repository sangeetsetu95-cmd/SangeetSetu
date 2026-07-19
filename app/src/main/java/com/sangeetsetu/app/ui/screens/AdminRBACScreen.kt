package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sangeetsetu.app.model.Role
import com.sangeetsetu.app.ui.theme.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRBACScreen(onBack: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    var roles by remember { mutableStateOf<List<Role>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf<Role?>(null) }

    LaunchedEffect(Unit) {
        val snapshot = db.collection("roles").get().await()
        roles = snapshot.toObjects(Role::class.java)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Role Based Access Control", color = PremiumWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PremiumNavyDark)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    selectedRole = null
                    showDialog = true 
                }, 
                containerColor = PremiumGold,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Icon(Icons.Default.Add, null, tint = AppBackground)
            }
        },
        containerColor = PremiumNavy
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PremiumGold)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(roles) { role ->
                    RoleCard(role, onEdit = {
                        selectedRole = role
                        showDialog = true
                    })
                }
            }
        }
    }

    if (showDialog) {
        RoleEditDialog(
            role = selectedRole,
            onDismiss = { showDialog = false },
            onSave = { updatedRole ->
                // In production, move this to ViewModel
                db.collection("roles").document(updatedRole.name).set(updatedRole)
                showDialog = false
                // Refresh list
                isLoading = true
            }
        )
    }
}

@Composable
fun RoleCard(role: Role, onEdit: () -> Unit) {
    Surface(
        color = PremiumNavyLight,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(role.name.uppercase(), color = PremiumGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Permissions: ${role.permissions.size}", color = SecondaryText, fontSize = 12.sp)
                Text(role.permissions.joinToString(", "), color = PremiumWhite.copy(0.7f), fontSize = 10.sp, maxLines = 1)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, null, tint = PremiumWhite)
            }
        }
    }
}

@Composable
fun RoleEditDialog(role: Role?, onDismiss: () -> Unit, onSave: (Role) -> Unit) {
    var name by remember { mutableStateOf(role?.name ?: "") }
    var permissionsInput by remember { mutableStateOf(role?.permissions?.joinToString(", ") ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (role == null) "Add Role" else "Edit Role", color = PremiumGold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Role Name") },
                    enabled = role == null,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = permissionsInput,
                    onValueChange = { permissionsInput = it },
                    label = { Text("Permissions (comma separated)") },
                    placeholder = { Text("access_admin_panel, manage_users") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val perms = permissionsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                onSave(Role(name, perms))
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = PremiumNavyLight
    )
}
