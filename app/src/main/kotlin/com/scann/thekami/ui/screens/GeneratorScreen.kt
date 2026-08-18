package com.scann.thekami.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.scann.thekami.model.QrContentEncoder
import com.scann.thekami.model.QrContentType
import com.scann.thekami.util.QrCodeGenerator
import java.io.File
import java.io.FileOutputStream
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedType by remember { mutableStateOf(QrContentType.TEXT) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var encodedContent by remember { mutableStateOf("") }

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

    LaunchedEffect(
        selectedType, textValue, urlValue, wifiSsid, wifiPassword, wifiSecurity, wifiHidden,
        contactName, contactPhone, contactEmail, contactOrg, phoneValue,
        emailAddress, emailSubject, emailBody, smsNumber, smsMessage
    ) {
        encodedContent = when (selectedType) {
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

        qrBitmap = if (encodedContent.isNotBlank()) {
            QrCodeGenerator.generate(encodedContent)
        } else {
            null
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    fun saveQrToGallery() {
        val bitmap = qrBitmap ?: return
        try {
            val filename = "Scann_QR_${System.currentTimeMillis()}.png"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Scann")
                }
                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("QR code saved to Pictures/Scann")
                    }
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Failed to save QR code")
                    }
                }
            } else {
                val picturesDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "Scann"
                )
                picturesDir.mkdirs()
                val file = File(picturesDir, filename)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("QR code saved to Pictures/Scann")
                }
            }
        } catch (e: Exception) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Failed to save QR code: ${e.message}")
            }
        }
    }

    fun shareQr() {
        val bitmap = qrBitmap ?: return
        try {
            val cacheDir = File(context.cacheDir, "qr_share")
            cacheDir.mkdirs()
            val file = File(cacheDir, "scann_qr.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share QR Code"))
        } catch (e: Exception) {
            // Sharing failed silently
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generate QR Code") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Type selector chips
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
                    .padding(horizontal = 20.dp)
            ) {
                // Dynamic form based on selected type
                when (selectedType) {
                    QrContentType.TEXT -> {
                        OutlinedTextField(
                            value = textValue,
                            onValueChange = { textValue = it },
                            label = { Text("Enter your text") },
                            modifier = Modifier.fillMaxWidth(),
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
                            singleLine = true
                        )
                    }
                    QrContentType.WIFI -> {
                        OutlinedTextField(
                            value = wifiSsid,
                            onValueChange = { wifiSsid = it },
                            label = { Text("Network name (SSID)") },
                            modifier = Modifier.fillMaxWidth(),
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
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = contactPhone,
                            onValueChange = { contactPhone = it },
                            label = { Text("Phone number") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = contactEmail,
                            onValueChange = { contactEmail = it },
                            label = { Text("Email (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = contactOrg,
                            onValueChange = { contactOrg = it },
                            label = { Text("Organization (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    QrContentType.PHONE -> {
                        OutlinedTextField(
                            value = phoneValue,
                            onValueChange = { phoneValue = it },
                            label = { Text("Phone number") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    QrContentType.EMAIL -> {
                        OutlinedTextField(
                            value = emailAddress,
                            onValueChange = { emailAddress = it },
                            label = { Text("Email address") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = emailSubject,
                            onValueChange = { emailSubject = it },
                            label = { Text("Subject (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = emailBody,
                            onValueChange = { emailBody = it },
                            label = { Text("Message (optional)") },
                            modifier = Modifier.fillMaxWidth(),
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
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = smsMessage,
                            onValueChange = { smsMessage = it },
                            label = { Text("Message (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap!!.asImageBitmap(),
                                contentDescription = "Generated QR Code",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp)
                            )
                        } else {
                            Text(
                                text = "Fill in the details to generate a QR code",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { saveQrToGallery() },
                        enabled = qrBitmap != null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save")
                    }
                    Button(
                        onClick = { shareQr() },
                        enabled = qrBitmap != null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
