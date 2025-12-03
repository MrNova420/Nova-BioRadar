package com.bioradar.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioradar.core.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for the Mesh Network screen
 */
@HiltViewModel
class MeshViewModel @Inject constructor() : ViewModel() {
    
    private val _networkStatus = MutableStateFlow(NetworkStatus(
        role = MeshRole.STANDALONE,
        connectedNodes = emptyList(),
        isHealthy = false
    ))
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()
    
    private val _connectedNodes = MutableStateFlow<List<MeshNode>>(emptyList())
    val connectedNodes: StateFlow<List<MeshNode>> = _connectedNodes.asStateFlow()
    
    private val _isConnecting = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()
    
    /**
     * Create a new mesh network as hub
     */
    fun createNetwork(networkName: String) {
        viewModelScope.launch {
            _isConnecting.value = true
            
            // Simulate network creation
            delay(2000)
            
            _networkStatus.value = NetworkStatus(
                role = MeshRole.HUB,
                connectedNodes = emptyList(),
                isHealthy = true
            )
            
            _isConnecting.value = false
            
            // Start simulating incoming nodes
            startNodeSimulation()
        }
    }
    
    /**
     * Join an existing mesh network as a node
     */
    fun joinNetwork(hubAddress: String, nodeName: String) {
        viewModelScope.launch {
            _isConnecting.value = true
            
            // Simulate connection
            delay(3000)
            
            _networkStatus.value = NetworkStatus(
                role = MeshRole.NODE,
                connectedNodes = emptyList(),
                isHealthy = true
            )
            
            _isConnecting.value = false
        }
    }
    
    /**
     * Disconnect from the mesh network
     */
    fun disconnect() {
        _networkStatus.value = NetworkStatus(
            role = MeshRole.STANDALONE,
            connectedNodes = emptyList(),
            isHealthy = false
        )
        _connectedNodes.value = emptyList()
    }
    
    /**
     * Simulate nodes connecting to the hub
     */
    private fun startNodeSimulation() {
        viewModelScope.launch {
            val simulatedNodes = listOf(
                MeshNode(
                    nodeId = UUID.randomUUID().toString(),
                    nodeName = "Node-1",
                    zoneName = "NORTH GATE",
                    lastStatus = ZoneStatus.GREEN_CLEAR,
                    lastAngle = 45f,
                    lastConfidence = 0.85f,
                    lastUpdate = System.currentTimeMillis(),
                    connectionType = ConnectionType.WIFI_DIRECT,
                    batteryLevel = 78,
                    isOnline = true
                ),
                MeshNode(
                    nodeId = UUID.randomUUID().toString(),
                    nodeName = "Node-2",
                    zoneName = "STAIRS 2F",
                    lastStatus = ZoneStatus.GREEN_CLEAR,
                    lastAngle = 180f,
                    lastConfidence = 0.92f,
                    lastUpdate = System.currentTimeMillis(),
                    connectionType = ConnectionType.WIFI_DIRECT,
                    batteryLevel = 65,
                    isOnline = true
                ),
                MeshNode(
                    nodeId = UUID.randomUUID().toString(),
                    nodeName = "Node-3",
                    zoneName = "BACK DOOR",
                    lastStatus = ZoneStatus.YELLOW_POSSIBLE,
                    lastAngle = 270f,
                    lastConfidence = 0.45f,
                    lastUpdate = System.currentTimeMillis(),
                    connectionType = ConnectionType.BLUETOOTH,
                    batteryLevel = 42,
                    isOnline = true
                )
            )
            
            // Add nodes one by one
            for (node in simulatedNodes) {
                delay(2000)
                if (_networkStatus.value.role == MeshRole.HUB) {
                    _connectedNodes.value = _connectedNodes.value + node
                    _networkStatus.value = _networkStatus.value.copy(
                        connectedNodes = _connectedNodes.value
                    )
                }
            }
            
            // Continuously update node statuses
            while (_networkStatus.value.role == MeshRole.HUB) {
                delay(5000)
                updateNodeStatuses()
            }
        }
    }
    
    /**
     * Update node statuses (simulated)
     */
    private fun updateNodeStatuses() {
        _connectedNodes.value = _connectedNodes.value.map { node ->
            node.copy(
                lastStatus = when {
                    Math.random() < 0.05 -> ZoneStatus.RED_PRESENCE
                    Math.random() < 0.1 -> ZoneStatus.YELLOW_POSSIBLE
                    else -> ZoneStatus.GREEN_CLEAR
                },
                lastConfidence = (Math.random() * 0.5 + 0.5).toFloat(),
                lastUpdate = System.currentTimeMillis(),
                batteryLevel = ((node.batteryLevel ?: 50) - 1).coerceAtLeast(0)
            )
        }
    }
}
