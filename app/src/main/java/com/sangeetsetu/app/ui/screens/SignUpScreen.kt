package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sangeetsetu.app.ui.components.PremiumButton
import com.sangeetsetu.app.ui.components.PremiumTextField
import com.sangeetsetu.app.ui.theme.*

@Composable
fun SignUpScreen(
    isLoading: Boolean = false,
    initialReferralCode: String = "",
    onSignUpRequested: (String, String, String, String) -> Unit,
    onLoginClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var referralCode by remember { mutableStateOf(initialReferralCode) }
    
    val isFormValid by remember(name, email, password, confirmPassword) {
        derivedStateOf {
            name.isNotEmpty() && 
            email.contains("@") && 
            password.length >= 6 && 
            password == confirmPassword
        }
    }

    Scaffold(
        containerColor = AppBackground
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            PremiumBackgroundDecoration()
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))
                
                Text(
                    "Create Account",
                    style = Typography.headlineLarge,
                    color = PremiumGold,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Join the premium circle of artists and seekers",
                    style = Typography.bodyMedium,
                    color = SecondaryText,
                    modifier = Modifier.padding(top = 8.dp, bottom = 40.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(CardBackground)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(32.dp))
                        .padding(24.dp)
                ) {
                    PremiumTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Full Name",
                        leadingIcon = Icons.Default.Person
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PremiumTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email Address",
                        leadingIcon = Icons.Default.Email,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    var passwordVisible by remember { mutableStateOf(false) }
                    PremiumTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        leadingIcon = Icons.Default.Lock,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = PremiumGold.copy(alpha = 0.6f)
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PremiumTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirm Password",
                        leadingIcon = Icons.Default.Lock,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PremiumTextField(
                        value = referralCode,
                        onValueChange = { referralCode = it },
                        label = "Referral Code (Optional)",
                        leadingIcon = Icons.Default.Person // Or some other icon
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    PremiumButton(
                        text = "CREATE ACCOUNT",
                        onClick = { onSignUpRequested(name, email, password, referralCode) },
                        enabled = isFormValid,
                        isLoading = isLoading
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 40.dp)
                ) {
                    Text("Already have an account?", color = SecondaryText)
                    TextButton(onClick = onLoginClick) {
                        Text("Login", color = PremiumGold, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
