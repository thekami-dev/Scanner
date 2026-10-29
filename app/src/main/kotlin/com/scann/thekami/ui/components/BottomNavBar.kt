package com.scann.thekami.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class NavTab(val label: String, val icon: ImageVector) {
    SCAN("Scan", Icons.Filled.QrCodeScanner),
    CREATE("Create", Icons.Filled.QrCode2),
    SETTINGS("Settings", Icons.Filled.Settings)
}

// Space a screen must leave at its bottom so content clears the nav bar:
// NavigationBar height (80dp) + 16dp gap. The system-bar inset is handled separately.
val BottomNavHeight = 80.dp
val BottomNavClearance = BottomNavHeight + 16.dp

@Composable
fun BottomNavBar(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            NavTab.entries.forEach { tab ->
                NavigationBarItem(
                    selected = tab == selectedTab,
                    onClick = { onTabSelected(tab) },
                    icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                    label = { Text(tab.label) }
                )
            }
        }
    }
}
