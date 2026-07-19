package com.sangeetsetu.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sangeetsetu.app.model.Event
import com.sangeetsetu.app.ui.theme.*

@Composable
fun LiveEventCard(
    event: Event,
    onEventClick: () -> Unit,
    onBookClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(260.dp)
            .clickable { onEventClick() },
        shape = RoundedCornerShape(20.dp),
        color = CardBackground,
        border = BorderStroke(1.dp, BorderColor),
        shadowElevation = 10.dp
    ) {
        Column {
            Box(modifier = Modifier.height(140.dp).fillMaxWidth()) {
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
                )
                
                // LIVE Badge
                Surface(
                    modifier = Modifier.padding(12.dp).align(Alignment.TopStart),
                    color = Color.Red,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("LIVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }

                // Date Badge Overlay
                Box(
                    modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)))),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(id = android.R.drawable.ic_menu_my_calendar), null, tint = PremiumGold, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${event.day} ${event.month}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = event.title, 
                    color = PremiumWhite, 
                    style = Typography.titleLarge, 
                    fontSize = 15.sp,
                    fontFamily = NotoSansDevanagariFamily, 
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(painterResource(id = android.R.drawable.ic_menu_mylocation), null, tint = SecondaryText, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = event.location, color = SecondaryText, fontSize = 12.sp, fontFamily = NotoSansDevanagariFamily)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    PremiumButton(
                        text = "बुक करें",
                        onClick = onBookClick,
                        modifier = Modifier.width(80.dp).height(32.dp)
                    )
                }
            }
        }
    }
}
