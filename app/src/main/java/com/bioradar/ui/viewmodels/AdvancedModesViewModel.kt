package com.bioradar.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioradar.modes.UltimateMode
import com.bioradar.modes.BlackoutMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Advanced Modes Screen
 * Manages Ultimate Mode and Blackout Mode state
 */
@HiltViewModel
class AdvancedModesViewModel @Inject constructor(
    private val ultimateMode: UltimateMode,
    private val blackoutMode: BlackoutMode
) : ViewModel() {
    
    // Ultimate Mode state
    val ultimateModeConfiguration = ultimateMode.configuration
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    val ultimateModeActive = ultimateMode.isActive
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    
    // Blackout Mode state
    val blackoutModeStatus = blackoutMode.status
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BlackoutMode.BlackoutStatus.Inactive
        )
    
    val blackoutDetections = blackoutMode.detections
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    // Selected Blackout profile
    private val _selectedBlackoutProfile = MutableStateFlow(BlackoutMode.BlackoutProfile.BALANCED)
    val selectedBlackoutProfile = _selectedBlackoutProfile.asStateFlow()
    
    // UI state
    private val _showUltimateDetails = MutableStateFlow(false)
    val showUltimateDetails = _showUltimateDetails.asStateFlow()
    
    private val _showBlackoutDetails = MutableStateFlow(false)
    val showBlackoutDetails = _showBlackoutDetails.asStateFlow()
    
    /**
     * Activate Ultimate Mode
     */
    fun activateUltimateMode() {
        viewModelScope.launch {
            try {
                ultimateMode.activate()
            } catch (e: Exception) {
                android.util.Log.e("AdvancedModesVM", "Failed to activate Ultimate Mode", e)
            }
        }
    }
    
    /**
     * Deactivate Ultimate Mode
     */
    fun deactivateUltimateMode() {
        ultimateMode.deactivate()
    }
    
    /**
     * Activate Blackout Mode with selected profile
     */
    fun activateBlackoutMode() {
        viewModelScope.launch {
            try {
                blackoutMode.activate(_selectedBlackoutProfile.value)
            } catch (e: Exception) {
                android.util.Log.e("AdvancedModesVM", "Failed to activate Blackout Mode", e)
            }
        }
    }
    
    /**
     * Deactivate Blackout Mode
     */
    fun deactivateBlackoutMode() {
        blackoutMode.deactivate()
    }
    
    /**
     * Change selected Blackout profile
     */
    fun setBlackoutProfile(profile: BlackoutMode.BlackoutProfile) {
        _selectedBlackoutProfile.value = profile
    }
    
    /**
     * Toggle Ultimate Mode details visibility
     */
    fun toggleUltimateDetails() {
        _showUltimateDetails.value = !_showUltimateDetails.value
    }
    
    /**
     * Toggle Blackout Mode details visibility
     */
    fun toggleBlackoutDetails() {
        _showBlackoutDetails.value = !_showBlackoutDetails.value
    }
    
    /**
     * Get recommended Blackout profile based on battery
     */
    fun getRecommendedBlackoutProfile(batteryPercent: Int, isCharging: Boolean): BlackoutMode.BlackoutProfile {
        return blackoutMode.getRecommendedProfile(batteryPercent, isCharging)
    }
    
    /**
     * Estimate remaining battery time for profile
     */
    fun estimateRemainingTime(batteryPercent: Int, profile: BlackoutMode.BlackoutProfile): Float {
        return blackoutMode.estimateRemainingTime(batteryPercent, profile)
    }
    
    override fun onCleared() {
        super.onCleared()
        // Cleanup if modes are active
        if (ultimateModeActive.value) {
            ultimateMode.deactivate()
        }
        if (blackoutModeStatus.value is BlackoutMode.BlackoutStatus.Active) {
            blackoutMode.deactivate()
        }
    }
}
