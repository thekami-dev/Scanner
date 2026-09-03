package com.scann.thekami.ui.screens

import android.content.Intent
import android.net.Uri
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.android.play.core.review.ReviewManagerFactory
import com.scann.thekami.BuildConfig
import com.scann.thekami.R
import com.scann.thekami.ads.BannerAdCard
import com.scann.thekami.util.NotificationController
import androidx.core.content.ContextCompat

// Nested-radius system, shared shape language with other screens
private val CardRowPadding = 16.dp
private val CardInnerRadius = 12.dp
private val CardRadius = CardInnerRadius + 4.dp // 16.dp — cards are the outermost element here, no inner shape to nest against, so this stays a fixed constant rather than +padding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onAboutClick: () -> Unit
) {
    val context = LocalContext.current
    var createNotificationEnabled by remember {
        mutableStateOf(NotificationController.isCreateNotificationEnabled(context))
    }
    val notificationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
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
            val activity = context as? android.app.Activity
            if (activity == null) {
                openPlayStoreListing()
                return
            }
            val reviewManager = ReviewManagerFactory.create(context)
            val request = reviewManager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val reviewInfo = task.result
                    reviewManager.launchReviewFlow(activity, reviewInfo)
                } else {
                    openPlayStoreListing()
                }
            }
        } catch (e: Exception) {
            openPlayStoreListing()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings", fontWeight = FontWeight.SemiBold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 140.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            SectionLabel("Quick access")
            SettingsCard {
                NotificationSettingRow(
                    enabled = createNotificationEnabled,
                    onCheckedChange = { checked ->
                        if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            createNotificationEnabled = checked
                            NotificationController.setCreateNotificationEnabled(context, checked)
                        }
                    }
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
                    onClick = { launchInAppReview() }
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
                SocialIconButton(R.drawable.ic_linkedin) {
                    openUrl("https://www.linkedin.com/company/thekamiofficial")
                }
                SocialIconButton(R.drawable.ic_discord) {
                    openUrl("https://www.thekami.tech/discord/")
                }
                SocialIconButton(R.drawable.ic_github) {
                    openUrl("https://github.com/thekami-dev")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionLabel("More from TheKami")
            SettingsCard {
                SettingsRow(
                    icon = null,
                    title = "More apps by TheKami",
                    subtitle = "Explore our other apps on Play Store",
                    onClick = {
                        openUrl("https://play.google.com/store/apps/dev?id=8580703469843686933")
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            BannerAdCard(modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(28.dp))

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .clickable { openUrl("https://www.thekami.tech") }
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
            fontWeight = FontWeight.SemiBold
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(CardRadius),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector?,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(CardRowPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
        }
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
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
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
            .padding(CardRowPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
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
        Switch(checked = enabled, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SocialIconButton(iconRes: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.size(52.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
