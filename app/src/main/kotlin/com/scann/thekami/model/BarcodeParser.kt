package com.scann.thekami.model

import com.google.mlkit.vision.barcode.common.Barcode

object BarcodeParser {

    fun parse(barcode: Barcode): ScanResult {
        val raw = barcode.rawValue ?: ""
        val format = formatName(barcode.format)

        val type: ScanResultType = when (barcode.valueType) {
            Barcode.TYPE_URL -> ScanResultType.Url(
                barcode.url?.url ?: raw
            )

            Barcode.TYPE_PHONE -> ScanResultType.Phone(
                barcode.phone?.number ?: raw
            )

            Barcode.TYPE_EMAIL -> ScanResultType.Email(
                address = barcode.email?.address ?: "",
                subject = barcode.email?.subject,
                body = barcode.email?.body
            )

            Barcode.TYPE_SMS -> ScanResultType.Sms(
                phoneNumber = barcode.sms?.phoneNumber ?: "",
                message = barcode.sms?.message
            )

            Barcode.TYPE_GEO -> ScanResultType.Location(
                latitude = barcode.geoPoint?.lat ?: 0.0,
                longitude = barcode.geoPoint?.lng ?: 0.0
            )

            Barcode.TYPE_WIFI -> ScanResultType.WifiNetwork(
                ssid = barcode.wifi?.ssid ?: "",
                password = barcode.wifi?.password,
                encryptionType = when (barcode.wifi?.encryptionType) {
                    Barcode.WiFi.TYPE_OPEN -> "Open"
                    Barcode.WiFi.TYPE_WPA -> "WPA"
                    Barcode.WiFi.TYPE_WEP -> "WEP"
                    else -> null
                }
            )

            Barcode.TYPE_CONTACT_INFO -> {
                val contact = barcode.contactInfo
                ScanResultType.Contact(
                    name = contact?.name?.formattedName,
                    phone = contact?.phones?.firstOrNull()?.number,
                    email = contact?.emails?.firstOrNull()?.address,
                    organization = contact?.organization
                )
            }

            Barcode.TYPE_CALENDAR_EVENT -> {
                val event = barcode.calendarEvent
                ScanResultType.CalendarEvent(
                    title = event?.summary,
                    description = event?.description,
                    location = event?.location,
                    startTimeMillis = event?.start?.let {
                        try {
                            java.util.Calendar.getInstance().apply {
                                set(it.year, it.month - 1, it.day, it.hours, it.minutes, it.seconds)
                            }.timeInMillis
                        } catch (e: Exception) {
                            null
                        }
                    },
                    endTimeMillis = event?.end?.let {
                        try {
                            java.util.Calendar.getInstance().apply {
                                set(it.year, it.month - 1, it.day, it.hours, it.minutes, it.seconds)
                            }.timeInMillis
                        } catch (e: Exception) {
                            null
                        }
                    }
                )
            }

            Barcode.TYPE_TEXT -> ScanResultType.PlainText(raw)

            else -> {
                if (raw.isNotBlank()) ScanResultType.PlainText(raw)
                else ScanResultType.Unknown(raw)
            }
        }

        return ScanResult(type = type, rawValue = raw, format = format)
    }

    private fun formatName(format: Int): String = when (format) {
        Barcode.FORMAT_QR_CODE -> "QR Code"
        Barcode.FORMAT_CODE_128 -> "Code 128"
        Barcode.FORMAT_CODE_39 -> "Code 39"
        Barcode.FORMAT_CODE_93 -> "Code 93"
        Barcode.FORMAT_CODABAR -> "Codabar"
        Barcode.FORMAT_EAN_13 -> "EAN-13"
        Barcode.FORMAT_EAN_8 -> "EAN-8"
        Barcode.FORMAT_ITF -> "ITF"
        Barcode.FORMAT_UPC_A -> "UPC-A"
        Barcode.FORMAT_UPC_E -> "UPC-E"
        Barcode.FORMAT_PDF417 -> "PDF417"
        Barcode.FORMAT_AZTEC -> "Aztec"
        Barcode.FORMAT_DATA_MATRIX -> "Data Matrix"
        else -> "Unknown"
    }
}
