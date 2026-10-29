package com.scann.thekami.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.scann.thekami.R

val UbuntuFontFamily = FontFamily(
    Font(R.font.ubuntu_regular, FontWeight.Normal),
    Font(R.font.ubuntu_medium, FontWeight.Medium),
    Font(R.font.ubuntu_bold, FontWeight.Bold)
)

private fun TextStyle.ubuntu() = copy(fontFamily = UbuntuFontFamily)

private val Default = Typography()

val ScannTypography = Typography(
    displayLarge = Default.displayLarge.ubuntu(),
    displayMedium = Default.displayMedium.ubuntu(),
    displaySmall = Default.displaySmall.ubuntu(),
    headlineLarge = Default.headlineLarge.ubuntu(),
    headlineMedium = Default.headlineMedium.ubuntu(),
    headlineSmall = Default.headlineSmall.ubuntu().copy(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    titleLarge = Default.titleLarge.ubuntu().copy(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    ),
    titleMedium = Default.titleMedium.ubuntu().copy(
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp
    ),
    titleSmall = Default.titleSmall.ubuntu(),
    bodyLarge = Default.bodyLarge.ubuntu(),
    bodyMedium = Default.bodyMedium.ubuntu(),
    bodySmall = Default.bodySmall.ubuntu(),
    labelLarge = Default.labelLarge.ubuntu(),
    labelMedium = Default.labelMedium.ubuntu(),
    labelSmall = Default.labelSmall.ubuntu()
)
