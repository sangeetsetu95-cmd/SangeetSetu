package com.sangeetsetu.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sangeetsetu.app.model.BookingStatus
import com.sangeetsetu.app.ui.theme.*

@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = PremiumGold.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(24.dp),
        color = CardBackground,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun PremiumButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = PremiumGold.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.Black
        ),
        contentPadding = PaddingValues(0.dp),
        enabled = enabled && !isLoading
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (enabled && !isLoading) GoldenGradient else Brush.linearGradient(listOf(Color.Gray, Color.DarkGray))),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.Black,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = text,
                    style = Typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label, color = SecondaryText) },
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null, tint = PremiumGold) } },
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = PremiumWhite,
            unfocusedTextColor = PremiumWhite,
            cursorColor = PremiumGold,
            focusedBorderColor = PremiumGold,
            unfocusedBorderColor = BorderColor,
            focusedLabelColor = PremiumGold,
            unfocusedLabelColor = SecondaryText,
            focusedContainerColor = Color.White.copy(alpha = 0.05f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(16.dp),
        singleLine = true
    )
}

@Composable
fun PremiumHeader(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBackClick != null) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = PremiumWhite
                )
            }
        }
        Text(
            text = title,
            style = Typography.headlineMedium,
            color = PremiumGold,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = if (onBackClick == null) 16.dp else 0.dp)
                .weight(1f)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            content = actions
        )
    }
}

@Composable
fun PremiumStatusBadge(status: BookingStatus) {
    val (color, label) = when (status) {
        BookingStatus.PENDING -> Color(0xFFFFC107) to "Pending"
        BookingStatus.ACCEPTED -> Color(0xFF4CAF50) to "Accepted"
        BookingStatus.IN_PROGRESS -> Color(0xFF2196F3) to "In Progress"
        BookingStatus.COMPLETED -> PremiumGold to "Completed"
        BookingStatus.CANCELLED -> Color(0xFFF44336) to "Cancelled"
        else -> Color.Gray to "Unknown"
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
