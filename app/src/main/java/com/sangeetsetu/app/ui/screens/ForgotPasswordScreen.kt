package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sangeetsetu.app.ui.theme.*

@Composable
fun ForgotPasswordScreen(
    isLoading: Boolean = false,
    onResetRequested: (String) -> Unit,
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    val isEmailValid = email.contains("@") && email.contains(".")

    Scaffold(
        containerColor = PremiumNavy
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "Reset Password",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = PremiumWhite
            )
            Text(
                text = "Enter your email to receive a reset link",
                fontSize = 16.sp,
                color = PremiumGray
            )

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address", color = PremiumGray) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Email, null, tint = PremiumGold) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PremiumWhite,
                    unfocusedTextColor = PremiumWhite,
                    focusedBorderColor = PremiumGold,
                    unfocusedBorderColor = CardBorder
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onResetRequested(email) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isEmailValid && !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PremiumGold),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = PremiumNavy, strokeWidth = 2.dp)
                } else {
                    Text("Send Reset Link", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PremiumNavy)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onBackToLogin) {
                Text("Back to Login", color = PremiumGold, fontWeight = FontWeight.Bold)
            }
        }
    }
}
