package com.scann.thekami.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.scann.thekami.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    fun openUrl(url: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            // No browser available
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SectionLabel("About")
            SettingsCard {
                SettingsRow(
                    icon = Icons.Filled.Info,
                    title = "Scann",
                    subtitle = "Version ${BuildConfig.VERSION_NAME}",
                    onClick = null
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Filled.PrivacyTip,
                    title = "Privacy",
                    subtitle = "No account, no data collection, works offline",
                    onClick = null
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            SectionLabel("Connect")
            SettingsCard {
                SettingsRow(
                    icon = Icons.Filled.Language,
                    title = "Website",
                    subtitle = "thekami.tech",
                    onClick = { openUrl("https://www.thekami.tech/") }
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Filled.Forum,
                    title = "Discord Community",
                    subtitle = "Join for support and updates",
                    onClick = { openUrl("https://www.thekami.tech/discord/") }
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Filled.Code,
                    title = "GitHub",
                    subtitle = "github.com/thekami-dev",
                    onClick = { openUrl("https://github.com/thekami-dev") }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            SectionLabel("More apps by TheKami")
            SettingsCard {
                SettingsRow(
                    icon = Icons.Filled.Apps,
                    title = "Explore our other apps",
                    subtitle = "Flow Browser, Aurora Gallery, KamiAuth",
                    onClick = { openUrl("https://www.thekami.tech/") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Made with care by TheKami",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (onClick != null) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}
