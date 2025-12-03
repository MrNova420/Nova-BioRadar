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
import com.bioradar.ui.viewmodels.MeshViewModel

/**
 * Mesh Network Screen for multi-device coordination
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeshScreen(
    viewModel: MeshViewModel = hiltViewModel()
) {
    val networkStatus by viewModel.networkStatus.collectAsState()
    val connectedNodes by viewModel.connectedNodes.collectAsState()
    val isConnecting by viewModel.isConnecting.collectAsState()
    var showCreateNetworkDialog by remember { mutableStateOf(false) }
    var showJoinNetworkDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "MESH NETWORK",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = RadarGreen
        )
        
        Text(
            text = "Connect multiple devices for distributed detection",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Network Status Card
        NetworkStatusCard(
            role = networkStatus.role,
            nodeCount = connectedNodes.size,
            isHealthy = networkStatus.isHealthy,
            isConnecting = isConnecting
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Action Buttons
        when (networkStatus.role) {
            MeshRole.STANDALONE -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showCreateNetworkDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RadarGreen
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiTethering,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CREATE HUB")
                    }
                    
                    OutlinedButton(
                        onClick = { showJoinNetworkDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = RadarGreen
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiFind,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("JOIN")
                    }
                }
            }
            MeshRole.HUB, MeshRole.NODE -> {
                Button(
                    onClick = { viewModel.disconnect() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlertRed
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LinkOff,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("DISCONNECT")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Connected Nodes
        if (connectedNodes.isNotEmpty()) {
            Text(
                text = "Connected Nodes (${connectedNodes.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(connectedNodes) { node ->
                    NodeCard(node = node)
                }
            }
        } else if (networkStatus.role != MeshRole.STANDALONE) {
            WaitingForNodesPlaceholder()
        } else {
            NotConnectedPlaceholder()
        }
    }
    
    // Create Network Dialog
    if (showCreateNetworkDialog) {
        CreateNetworkDialog(
            onDismiss = { showCreateNetworkDialog = false },
            onCreate = { name ->
                viewModel.createNetwork(name)
                showCreateNetworkDialog = false
            }
        )
    }
    
    // Join Network Dialog
    if (showJoinNetworkDialog) {
        JoinNetworkDialog(
            onDismiss = { showJoinNetworkDialog = false },
            onJoin = { hubAddress, nodeName ->
                viewModel.joinNetwork(hubAddress, nodeName)
                showJoinNetworkDialog = false
            }
        )
    }
}

@Composable
private fun NetworkStatusCard(
    role: MeshRole,
    nodeCount: Int,
    isHealthy: Boolean,
    isConnecting: Boolean
) {
    val statusColor = when {
        isConnecting -> AlertYellow
        isHealthy && role != MeshRole.STANDALONE -> RadarGreen
        role == MeshRole.STANDALONE -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        else -> AlertRed
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (role) {
                MeshRole.HUB -> RadarGreen.copy(alpha = 0.1f)
                MeshRole.NODE -> TargetUwb.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }
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
                        .background(statusColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (role) {
                            MeshRole.HUB -> Icons.Default.Hub
                            MeshRole.NODE -> Icons.Default.DeviceHub
                            MeshRole.STANDALONE -> Icons.Default.WifiOff
                        },
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Column {
                    Text(
                        text = when {
                            isConnecting -> "CONNECTING..."
                            role == MeshRole.HUB -> "HUB ACTIVE"
                            role == MeshRole.NODE -> "CONNECTED"
                            else -> "NOT CONNECTED"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Text(
                        text = when (role) {
                            MeshRole.HUB -> "$nodeCount node(s) connected"
                            MeshRole.NODE -> "Operating as remote node"
                            MeshRole.STANDALONE -> "Join or create a network"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            if (isConnecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = statusColor,
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

@Composable
private fun NodeCard(node: MeshNode) {
    val statusColor = when (node.lastStatus) {
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
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if (node.isOnline) statusColor else Color.Gray)
                )
                
                Column {
                    Text(
                        text = node.zoneName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${node.connectionType.name} • ${node.batteryLevel ?: "?"}%",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${(node.lastConfidence * 100).toInt()}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                Text(
                    text = node.lastStatus.name.replace("_", " "),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun NotConnectedPlaceholder() {
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
                imageVector = Icons.Default.Wifi,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No active mesh network",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "Create a hub or join an existing network",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun WaitingForNodesPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = RadarGreen
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Waiting for nodes to connect...",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateNetworkDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var networkName by remember { mutableStateOf("BioRadar-Hub") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Mesh Network") },
        text = {
            Column {
                Text(
                    text = "This device will become the hub. Other devices can join to form a distributed sensor network.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = networkName,
                    onValueChange = { networkName = it },
                    label = { Text("Network Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(networkName) },
                enabled = networkName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JoinNetworkDialog(
    onDismiss: () -> Unit,
    onJoin: (String, String) -> Unit
) {
    var hubAddress by remember { mutableStateOf("") }
    var nodeName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Join Mesh Network") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Enter the hub address and give this node a name.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                OutlinedTextField(
                    value = hubAddress,
                    onValueChange = { hubAddress = it },
                    label = { Text("Hub Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nodeName,
                    onValueChange = { nodeName = it },
                    label = { Text("Node Name (e.g., NORTH GATE)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onJoin(hubAddress, nodeName) },
                enabled = hubAddress.isNotBlank() && nodeName.isNotBlank()
            ) {
                Text("Join")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
