package com.bioradar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bioradar.ui.theme.*
import com.bioradar.ui.viewmodels.SettingsViewModel

/**
 * Settings Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    var showPanicWipeDialog by remember { mutableStateOf(false) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "SETTINGS",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = RadarGreen
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Radar Settings
        item {
            SettingsSection(title = "Radar") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SliderSetting(
                        title = "Max Range",
                        value = settings.maxRange,
                        valueRange = 5f..50f,
                        valueLabel = "${settings.maxRange.toInt()}m",
                        onValueChange = { viewModel.setMaxRange(it) }
                    )
                    
                    SliderSetting(
                        title = "Sweep Speed",
                        value = settings.sweepSpeed,
                        valueRange = 1f..5f,
                        valueLabel = "${settings.sweepSpeed.toInt()}x",
                        onValueChange = { viewModel.setSweepSpeed(it) }
                    )
                    
                    SwitchSetting(
                        title = "Grid Lines",
                        subtitle = "Show distance rings and angle lines",
                        checked = settings.showGridLines,
                        onCheckedChange = { viewModel.setShowGridLines(it) }
                    )
                }
            }
        }
        
        // Sensor Settings
        item {
            SettingsSection(title = "Sensors") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SwitchSetting(
                        title = "WiFi Scanning",
                        subtitle = "Detect presence via WiFi signal analysis",
                        checked = settings.wifiEnabled,
                        onCheckedChange = { viewModel.setWifiEnabled(it) }
                    )
                    
                    SwitchSetting(
                        title = "Bluetooth Scanning",
                        subtitle = "Scan for nearby Bluetooth devices",
                        checked = settings.bluetoothEnabled,
                        onCheckedChange = { viewModel.setBluetoothEnabled(it) }
                    )
                    
                    SwitchSetting(
                        title = "Audio Sonar",
                        subtitle = "Emit ultrasonic pings for echo detection",
                        checked = settings.sonarEnabled,
                        onCheckedChange = { viewModel.setSonarEnabled(it) }
                    )
                    
                    if (settings.sonarEnabled) {
                        SliderSetting(
                            title = "Sonar Frequency",
                            value = settings.sonarFrequency,
                            valueRange = 16000f..22000f,
                            valueLabel = "${(settings.sonarFrequency / 1000).toInt()}kHz",
                            onValueChange = { viewModel.setSonarFrequency(it) }
                        )
                    }
                    
                    SwitchSetting(
                        title = "Camera Motion",
                        subtitle = "Use camera for optical flow detection",
                        checked = settings.cameraEnabled,
                        onCheckedChange = { viewModel.setCameraEnabled(it) }
                    )
                }
            }
        }
        
        // Alert Settings
        item {
            SettingsSection(title = "Alerts") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SwitchSetting(
                        title = "Sound Alerts",
                        subtitle = "Play sound when presence detected",
                        checked = settings.soundAlertsEnabled,
                        onCheckedChange = { viewModel.setSoundAlertsEnabled(it) }
                    )
                    
                    SwitchSetting(
                        title = "Vibration",
                        subtitle = "Vibrate on detection",
                        checked = settings.vibrationEnabled,
                        onCheckedChange = { viewModel.setVibrationEnabled(it) }
                    )
                    
                    SwitchSetting(
                        title = "Screen Flash",
                        subtitle = "Flash screen on high-confidence detection",
                        checked = settings.screenFlashEnabled,
                        onCheckedChange = { viewModel.setScreenFlashEnabled(it) }
                    )
                }
            }
        }
        
        // Battery Settings
        item {
            SettingsSection(title = "Battery") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SwitchSetting(
                        title = "Auto-Downgrade",
                        subtitle = "Switch to Emergency mode at low battery",
                        checked = settings.autoDowngrade,
                        onCheckedChange = { viewModel.setAutoDowngrade(it) }
                    )
                    
                    if (settings.autoDowngrade) {
                        SliderSetting(
                            title = "Downgrade Threshold",
                            value = settings.downgradeThreshold.toFloat(),
                            valueRange = 5f..30f,
                            valueLabel = "${settings.downgradeThreshold}%",
                            onValueChange = { viewModel.setDowngradeThreshold(it.toInt()) }
                        )
                    }
                }
            }
        }
        
        // Security Settings
        item {
            SettingsSection(title = "Security") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SwitchSetting(
                        title = "Encrypt Logs",
                        subtitle = "Use AES-256 encryption for detection logs",
                        checked = settings.encryptLogs,
                        onCheckedChange = { viewModel.setEncryptLogs(it) }
                    )
                    
                    SwitchSetting(
                        title = "Enable Panic Wipe",
                        subtitle = "Allow quick data destruction",
                        checked = settings.panicWipeEnabled,
                        onCheckedChange = { viewModel.setPanicWipeEnabled(it) }
                    )
                    
                    if (settings.panicWipeEnabled) {
                        Button(
                            onClick = { showPanicWipeDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AlertRed
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PANIC WIPE")
                        }
                    }
                }
            }
        }
        
        // About
        item {
            SettingsSection(title = "About") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "BioRadar v1.0.0",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Life-Form Detection Radar for Android",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Detect the invisible. Protect what matters.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = RadarGreen
                    )
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Panic Wipe Confirmation Dialog
    if (showPanicWipeDialog) {
        AlertDialog(
            onDismissRequest = { showPanicWipeDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = AlertRed,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("Confirm Panic Wipe") },
            text = {
                Text("This will permanently delete all logs, calibration data, and settings. This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.executePanicWipe()
                        showPanicWipeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlertRed
                    )
                ) {
                    Text("WIPE NOW")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPanicWipeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = RadarGreen,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
private fun SwitchSetting(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = RadarGreen,
                checkedTrackColor = RadarGreen.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
private fun SliderSetting(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueLabel: String,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = valueLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = RadarGreen
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = RadarGreen,
                activeTrackColor = RadarGreen,
                inactiveTrackColor = RadarGreen.copy(alpha = 0.3f)
            )
        )
    }
}
