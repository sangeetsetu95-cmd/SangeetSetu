package com.sangeetsetu.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.res.stringResource
import com.sangeetsetu.app.R
import com.sangeetsetu.app.model.User
import com.sangeetsetu.app.ui.theme.*

@Composable
fun PremiumArtistCard(
    artist: User,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onFavoriteToggle: () -> Unit = {},
    isFavorite: Boolean = false
) {
    Surface(
        modifier = modifier
            .padding(vertical = 4.dp)
            .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = PremiumGold.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(24.dp),
        color = CardBackground,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                AsyncImage(
                    model = artist.photoUrl,
                    contentDescription = artist.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Verified Badge
                if (artist.verificationStatus == "VERIFIED") {
                    Surface(
                        color = PremiumGold,
                        shape = RoundedCornerShape(bottomEnd = 12.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Verified, null, tint = Color.Black, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.verified_badge), color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Favorite Button
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(36.dp)
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        null,
                        tint = if (isFavorite) Color.Red else PremiumWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Rating Tag
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Star, null, tint = PremiumGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(artist.rating.toString(), color = PremiumWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp).clickable { onClick() }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = artist.name,
                        color = PremiumWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                }
                
                Text(
                    text = artist.category,
                    color = PremiumGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = SecondaryText, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(artist.city, color = SecondaryText, fontSize = 12.sp)
                        }
                        Text(
                            text = stringResource(R.string.years_exp_suffix, artist.experience ?: "5"),
                            color = SecondaryText,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    
                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumGold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Text(stringResource(R.string.book_now), color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
