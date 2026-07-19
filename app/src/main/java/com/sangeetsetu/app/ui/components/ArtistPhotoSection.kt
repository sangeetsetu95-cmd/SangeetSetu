package com.sangeetsetu.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sangeetsetu.app.ui.theme.AppBackground
import com.sangeetsetu.app.ui.theme.CardBackground
import com.sangeetsetu.app.ui.theme.PremiumGold

/**
 * A reusable photo section for artists (and potentially users).
 * Displays the current photo with an upload overlay and a removal option.
 */
@Composable
fun ArtistPhotoSection(
    photoUrl: String,
    isUploading: Boolean,
    onUploadClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            if (isUploading) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(CardBackground)
                        .border(2.dp, PremiumGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PremiumGold, strokeWidth = 3.dp)
                }
            } else {
                AsyncImage(
                    model = photoUrl.takeIf { it.isNotEmpty() },
                    contentDescription = "Artist Profile Photo",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, PremiumGold, CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = rememberVectorPainter(Icons.Default.Person),
                    error = rememberVectorPainter(Icons.Default.Person)
                )
            }
            
            // Upload Button Overlay
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(38.dp)
                    .offset(x = (-4).dp, y = (-4).dp)
                    .clickable(enabled = !isUploading) { onUploadClick() },
                shape = CircleShape,
                color = PremiumGold,
                border = BorderStroke(2.dp, AppBackground),
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Change Photo",
                        tint = AppBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        
        if (photoUrl.isNotEmpty() && !isUploading) {
            TextButton(
                onClick = onRemoveClick,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red.copy(alpha = 0.8f))
            ) {
                Text("Remove Photo", fontSize = 12.sp)
            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
