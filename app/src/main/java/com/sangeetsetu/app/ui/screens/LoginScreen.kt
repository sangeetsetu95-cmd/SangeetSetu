package com.sangeetsetu.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sangeetsetu.app.R
import com.sangeetsetu.app.ui.components.PremiumButton
import com.sangeetsetu.app.ui.components.PremiumTextField
import com.sangeetsetu.app.ui.theme.*

import androidx.compose.ui.res.stringResource

@Composable
fun LoginScreen(
    isLoading: Boolean = false,
    onLoginRequested: (String, String) -> Unit,
    onSignUpClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onGoogleLoginRequested: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showContent = true
    }

    val isEmailValid = email.contains("@") && email.contains(".")
    val isFormValid = isEmailValid && password.length >= 6

    Scaffold(
        containerColor = AppBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PremiumBackgroundDecoration()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))

                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(1000)) + scaleIn(tween(1000))
                ) {
                    LoginLogoHeader()
                }

                Spacer(modifier = Modifier.height(48.dp))

                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(800, 400)) + slideInVertically(tween(800, 400)) { it / 2 }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(32.dp))
                            .background(CardBackground)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(32.dp))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            stringResource(R.string.welcome_back),
                            style = Typography.headlineMedium,
                            color = PremiumGold,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            stringResource(R.string.signin_subtitle),
                            style = Typography.bodyMedium,
                            color = SecondaryText,
                            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
                        )

                        PremiumTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = stringResource(R.string.email_address),
                            leadingIcon = Icons.Default.Email,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        var passwordVisible by remember { mutableStateOf(false) }
                        PremiumTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = stringResource(R.string.password),
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

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                stringResource(R.string.forgot_password),
                                modifier = Modifier.clickable { onForgotPasswordClick() },
                                style = Typography.labelMedium,
                                color = PremiumGold,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        PremiumButton(
                            text = stringResource(R.string.login_button),
                            onClick = { onLoginRequested(email, password) },
                            enabled = isFormValid,
                            isLoading = isLoading
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.1f))
                            Text(
                                "  ${stringResource(R.string.or_text)}  ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryText
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.1f))
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        OutlinedButton(
                            onClick = onGoogleLoginRequested,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PremiumWhite)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_google_logo),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(stringResource(R.string.continue_with_google), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 40.dp)
                ) {
                    Text(stringResource(R.string.dont_have_account), color = SecondaryText)
                    TextButton(onClick = onSignUpClick) {
                        Text(stringResource(R.string.signup_text), color = PremiumGold, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun LoginLogoHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = R.drawable.sangeet_setu_logo),
            contentDescription = null,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "SANGEET SETU",
            style = Typography.headlineLarge,
            color = PremiumGold,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp
        )
    }
}

@Composable
fun PremiumBackgroundDecoration() {
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(PremiumGold.copy(alpha = 0.05f), Color.Transparent),
                    center = Offset(size.width, 0f),
                    radius = size.width
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(PremiumGold.copy(alpha = 0.05f), Color.Transparent),
                    center = Offset(0f, size.height),
                    radius = size.width
                )
            )
        }
    }
}
