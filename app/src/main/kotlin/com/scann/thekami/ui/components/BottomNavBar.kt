package com.scann.thekami.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
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
    FLOATING(80.dp) // 64dp pill + 16dp bottom margin
}

val LocalNavBarStyle = staticCompositionLocalOf { NavBarStyle.DOCKED }

/** Height of the nav bar area that overlays screen content. */
@Composable
fun bottomNavHeight(): Dp = LocalNavBarStyle.current.overlap

/** Space a screen must leave at its bottom so content clears the nav bar. */
@Composable
fun bottomNavClearance(): Dp = bottomNavHeight() + 16.dp

private val PillHeight = 64.dp
private val PillPadding = 6.dp
private val PillItemHeight = PillHeight - PillPadding * 2

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

/** Compact pill that hugs its content: only the selected tab shows its label. */
@Composable
private fun FloatingNavBar(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier
            .padding(bottom = 16.dp)
            .navigationBarsPadding()
            .height(PillHeight)
    ) {
        Row(
            modifier = Modifier.padding(PillPadding),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTab.entries.forEach { tab ->
                FloatingNavItem(
                    tab = tab,
                    selected = tab == selectedTab,
                    onClick = { onTabSelected(tab) }
                )
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    tab: NavTab,
    selected: Boolean,
    onClick: () -> Unit
) {
    val animations = LocalAnimationsEnabled.current
    val colorSpec = tween<Color>(durationMillis = if (animations) 250 else 0)

    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        animationSpec = colorSpec,
        label = "navItemBackground"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = colorSpec,
        label = "navItemColor"
    )

    Row(
        modifier = Modifier
            .height(PillItemHeight)
            .clip(CircleShape)
            .background(backgroundColor)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = if (selected) null else tab.label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        AnimatedVisibility(
            visible = selected,
            enter = if (animations) fadeIn() + expandHorizontally() else EnterTransition.None,
            exit = if (animations) fadeOut() + shrinkHorizontally() else ExitTransition.None
        ) {
            Text(
                text = tab.label,
                color = contentColor,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
