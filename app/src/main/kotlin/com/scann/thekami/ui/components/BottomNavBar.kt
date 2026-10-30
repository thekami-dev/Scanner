package com.scann.thekami.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class NavTab(val label: String, val icon: ImageVector) {
    SCAN("Scan", Icons.Filled.QrCodeScanner),
    CREATE("Create", Icons.Filled.QrCode2),
    SETTINGS("Settings", Icons.Filled.Settings)
}

/**
 * [overlap] is the height of the area (excluding the system navigation inset)
 * that the bar covers at the bottom of the screen.
 */
enum class NavBarStyle(val overlap: Dp) {
    DOCKED(80.dp),
    FLOATING(96.dp) // 72dp pill + 24dp bottom margin
}

val LocalNavBarStyle = staticCompositionLocalOf { NavBarStyle.DOCKED }

/** Height of the nav bar area that overlays screen content. */
@Composable
fun bottomNavHeight(): Dp = LocalNavBarStyle.current.overlap

/** Space a screen must leave at its bottom so content clears the nav bar. */
@Composable
fun bottomNavClearance(): Dp = bottomNavHeight() + 16.dp

// Nested-radius formula: Outer R = Inner R + Padding
private val NavBarPadding = 8.dp
private val NavItemRadius = 20.dp
private val NavBarRadius = NavItemRadius + NavBarPadding // 28.dp

@Composable
fun BottomNavBar(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    style: NavBarStyle,
    modifier: Modifier = Modifier
) {
    when (style) {
        NavBarStyle.DOCKED -> DockedNavBar(selectedTab, onTabSelected, modifier)
        NavBarStyle.FLOATING -> FloatingNavBar(selectedTab, onTabSelected, modifier)
    }
}

@Composable
private fun DockedNavBar(
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

@Composable
private fun FloatingNavBar(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(NavBarRadius),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 6.dp,
        modifier = modifier
            .padding(horizontal = 32.dp)
            .padding(bottom = 24.dp)
            .navigationBarsPadding()
            .height(72.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(NavBarPadding),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTab.entries.forEach { tab ->
                FloatingNavItem(
                    tab = tab,
                    selected = tab == selectedTab,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    tab: NavTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        },
        animationSpec = tween(durationMillis = 250),
        label = "navItemBackground"
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 250),
        label = "navItemColor"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(NavItemRadius))
            .background(backgroundColor)
            .selectable(
                selected = selected,
                role = Role.Tab,
                onClick = onClick
            )
            .semantics { this.selected = selected },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = tab.label,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
