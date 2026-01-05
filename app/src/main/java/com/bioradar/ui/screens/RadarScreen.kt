package com.bioradar.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bioradar.core.models.DataSource
import com.bioradar.core.models.OperatingMode
import com.bioradar.core.models.RadarTarget
import com.bioradar.core.models.TargetType
import com.bioradar.ui.components.RadarDisplay
import com.bioradar.ui.theme.*
import com.bioradar.ui.viewmodels.RadarViewModel
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * Main Radar Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadarScreen(
    viewModel: RadarViewModel = hiltViewModel()
) {
    val radarState by viewModel.radarState.collectAsState()
    val targets by viewModel.targets.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with mode and battery
        RadarHeader(
            mode = currentMode,
            batteryLevel = radarState.batteryLevel,
            isScanning = isScanning,
            onModeChange = { viewModel.setMode(it) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Main Radar Display
        Box(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            RadarDisplay(
                targets = targets,
                isScanning = isScanning,
                maxRange = radarState.maxRange,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Status Panel
        StatusPanel(
            targetCount = targets.size,
            avgConfidence = targets.map { it.confidence }.average().toFloat().takeIf { !it.isNaN() } ?: 0f,
            activeSensors = radarState.activeSensors,
            maxRange = radarState.maxRange
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Control Buttons
        ControlPanel(
            isScanning = isScanning,
            onStartScan = { viewModel.startScanning() },
            onStopScan = { viewModel.stopScanning() },
            onCalibrate = { viewModel.calibrate() }
        )
    }
}

@Composable
private fun RadarHeader(
    mode: OperatingMode,
    batteryLevel: Int,
    isScanning: Boolean,
    onModeChange: (OperatingMode) -> Unit
) {
    var showModeMenu by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mode Selector
        Box {
            TextButton(
                onClick = { showModeMenu = true },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = RadarGreen
                )
            ) {
                Text(
                    text = mode.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select Mode"
                )
            }
            
            DropdownMenu(
                expanded = showModeMenu,
                onDismissRequest = { showModeMenu = false }
            ) {
                OperatingMode.entries.forEach { operatingMode ->
                    DropdownMenuItem(
                        text = { Text(operatingMode.name) },
                        onClick = {
                            onModeChange(operatingMode)
                            showModeMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = when (operatingMode) {
                                    OperatingMode.NORMAL -> Icons.Default.RadioButtonChecked
                                    OperatingMode.EMERGENCY -> Icons.Default.Warning
                                    OperatingMode.GUARD -> Icons.Default.Security
                                    OperatingMode.STEALTH -> Icons.Default.VisibilityOff
                                    OperatingMode.SEARCH -> Icons.Default.Search
                                    OperatingMode.LAB -> Icons.Default.Science
                                    OperatingMode.SENTRY -> Icons.Default.Shield
                                },
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        }
        
        // Status Indicators
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Scanning Indicator
            if (isScanning) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(RadarGreen)
                )
            }
            
            // Battery
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = when {
                        batteryLevel > 75 -> Icons.Default.BatteryFull
                        batteryLevel > 50 -> Icons.Default.Battery5Bar
                        batteryLevel > 25 -> Icons.Default.Battery3Bar
                        else -> Icons.Default.Battery1Bar
                    },
                    contentDescription = "Battery",
                    tint = when {
                        batteryLevel > 50 -> RadarGreen
                        batteryLevel > 20 -> AlertYellow
                        else -> AlertRed
                    },
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "$batteryLevel%",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun StatusPanel(
    targetCount: Int,
    avgConfidence: Float,
    activeSensors: Set<DataSource>,
    maxRange: Float
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatusItem(
                    label = "TARGETS",
                    value = targetCount.toString(),
                    color = if (targetCount > 0) AlertRed else RadarGreen
                )
                StatusItem(
                    label = "CONFIDENCE",
                    value = "${(avgConfidence * 100).toInt()}%",
                    color = when {
                        avgConfidence > 0.75f -> AlertRed
                        avgConfidence > 0.5f -> AlertYellow
                        else -> RadarGreen
                    }
                )
                StatusItem(
                    label = "RANGE",
                    value = "${maxRange.toInt()}m",
                    color = RadarGreen
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Active Sensors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SENSORS: ",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                activeSensors.forEach { sensor ->
                    SensorIndicator(sensor = sensor)
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }
    }
}

@Composable
private fun StatusItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun SensorIndicator(sensor: DataSource) {
    val color = when (sensor) {
        DataSource.WIFI -> SensorWifi
        DataSource.BLUETOOTH -> SensorBluetooth
        DataSource.SONAR -> SensorSonar
        DataSource.CAMERA -> SensorCamera
        DataSource.UWB -> SensorUwb
        else -> Color.Gray
    }
    
    val icon = when (sensor) {
        DataSource.WIFI -> Icons.Default.Wifi
        DataSource.BLUETOOTH -> Icons.Default.Bluetooth
        DataSource.SONAR -> Icons.Default.GraphicEq
        DataSource.CAMERA -> Icons.Default.CameraAlt
        DataSource.UWB -> Icons.Default.Radar
        else -> Icons.Default.Sensors
    }
    
    Icon(
        imageVector = icon,
        contentDescription = sensor.name,
        tint = color,
        modifier = Modifier.size(16.dp)
    )
}

@Composable
private fun ControlPanel(
    isScanning: Boolean,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onCalibrate: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Scan Button
        Button(
            onClick = if (isScanning) onStopScan else onStartScan,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isScanning) AlertRed else RadarGreen
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = if (isScanning) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isScanning) "STOP" else "SCAN",
                fontWeight = FontWeight.Bold
            )
        }
        
        // Calibrate Button
        OutlinedButton(
            onClick = onCalibrate,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = RadarGreen
            )
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CALIBRATE",
                fontWeight = FontWeight.Bold
            )
        }
    }
}
