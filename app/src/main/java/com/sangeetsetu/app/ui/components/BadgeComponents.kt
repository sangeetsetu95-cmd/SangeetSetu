package com.sangeetsetu.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sangeetsetu.app.ui.theme.NeonBlue
import com.sangeetsetu.app.ui.theme.PremiumGold
import com.sangeetsetu.app.viewmodel.MainViewModel

@Composable
fun VerifiedBadge(
    modifier: Modifier = Modifier.size(16.dp),
    tint: Color = NeonBlue,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val badgeConfig by mainViewModel.verifiedBadge.collectAsState()

    if (badgeConfig?.isEnabled == true && badgeConfig?.imageUrl?.isNotEmpty() == true) {
        AsyncImage(
            model = badgeConfig?.imageUrl,
            contentDescription = "Verified",
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    } else {
        Icon(
            imageVector = Icons.Default.Verified,
            contentDescription = "Verified",
            tint = tint,
            modifier = modifier
        )
    }
}

@Composable
fun VipBadge(
    modifier: Modifier = Modifier.size(16.dp),
    tint: Color = PremiumGold,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val badgeConfig by mainViewModel.vipBadge.collectAsState()

    if (badgeConfig?.isEnabled == true && badgeConfig?.imageUrl?.isNotEmpty() == true) {
        AsyncImage(
            model = badgeConfig?.imageUrl,
            contentDescription = "VIP",
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    } else {
        Icon(
            imageVector = Icons.Default.Stars,
            contentDescription = "VIP",
            tint = tint,
            modifier = modifier
        )
    }
}
