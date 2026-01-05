package com.bioradar.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bioradar.modes.UltimateMode
import com.bioradar.modes.BlackoutMode
import kotlinx.coroutines.launch

/**
 * Advanced Modes Configuration Screen
 * Provides UI for Ultimate Mode and Blackout Mode
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedModesScreen(
    viewModel: com.bioradar.ui.viewmodels.AdvancedModesViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // State from ViewModel
    val ultimateModeConfig by viewModel.ultimateModeConfiguration.collectAsState()
    val ultimateModeActive by viewModel.ultimateModeActive.collectAsState()
    val blackoutModeStatus by viewModel.blackoutModeStatus.collectAsState()
    val selectedBlackoutProfile by viewModel.selectedBlackoutProfile.collectAsState()
    val showUltimateDetails by viewModel.showUltimateDetails.collectAsState()
    val showBlackoutDetails by viewModel.showBlackoutDetails.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Advanced Modes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Ultimate Mode Section
            UltimateModeCard(
                isActive = ultimateModeActive,
                config = ultimateModeConfig,
                showDetails = showUltimateDetails,
                onToggleDetails = { viewModel.toggleUltimateDetails() },
                onActivate = { viewModel.activateUltimateMode() },
                onDeactivate = { viewModel.deactivateUltimateMode() }
            )
            
            Divider()
            
            // Blackout Mode Section
            BlackoutModeCard(
                status = blackoutModeStatus,
                selectedProfile = selectedBlackoutProfile,
                showDetails = showBlackoutDetails,
                onToggleDetails = { viewModel.toggleBlackoutDetails() },
                onProfileChange = { viewModel.setBlackoutProfile(it) },
                onActivate = { viewModel.activateBlackoutMode() },
                onDeactivate = { viewModel.deactivateBlackoutMode() }
            )
        }
    }
}

@Composable
private fun UltimateModeCard(
    isActive: Boolean,
    config: UltimateMode.UltimateConfiguration?,
    showDetails: Boolean,
    onToggleDetails: () -> Unit,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = null,
                        tint = if (isActive) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Ultimate Mode",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (isActive) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text("ACTIVE")
                    }
                }
            }
            
            Text(
                text = "One-button auto-optimization. Automatically detects and maximizes ALL device capabilities.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Metrics
            if (config != null && isActive) {
                MetricsGrid(config)
            }
            
            // Details toggle
            TextButton(
                onClick = onToggleDetails,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(if (showDetails) "Hide Details" else "Show Details")
                Icon(
                    imageVector = if (showDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
            
            // Expandable details
            AnimatedVisibility(visible = showDetails && config != null) {
                config?.let { DetailedMetrics(it) }
            }
            
            // Action button
            Button(
                onClick = if (isActive) onDeactivate else onActivate,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (isActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isActive) "Deactivate" else "Activate Ultimate Mode")
            }
        }
    }
}

@Composable
private fun MetricsGrid(config: UltimateMode.UltimateConfiguration) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricChip(
            label = "Sensors",
            value = "${config.metrics.totalSensors}",
            icon = Icons.Default.Sensors,
            modifier = Modifier.weight(1f)
        )
        MetricChip(
            label = "Methods",
            value = "${config.metrics.detectionMethods}",
            icon = Icons.Default.Functions,
            modifier = Modifier.weight(1f)
        )
        MetricChip(
            label = "Range",
            value = "${config.metrics.estimatedRange.toInt()}m",
            icon = Icons.Default.MyLocation,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricChip(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DetailedMetrics(config: UltimateMode.UltimateConfiguration) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Device Tier: ${config.deviceTier.name.replace("_", " ")}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Update Rate: ${config.metrics.updateRateHz} Hz",
            style = MaterialTheme.typography.bodySmall
        )
        
        Text(
            text = "Confidence: ${(config.metrics.expectedConfidence * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall
        )
        
        Text(
            text = "Battery Life: ~${config.metrics.batteryLifeHours.toInt()} hours",
            style = MaterialTheme.typography.bodySmall
        )
        
        if (config.recommendations.isNotEmpty()) {
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            Text(
                text = "Recommendations:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            config.recommendations.forEach { recommendation ->
                Text(
                    text = "• $recommendation",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun BlackoutModeCard(
    status: BlackoutMode.BlackoutStatus,
    selectedProfile: BlackoutMode.BlackoutProfile,
    showDetails: Boolean,
    onToggleDetails: () -> Unit,
    onProfileChange: (BlackoutMode.BlackoutProfile) -> Unit,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit
) {
    val isActive = status is BlackoutMode.BlackoutStatus.Active
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) 
                MaterialTheme.colorScheme.tertiaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerOff,
                        contentDescription = null,
                        tint = if (isActive) 
                            MaterialTheme.colorScheme.tertiary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Blackout Mode",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (isActive) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    ) {
                        Text("OFF-GRID")
                    }
                }
            }
            
            Text(
                text = "Complete infrastructure independence. Works with ZERO external dependencies.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Profile selector
            Text(
                text = "Profile:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            
            BlackoutProfileSelector(
                selectedProfile = selectedProfile,
                onProfileChange = onProfileChange
            )
            
            // Details toggle
            TextButton(
                onClick = onToggleDetails,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(if (showDetails) "Hide Details" else "Show Details")
                Icon(
                    imageVector = if (showDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
            
            // Expandable profile details
            AnimatedVisibility(visible = showDetails) {
                BlackoutProfileDetails(selectedProfile)
            }
            
            // Action button
            Button(
                onClick = if (isActive) onDeactivate else onActivate,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.tertiary
                )
            ) {
                Icon(
                    imageVector = if (isActive) Icons.Default.Stop else Icons.Default.PowerOff,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isActive) "Deactivate" else "Activate Blackout Mode")
            }
        }
    }
}

@Composable
private fun BlackoutProfileSelector(
    selectedProfile: BlackoutMode.BlackoutProfile,
    onProfileChange: (BlackoutMode.BlackoutProfile) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BlackoutMode.BlackoutProfile.values().forEach { profile ->
            FilterChip(
                selected = profile == selectedProfile,
                onClick = { onProfileChange(profile) },
                label = {
                    Text(
                        text = profile.name.replace("_", " "),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = getProfileIcon(profile),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun BlackoutProfileDetails(profile: BlackoutMode.BlackoutProfile) {
    val (range, battery, description) = when (profile) {
        BlackoutMode.BlackoutProfile.MAXIMUM_RANGE -> 
            Triple("200m+", "6-8hrs", "Maximum detection range with self-generated WiFi. Best for perimeter defense.")
        BlackoutMode.BlackoutProfile.BALANCED -> 
            Triple("100m", "12-16hrs", "Balanced detection and battery life. WiFi Direct for coordination.")
        BlackoutMode.BlackoutProfile.MAXIMUM_ENDURANCE -> 
            Triple("50m", "24+hrs", "Extended battery life mode. Minimal WiFi use. Perfect for long-term deployment.")
        BlackoutMode.BlackoutProfile.STEALTH -> 
            Triple("30m", "18-24hrs", "Silent operation with no RF emissions. Invisible to RF detectors.")
        BlackoutMode.BlackoutProfile.MESH_HUB -> 
            Triple("200m+", "8-10hrs", "Multi-device mesh coordinator. Acts as command center.")
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Range",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = range,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(
                        text = "Battery",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = battery,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Divider()
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getProfileIcon(profile: BlackoutMode.BlackoutProfile): ImageVector {
    return when (profile) {
        BlackoutMode.BlackoutProfile.MAXIMUM_RANGE -> Icons.Default.ZoomOutMap
        BlackoutMode.BlackoutProfile.BALANCED -> Icons.Default.Balance
        BlackoutMode.BlackoutProfile.MAXIMUM_ENDURANCE -> Icons.Default.BatteryFull
        BlackoutMode.BlackoutProfile.STEALTH -> Icons.Default.VisibilityOff
        BlackoutMode.BlackoutProfile.MESH_HUB -> Icons.Default.Hub
    }
}
