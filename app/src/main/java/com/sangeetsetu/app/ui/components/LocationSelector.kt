package com.sangeetsetu.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sangeetsetu.app.model.LocationData
import com.sangeetsetu.app.ui.theme.PremiumGold
import com.sangeetsetu.app.ui.theme.PremiumGoldDark
import com.sangeetsetu.app.ui.theme.PremiumWhite
import com.sangeetsetu.app.ui.theme.SecondaryText
import com.sangeetsetu.app.ui.theme.PremiumNavyLight
import com.sangeetsetu.app.ui.theme.PremiumNavyDeep

/**
 * Premium Location Selector Component
 * Redesigned for Sangeet Setu Ultra Luxury UI
 */
@Composable
fun LocationSelector(
    selectedState: String,
    selectedDistrict: String,
    onStateSelected: (String) -> Unit,
    onDistrictSelected: (String) -> Unit,
    states: List<String> = emptyList(),
    districts: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    var showStateDialog by remember { mutableStateOf(false) }
    var showDistrictDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        // State Selector
        LocationField(
            label = "State / Union Territory",
            value = selectedState.ifEmpty { "Select State" },
            onClick = { showStateDialog = true }
        )

        // District Selector
        LocationField(
            label = "District",
            value = selectedDistrict.ifEmpty { "Select District" },
            onClick = { 
                if (selectedState.isNotEmpty()) showDistrictDialog = true 
            },
            enabled = selectedState.isNotEmpty()
        )
    }

    if (showStateDialog) {
        SearchableListDialog(
            title = "Select State",
            list = states.ifEmpty { LocationData.states },
            onDismiss = { showStateDialog = false },
            onSelect = {
                onStateSelected(it)
                onDistrictSelected("") // Reset district when state changes
                showStateDialog = false
            }
        )
    }

    if (showDistrictDialog) {
        val displayDistricts = if (districts.isNotEmpty()) districts else (LocationData.stateDistricts[selectedState] ?: emptyList())
        SearchableListDialog(
            title = "Select District",
            list = displayDistricts,
            onDismiss = { showDistrictDialog = false },
            onSelect = {
                onDistrictSelected(it)
                showDistrictDialog = false
            }
        )
    }
}

@Composable
fun LocationField(
    label: String,
    value: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val isPlaceholder = value.contains("Select")
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label, 
            color = if (enabled) PremiumGold else SecondaryText, 
            fontSize = 13.sp, 
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        Surface(
            onClick = onClick,
            enabled = enabled,
            color = if (enabled) PremiumNavyLight else PremiumNavyLight.copy(alpha = 0.4f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                width = 1.dp,
                color = if (enabled) PremiumGold.copy(alpha = 0.5f) else PremiumGold.copy(alpha = 0.2f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = value,
                    color = if (isPlaceholder) PremiumWhite.copy(alpha = 0.5f) else PremiumWhite,
                    fontSize = 15.sp,
                    fontWeight = if (isPlaceholder) FontWeight.Normal else FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown, 
                    contentDescription = null, 
                    tint = PremiumGold,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchableListDialog(
    title: String,
    list: List<String>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredList = remember(searchQuery, list) {
        if (searchQuery.isEmpty()) list
        else list.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.75f),
            color = PremiumNavyDeep,
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.2.dp, PremiumGold.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                // Header
                Text(
                    text = title,
                    color = PremiumGold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search location...", color = PremiumWhite.copy(alpha = 0.4f)) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = PremiumGold) },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PremiumGold,
                        unfocusedBorderColor = PremiumGold.copy(alpha = 0.3f),
                        focusedTextColor = PremiumWhite,
                        unfocusedTextColor = PremiumWhite,
                        cursorColor = PremiumGold,
                        focusedContainerColor = PremiumNavyLight.copy(alpha = 0.5f),
                        unfocusedContainerColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // List
                Box(modifier = Modifier.weight(1f)) {
                    if (filteredList.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No matching locations found", color = PremiumWhite.copy(alpha = 0.4f), fontSize = 14.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(filteredList) { item ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelect(item) },
                                    color = Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 14.dp, horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item,
                                            color = PremiumWhite,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                HorizontalDivider(color = PremiumWhite.copy(alpha = 0.1f), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}
