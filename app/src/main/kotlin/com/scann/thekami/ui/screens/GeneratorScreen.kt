package com.scann.thekami.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.scann.thekami.model.QrContentEncoder
import com.scann.thekami.model.QrContentType
import com.scann.thekami.util.QrCodeGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorScreen() {
    var selectedType by remember { mutableStateOf(QrContentType.TEXT) }
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Form fields
    var textValue by remember { mutableStateOf("") }
    var urlValue by remember { mutableStateOf("") }
    var wifiSsid by remember { mutableStateOf("") }
    var wifiPassword by remember { mutableStateOf("") }
    var wifiSecurity by remember { mutableStateOf("WPA/WPA2") }
    var wifiHidden by remember { mutableStateOf(false) }
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var contactOrg by remember { mutableStateOf("") }
    var phoneValue by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var emailSubject by remember { mutableStateOf("") }
    var emailBody by remember { mutableStateOf("") }
    var smsNumber by remember { mutableStateOf("") }
    var smsMessage by remember { mutableStateOf("") }
    val inputShape = RoundedCornerShape(16.dp)

    fun buildEncodedContent(): String {
        return when (selectedType) {
            QrContentType.TEXT -> QrContentEncoder.encodeText(textValue)
            QrContentType.URL -> if (urlValue.isNotBlank()) QrContentEncoder.encodeUrl(urlValue) else ""
            QrContentType.WIFI -> if (wifiSsid.isNotBlank()) {
                QrContentEncoder.encodeWifi(wifiSsid, wifiPassword, wifiSecurity, wifiHidden)
            } else ""
            QrContentType.CONTACT -> if (contactName.isNotBlank() || contactPhone.isNotBlank()) {
                QrContentEncoder.encodeContact(contactName, contactPhone, contactEmail, contactOrg)
            } else ""
            QrContentType.PHONE -> if (phoneValue.isNotBlank()) QrContentEncoder.encodePhone(phoneValue) else ""
            QrContentType.EMAIL -> if (emailAddress.isNotBlank()) {
                QrContentEncoder.encodeEmail(emailAddress, emailSubject, emailBody)
            } else ""
            QrContentType.SMS -> if (smsNumber.isNotBlank()) {
                QrContentEncoder.encodeSms(smsNumber, smsMessage)
            } else ""
        }
    }

    fun generateQr() {
        val content = buildEncodedContent()
        generatedBitmap = if (content.isNotBlank()) QrCodeGenerator.generate(content) else null
    }

    val currentBitmap = generatedBitmap
    if (currentBitmap != null) {
        QrResultScreen(
            qrBitmap = currentBitmap,
            onBack = { generatedBitmap = null }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Create QR Code") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(QrContentType.values().toList()) { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type.label) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                when (selectedType) {
                    QrContentType.TEXT -> {
                        OutlinedTextField(
                            value = textValue,
                            onValueChange = { textValue = it },
                            label = { Text("Enter your text") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = inputShape,
                            minLines = 2,
                            maxLines = 5
                        )
                    }
                    QrContentType.URL -> {
                        OutlinedTextField(
                            value = urlValue,
                            onValueChange = { urlValue = it },
                            label = { Text("Enter a link (e.g. example.com)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = inputShape,
                            singleLine = true
                        )
                    }
                    QrContentType.WIFI -> {
                        OutlinedTextField(
                            value = wifiSsid,
                            onValueChange = { wifiSsid = it },
                            label = { Text("Network name (SSID)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = inputShape,
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            listOf("WPA/WPA2", "WEP", "None").forEach { option ->
                                FilterChip(
                                    selected = wifiSecurity == option,
                                    onClick = { wifiSecurity = option },
                                    label = { Text(option) },
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        }
                        if (wifiSecurity != "None") {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = wifiPassword,
                                onValueChange = { wifiPassword = it },
                                label = { Text("Password") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = inputShape,
                                singleLine = true
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(checked = wifiHidden, onCheckedChange = { wifiHidden = it })
                            Text("Hidden network")
                        }
                    }
                    QrContentType.CONTACT -> {
                        OutlinedTextField(
                            value = contactName,
                            onValueChange = { contactName = it },
                            label = { Text("Full name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = inputShape,
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = contactPhone,
                            onValueChange = { contactPhone = it },
                            label = { Text("Phone number") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = inputShape,
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = contactEmail,
                            onValueChange = { contactEmail = it },
                            label = { Text("Email (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = inputShape,
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = contactOrg,
                            onValueChange = { contactOrg = it },
                            label = { Text("Organization (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = inputShape,
                            singleLine = true
                        )
                    }
                    QrContentType.PHONE -> {
                        OutlinedTextField(
                            value = phoneValue,
                            onValueChange = { phoneValue = it },
                            label = { Text("Phone number") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = inputShape,
                            singleLine = true
                        )
                    }
                    QrContentType.EMAIL -> {
                        OutlinedTextField(
                            value = emailAddress,
                            onValueChange = { emailAddress = it },
                            label = { Text("Email address") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = inputShape,
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = emailSubject,
                            onValueChange = { emailSubject = it },
                            label = { Text("Subject (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = inputShape,
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = emailBody,
                            onValueChange = { emailBody = it },
                            label = { Text("Message (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = inputShape,
                            minLines = 2,
                            maxLines = 4
                        )
                    }
                    QrContentType.SMS -> {
                        OutlinedTextField(
                            value = smsNumber,
                            onValueChange = { smsNumber = it },
                            label = { Text("Phone number") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = inputShape,
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = smsMessage,
                            onValueChange = { smsMessage = it },
                            label = { Text("Message (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = inputShape,
                            minLines = 2,
                            maxLines = 4
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { generateQr() },
                    shape = RoundedCornerShape(16.dp),
                    enabled = buildEncodedContent().isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(Icons.Filled.QrCode2, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate QR Code")
                }

                Spacer(modifier = Modifier.height(140.dp))
            }
        }
    }
}
