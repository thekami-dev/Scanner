package com.scann.thekami.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.android.play.core.review.ReviewManagerFactory
import com.scann.thekami.BuildConfig
import com.scann.thekami.R
import com.scann.thekami.ui.components.BottomNavClearance
import com.scann.thekami.util.NotificationController

private val CardRowPadding = 16.dp
private val CardRadius = 16.dp
private val IconTileRadius = 10.dp
private val IconTileSize = 38.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onAboutClick: () -> Unit
) {
    val context = LocalContext.current
    var createNotificationEnabled by remember {
        mutableStateOf(NotificationController.isCreateNotificationEnabled(context))
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            createNotificationEnabled = true
            NotificationController.setCreateNotificationEnabled(context, true)
        }
    }

    fun openUrl(url: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            // No browser available
        }
    }

    fun openPlayStoreListing() {
        openUrl("https://play.google.com/store/apps/details?id=${context.packageName}")
    }

    fun launchInAppReview() {
        try {
            val activity = context as? Activity
            if (activity == null) {
                openPlayStoreListing()
                return
            }
            val reviewManager = ReviewManagerFactory.create(context)
            val request = reviewManager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    reviewManager.launchReviewFlow(activity, task.result)
                } else {
                    openPlayStoreListing()
                }
            }
        } catch (e: Exception) {
            openPlayStoreListing()
        }
    }

    fun onNotificationToggle(checked: Boolean) {
        val needsPermission = checked &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED

        if (needsPermission) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            createNotificationEnabled = checked
            NotificationController.setCreateNotificationEnabled(context, checked)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = BottomNavClearance)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            SectionLabel("Quick access")
            SettingsCard {
                NotificationSettingRow(
                    enabled = createNotificationEnabled,
                    onCheckedChange = ::onNotificationToggle
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionLabel("About")
            SettingsCard {
                SettingsRow(
                    icon = Icons.Filled.Info,
                    title = "About Scann",
                    subtitle = "Version ${BuildConfig.VERSION_NAME}",
                    onClick = onAboutClick
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Filled.StarRate,
                    title = "Rate Scann",
                    subtitle = "Enjoying the app? Leave us a rating",
                    onClick = ::launchInAppReview
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Filled.PrivacyTip,
                    title = "Privacy Policy",
                    subtitle = "How we handle your data",
                    onClick = { openUrl("https://blog.thekami.tech/posts/scann/") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionLabel("Follow us")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                SocialIconButton(R.drawable.ic_linkedin, "LinkedIn") {
                    openUrl("https://www.linkedin.com/company/thekamiofficial")
                }
                SocialIconButton(R.drawable.ic_discord, "Discord") {
                    openUrl("https://www.thekami.tech/discord/")
                }
                SocialIconButton(R.drawable.ic_github, "GitHub") {
                    openUrl("https://github.com/thekami-dev")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionLabel("More from TheKami")
            SettingsCard {
                SettingsRow(
                    icon = Icons.Filled.Apps,
                    title = "More apps by TheKami",
                    subtitle = "Explore our other apps on Play Store",
                    onClick = {
                        openUrl("https://play.google.com/store/apps/dev?id=8580703469843686933")
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .clickable(role = Role.Button) { openUrl("https://www.thekami.tech") }
            ) {
                Text(
                    text = "Made by",
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                AsyncImage(
                    model = "https://www.thekami.tech/logo.png",
                    contentDescription = "TheKami",
                    modifier = Modifier.height(15.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(
            letterSpacing = 0.8.sp,
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun settingsSurfaceColor() =
    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(CardRadius),
        color = settingsSurfaceColor(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(content = content)
    }
}

@Composable
private fun IconTile(icon: ImageVector) {
    Surface(
        shape = RoundedCornerShape(IconTileRadius),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(IconTileSize)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(CardRowPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(icon)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = CardRowPadding),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

@Composable
private fun NotificationSettingRow(
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange
            )
            .padding(CardRowPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(Icons.Filled.NotificationsActive)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Create QR shortcut", style = MaterialTheme.typography.bodyLarge)
            Text(
                "Adds a quick shortcut to create a QR code from your notifications",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(checked = enabled, onCheckedChange = null)
    }
}

@Composable
private fun SocialIconButton(iconRes: Int, label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = settingsSurfaceColor(),
        modifier = Modifier.size(52.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
