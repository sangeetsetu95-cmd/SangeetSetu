package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sangeetsetu.app.navigation.Screen
import com.sangeetsetu.app.ui.theme.SangeetSetuDesign
import com.sangeetsetu.app.ui.theme.PremiumGold
import com.sangeetsetu.app.ui.theme.SecondaryText

@Composable
fun AdminBottomNav(currentRoute: String, onNavigate: (String) -> Unit) {
    Surface(
        color = Color(0xFF0B0F1A), 
        shape = RoundedCornerShape(topStart = SangeetSetuDesign.CardCorner, topEnd = SangeetSetuDesign.CardCorner),
        border = BorderStroke(1.dp, Color.White.copy(0.05f)),
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding() 
    ) {
        // NavigationBar with a fixed height and centered content.
        NavigationBar(
            containerColor = Color.Transparent, 
            modifier = Modifier.height(84.dp),
            windowInsets = WindowInsets(0, 0, 0, 0) // Padding is handled by navigationBarsPadding above
        ) {
            val items = listOf(
                NavigationItemData("Dashboard", Icons.Rounded.Dashboard, Screen.AdminDashboard.route),
                NavigationItemData("Artists", Icons.Rounded.Mic, Screen.AdminArtistList.route),
                NavigationItemData("Bookings", Icons.Rounded.Bookmark, Screen.AdminBookings.route),
                NavigationItemData("Payments", Icons.Rounded.Payments, Screen.AdminPayments.route),
                NavigationItemData("More", Icons.Rounded.MoreHoriz, Screen.AdminMore.route)
            )
            
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { if (currentRoute != item.route) onNavigate(item.route) },
                    icon = { 
                        Icon(
                            item.icon, 
                            contentDescription = item.label,
                            modifier = Modifier.size(26.dp)
                        ) 
                    },
                    label = { 
                        Text(
                            item.label, 
                            fontSize = 11.sp, 
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ) 
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PremiumGold,
                        selectedTextColor = PremiumGold,
                        indicatorColor = PremiumGold.copy(alpha = 0.15f),
                        unselectedIconColor = Color(0xFFB8C1CC),
                        unselectedTextColor = Color(0xFFB8C1CC)
                    )
                )
            }
        }
    }
}

private data class NavigationItemData(
    val label: String, 
    val icon: androidx.compose.ui.graphics.vector.ImageVector, 
    val route: String
)
