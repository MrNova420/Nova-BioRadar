package com.bioradar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bioradar.core.models.*
import com.bioradar.ui.theme.*
import com.bioradar.ui.viewmodels.GuardViewModel

/**
 * Guard Mode Screen for perimeter monitoring
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuardScreen(
    viewModel: GuardViewModel = hiltViewModel()
) {
    val zones by viewModel.zones.collectAsState()
    val isGuarding by viewModel.isGuarding.collectAsState()
    val isCalibrating by viewModel.isCalibrating.collectAsState()
    var showAddZoneDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "PERIMETER GUARD",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = RadarGreen
        )
        
        Text(
            text = "Monitor zones for presence detection",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Guard Status Card
        GuardStatusCard(
            isGuarding = isGuarding,
            isCalibrating = isCalibrating,
            zoneCount = zones.size,
            onToggleGuard = { 
                if (isGuarding) viewModel.stopGuarding() 
                else viewModel.startGuarding() 
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Zone List
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Zones (${zones.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            IconButton(onClick = { showAddZoneDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Zone",
                    tint = RadarGreen
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (zones.isEmpty()) {
            EmptyZonesPlaceholder()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(zones) { zone ->
                    ZoneCard(
                        zone = zone,
                        status = viewModel.getZoneStatus(zone.id),
                        onCalibrate = { viewModel.calibrateZone(zone.id) },
                        onDelete = { viewModel.deleteZone(zone.id) }
                    )
                }
            }
        }
    }
    
    // Add Zone Dialog
    if (showAddZoneDialog) {
        AddZoneDialog(
            onDismiss = { showAddZoneDialog = false },
            onAddZone = { name, sector, sensitivity, alertType ->
                viewModel.addZone(name, sector, sensitivity, alertType)
                showAddZoneDialog = false
            }
        )
    }
}

@Composable
private fun GuardStatusCard(
    isGuarding: Boolean,
    isCalibrating: Boolean,
    zoneCount: Int,
    onToggleGuard: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGuarding) RadarGreen.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isGuarding) RadarGreen.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (isGuarding) RadarGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Column {
                    Text(
                        text = when {
                            isCalibrating -> "CALIBRATING..."
                            isGuarding -> "GUARDING"
                            else -> "STANDBY"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isGuarding) RadarGreen else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$zoneCount zone(s) configured",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Button(
                onClick = onToggleGuard,
                enabled = zoneCount > 0 && !isCalibrating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGuarding) AlertRed else RadarGreen
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isGuarding) "STOP" else "START",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ZoneCard(
    zone: PerimeterZone,
    status: ZoneStatus,
    onCalibrate: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (status) {
        ZoneStatus.GREEN_CLEAR -> ZoneClear
        ZoneStatus.YELLOW_POSSIBLE -> ZonePossible
        ZoneStatus.RED_PRESENCE -> ZonePresence
        ZoneStatus.UNKNOWN -> ZoneUnknown
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Status indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                
                Column {
                    Text(
                        text = zone.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${zone.monitoringSector.name} • ${zone.sensitivity.name}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Row {
                IconButton(onClick = onCalibrate) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Calibrate",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = AlertRed.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyZonesPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AddLocation,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No zones configured",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "Tap + to add a monitoring zone",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddZoneDialog(
    onDismiss: () -> Unit,
    onAddZone: (String, Sector, Sensitivity, AlertType) -> Unit
) {
    var zoneName by remember { mutableStateOf("") }
    var selectedSector by remember { mutableStateOf(Sector.FORWARD_CONE) }
    var selectedSensitivity by remember { mutableStateOf(Sensitivity.MEDIUM) }
    var selectedAlertType by remember { mutableStateOf(AlertType.SOUND_AND_VIBRATION) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Zone") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = zoneName,
                    onValueChange = { zoneName = it },
                    label = { Text("Zone Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text("Monitoring Sector", fontWeight = FontWeight.SemiBold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(Sector.FORWARD_CONE, Sector.FRONT_WIDE, Sector.FULL_360).forEach { sector ->
                        FilterChip(
                            selected = selectedSector == sector,
                            onClick = { selectedSector = sector },
                            label = { 
                                Text(
                                    when (sector) {
                                        Sector.FORWARD_CONE -> "90°"
                                        Sector.FRONT_WIDE -> "180°"
                                        Sector.FULL_360 -> "360°"
                                        else -> sector.name
                                    },
                                    fontSize = 12.sp
                                ) 
                            }
                        )
                    }
                }
                
                Text("Sensitivity", fontWeight = FontWeight.SemiBold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Sensitivity.entries.filter { it != Sensitivity.CUSTOM }.forEach { sens ->
                        FilterChip(
                            selected = selectedSensitivity == sens,
                            onClick = { selectedSensitivity = sens },
                            label = { Text(sens.name, fontSize = 12.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAddZone(zoneName, selectedSector, selectedSensitivity, selectedAlertType) },
                enabled = zoneName.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
