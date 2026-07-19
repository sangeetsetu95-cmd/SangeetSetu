package com.sangeetsetu.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sangeetsetu.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    onDismiss: () -> Unit,
    onApply: (FilterOptions) -> Unit
) {
    var selectedDistrict by remember { mutableStateOf("") }
    var selectedState by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf("All") }
    var sortBy by remember { mutableStateOf("Rating") }
    var isVIP by remember { mutableStateOf(false) }
    var isVerified by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(0.2f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filters", color = PremiumWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, null, tint = PremiumWhite)
                }
            }

            // State & District
            FilterSection("Location") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PremiumTextField(
                        value = selectedState,
                        onValueChange = { selectedState = it },
                        label = "State",
                        modifier = Modifier.weight(1f)
                    )
                    PremiumTextField(
                        value = selectedDistrict,
                        onValueChange = { selectedDistrict = it },
                        label = "District",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Availability
            FilterSection("Availability") {
                val options = listOf("All", "Available Today", "This Weekend")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(options) { opt ->
                        FilterChipItem(opt, availability == opt) { availability = opt }
                    }
                }
            }

            // Sort By
            FilterSection("Sort By") {
                val options = listOf("Rating", "Newest First", "Most Booked")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(options) { opt ->
                        FilterChipItem(opt, sortBy == opt) { sortBy = opt }
                    }
                }
            }

            // Badges
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isVIP,
                        onCheckedChange = { isVIP = it },
                        colors = CheckboxDefaults.colors(checkedColor = PremiumGold)
                    )
                    Text("VIP Only", color = PremiumWhite)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isVerified,
                        onCheckedChange = { isVerified = it },
                        colors = CheckboxDefaults.colors(checkedColor = NeonBlue)
                    )
                    Text("Verified Only", color = PremiumWhite)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = {
                        selectedDistrict = ""
                        selectedState = ""
                        availability = "All"
                        sortBy = "Rating"
                        isVIP = false
                        isVerified = false
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    border = BorderStroke(1.dp, Color.White.copy(0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reset", color = PremiumWhite)
                }
                
                PremiumButton(
                    text = "Apply Filters",
                    onClick = {
                        onApply(FilterOptions(selectedState, selectedDistrict, availability, sortBy, isVIP, isVerified))
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f).height(50.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun FilterSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, color = SecondaryText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        content()
    }
}

@Composable
fun FilterChipItem(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (selected) PremiumGold.copy(0.1f) else Color.White.copy(0.05f),
        border = BorderStroke(1.dp, if (selected) PremiumGold else Color.White.copy(0.1f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            label,
            color = if (selected) PremiumGold else PremiumWhite,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 12.sp
        )
    }
}

data class FilterOptions(
    val state: String,
    val district: String,
    val availability: String,
    val sortBy: String,
    val isVIP: Boolean,
    val isVerified: Boolean
)
