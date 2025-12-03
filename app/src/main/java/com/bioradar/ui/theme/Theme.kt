package com.bioradar.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Radar Green Colors
val RadarGreen = Color(0xFF00FF00)
val RadarGreenDark = Color(0xFF00CC00)
val RadarGreenLight = Color(0xFF33FF33)
val RadarGreenTransparent = Color(0x8000FF00)

// Background Colors
val BackgroundDark = Color(0xFF0A0A0A)
val BackgroundRadar = Color(0xFF0D1117)
val SurfaceDark = Color(0xFF161B22)

// Target Colors
val TargetHighConfidence = Color(0xFFFF0000)
val TargetMediumConfidence = Color(0xFFFFFF00)
val TargetLowConfidence = Color(0xFF00FF00)
val TargetUnknown = Color(0xFFFFFFFF)
val TargetUwb = Color(0xFF00BFFF)

// Alert Colors
val AlertRed = Color(0xFFFF0000)
val AlertYellow = Color(0xFFFFCC00)
val AlertGreen = Color(0xFF00FF00)
val AlertBlue = Color(0xFF00BFFF)

// Zone Status Colors
val ZoneClear = Color(0xFF00FF00)
val ZonePossible = Color(0xFFFFFF00)
val ZonePresence = Color(0xFFFF0000)
val ZoneUnknown = Color(0xFF808080)

// Grid Colors
val GridLine = Color(0xFF1A3A1A)
val GridLineMajor = Color(0xFF2A5A2A)
val SweepLine = Color(0xFF00FF00)

// Sensor Colors
val SensorWifi = Color(0xFF4FC3F7)
val SensorBluetooth = Color(0xFF2196F3)
val SensorSonar = Color(0xFF9C27B0)
val SensorCamera = Color(0xFFFF9800)
val SensorUwb = Color(0xFF00BCD4)

private val BioRadarColorScheme = darkColorScheme(
    primary = RadarGreen,
    onPrimary = Color.Black,
    primaryContainer = RadarGreenDark,
    onPrimaryContainer = Color.White,
    secondary = RadarGreenLight,
    onSecondary = Color.Black,
    secondaryContainer = SurfaceDark,
    onSecondaryContainer = RadarGreen,
    tertiary = TargetUwb,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF004D66),
    onTertiaryContainer = Color.White,
    error = AlertRed,
    onError = Color.White,
    errorContainer = Color(0xFF660000),
    onErrorContainer = Color.White,
    background = BackgroundDark,
    onBackground = Color.White,
    surface = SurfaceDark,
    onSurface = Color.White,
    surfaceVariant = BackgroundRadar,
    onSurfaceVariant = Color.LightGray,
    outline = GridLineMajor,
    outlineVariant = GridLine
)

@Composable
fun BioRadarTheme(
    darkTheme: Boolean = true, // Always dark for radar aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = BioRadarColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BackgroundDark.toArgb()
            window.navigationBarColor = BackgroundDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
