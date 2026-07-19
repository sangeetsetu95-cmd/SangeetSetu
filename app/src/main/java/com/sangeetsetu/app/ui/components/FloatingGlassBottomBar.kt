package com.sangeetsetu.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.EventNote
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sangeetsetu.app.R
import com.sangeetsetu.app.ui.theme.PremiumGold
import com.sangeetsetu.app.ui.theme.PremiumGoldDark

@Composable
fun FloatingGlassBottomBar(
    currentRoute: String,
    onHome: () -> Unit,
    onCategories: () -> Unit,
    onBookings: () -> Unit,
    onChat: () -> Unit,
    onProfile: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, bottom = 32.dp, top = 32.dp), // Increased top padding to prevent FAB cut
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            color = Color.Black.copy(alpha = 0.9f), // Slightly darker for premium look
            shape = RoundedCornerShape(36.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            shadowElevation = 15.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(Icons.Rounded.Home, stringResource(R.string.home), currentRoute == "home", onHome)
                NavItem(Icons.Rounded.GridView, stringResource(R.string.categories), currentRoute == "categories", onCategories)
                
                Spacer(modifier = Modifier.width(56.dp)) // Space for FAB
                
                NavItem(Icons.Rounded.ChatBubbleOutline, stringResource(R.string.chat), currentRoute == "chat", onChat)
                NavItem(Icons.Rounded.PersonOutline, stringResource(R.string.profile), currentRoute == "profile", onProfile)
            }
        }
        
        // Central Golden FAB
        Box(
            modifier = Modifier
                .offset(y = (-30).dp)
                .size(64.dp)
                .shadow(15.dp, CircleShape, spotColor = PremiumGold)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(PremiumGold, PremiumGoldDark))
                )
                .clickable { onBookings() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.EventNote, 
                contentDescription = stringResource(R.string.bookings),
                tint = Color.Black,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun NavItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .width(60.dp)
    ) {
        Icon(
            icon,
            null,
            tint = if (selected) Color(0xFFD4AF37) else Color(0xFFA8A8A8),
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            fontSize = 10.sp,
            color = if (selected) Color(0xFFD4AF37) else Color(0xFFA8A8A8),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
