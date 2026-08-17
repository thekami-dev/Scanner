package com.scann.thekami.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.scann.thekami.model.ScanResult
import com.scann.thekami.model.ScanResultType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    scanResult: ScanResult,
    onBack: () -> Unit,
    onAction: (ScanAction) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Result") },
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            ResultTypeLabel(scanResult)

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ResultContent(scanResult.type)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            ResultActions(scanResult, onAction)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Format: ${scanResult.format}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ResultTypeLabel(scanResult: ScanResult) {
    val label = when (scanResult.type) {
        is ScanResultType.Url -> "Website Link"
        is ScanResultType.PlainText -> "Text"
        is ScanResultType.Phone -> "Phone Number"
        is ScanResultType.Email -> "Email"
        is ScanResultType.Sms -> "SMS Message"
        is ScanResultType.Location -> "Location"
        is ScanResultType.WifiNetwork -> "Wi-Fi Network"
        is ScanResultType.Contact -> "Contact"
        is ScanResultType.CalendarEvent -> "Calendar Event"
        is ScanResultType.Unknown -> "Unknown"
    }
    Text(text = label, style = MaterialTheme.typography.headlineSmall)
}

@Composable
private fun ResultContent(type: ScanResultType) {
    when (type) {
        is ScanResultType.Url -> Text(type.value)
        is ScanResultType.PlainText -> Text(type.value)
        is ScanResultType.Phone -> Text(type.value)
        is ScanResultType.Email -> {
            Text("To: ${type.address}")
            if (!type.subject.isNullOrBlank()) Text("Subject: ${type.subject}")
            if (!type.body.isNullOrBlank()) Text("Body: ${type.body}")
        }
        is ScanResultType.Sms -> {
            Text("To: ${type.phoneNumber}")
            if (!type.message.isNullOrBlank()) Text("Message: ${type.message}")
        }
        is ScanResultType.Location -> Text("Lat: ${type.latitude}, Lng: ${type.longitude}")
        is ScanResultType.WifiNetwork -> {
            Text("Network: ${type.ssid}")
            Text("Security: ${type.encryptionType ?: "Unknown"}")
        }
        is ScanResultType.Contact -> {
            type.name?.let { Text("Name: $it") }
            type.phone?.let { Text("Phone: $it") }
            type.email?.let { Text("Email: $it") }
            type.organization?.let { Text("Organization: $it") }
        }
        is ScanResultType.CalendarEvent -> {
            type.title?.let { Text("Title: $it") }
            type.location?.let { Text("Location: $it") }
            type.description?.let { Text("Description: $it") }
        }
        is ScanResultType.Unknown -> Text(type.raw.ifBlank { "No data found" })
    }
}

@Composable
private fun ResultActions(scanResult: ScanResult, onAction: (ScanAction) -> Unit) {
    Column {
        when (val type = scanResult.type) {
            is ScanResultType.Url -> {
                ActionButton("Open Link", Icons.Filled.OpenInBrowser) {
                    onAction(ScanAction.OpenUrl(type.value))
                }
                ActionButton("Copy Link", Icons.Filled.ContentCopy) {
                    onAction(ScanAction.CopyText(type.value))
                }
            }
            is ScanResultType.PlainText -> {
                ActionButton("Copy Text", Icons.Filled.ContentCopy) {
                    onAction(ScanAction.CopyText(type.value))
                }
                ActionButton("Share", Icons.Filled.Share) {
                    onAction(ScanAction.ShareText(type.value))
                }
            }
            is ScanResultType.Phone -> {
                ActionButton("Call", Icons.Filled.Call) {
                    onAction(ScanAction.CallPhone(type.value))
                }
                ActionButton("Copy Number", Icons.Filled.ContentCopy) {
                    onAction(ScanAction.CopyText(type.value))
                }
            }
            is ScanResultType.Email -> {
                ActionButton("Send Email", Icons.Filled.Email) {
                    onAction(ScanAction.SendEmail(type.address, type.subject, type.body))
                }
                ActionButton("Copy Email", Icons.Filled.ContentCopy) {
                    onAction(ScanAction.CopyText(type.address))
                }
            }
            is ScanResultType.Sms -> {
                ActionButton("Send SMS", Icons.Filled.Send) {
                    onAction(ScanAction.SendSms(type.phoneNumber, type.message))
                }
            }
            is ScanResultType.Location -> {
                ActionButton("Open in Maps", Icons.Filled.Map) {
                    onAction(ScanAction.OpenLocation(type.latitude, type.longitude))
                }
            }
            is ScanResultType.WifiNetwork -> {
                ActionButton("Connect to Wi-Fi", Icons.Filled.Wifi) {
                    onAction(ScanAction.ConnectWifi(type.ssid, type.password, type.encryptionType))
                }
                type.password?.let {
                    ActionButton("Copy Password", Icons.Filled.ContentCopy) {
                        onAction(ScanAction.CopyText(it))
                    }
                }
            }
            is ScanResultType.Contact -> {
                ActionButton("Add to Contacts", Icons.Filled.PersonAdd) {
                    onAction(ScanAction.AddContact(type.name, type.phone, type.email, type.organization))
                }
            }
            is ScanResultType.CalendarEvent -> {
                ActionButton("Add to Calendar", Icons.Filled.Event) {
                    onAction(
                        ScanAction.AddCalendarEvent(
                            type.title, type.description, type.location,
                            type.startTimeMillis, type.endTimeMillis
                        )
                    )
                }
            }
            is ScanResultType.Unknown -> {
                ActionButton("Copy Raw Data", Icons.Filled.ContentCopy) {
                    onAction(ScanAction.CopyText(scanResult.rawValue))
                }
            }
        }
    }
}

@Composable
private fun ActionButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}
