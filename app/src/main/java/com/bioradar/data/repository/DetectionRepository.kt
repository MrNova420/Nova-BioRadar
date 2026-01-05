package com.bioradar.data.repository

import com.bioradar.core.models.*
import com.bioradar.data.database.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for detection logs
 */
@Singleton
class DetectionLogRepository @Inject constructor(
    private val detectionLogDao: DetectionLogDao
) {
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * Log a detection event
     */
    suspend fun logEvent(event: DetectionEvent) {
        val entity = DetectionLogEntity(
            id = event.id,
            timestamp = event.timestamp,
            mode = event.mode.name,
            targetCount = event.targets.size,
            maxConfidence = event.targets.maxOfOrNull { it.confidence } ?: 0f,
            activeSensors = event.activeSensors.joinToString(",") { it.name },
            deviceStable = event.deviceStable,
            batteryLevel = event.batteryLevel,
            locationLabel = event.locationLabel,
            encrypted = event.encrypted,
            encryptedData = null
        )
        detectionLogDao.insert(entity)
    }
    
    /**
     * Get all logs as DetectionEvents
     */
    fun getAllLogs(): Flow<List<DetectionLogSummary>> {
        return detectionLogDao.getAllLogs().map { entities ->
            entities.map { entity ->
                DetectionLogSummary(
                    id = entity.id,
                    timestamp = entity.timestamp,
                    mode = OperatingMode.valueOf(entity.mode),
                    targetCount = entity.targetCount,
                    maxConfidence = entity.maxConfidence,
                    locationLabel = entity.locationLabel
                )
            }
        }
    }
    
    /**
     * Get logs since a specific time
     */
    fun getLogsSince(since: Long): Flow<List<DetectionLogSummary>> {
        return detectionLogDao.getLogsSince(since).map { entities ->
            entities.map { entity ->
                DetectionLogSummary(
                    id = entity.id,
                    timestamp = entity.timestamp,
                    mode = OperatingMode.valueOf(entity.mode),
                    targetCount = entity.targetCount,
                    maxConfidence = entity.maxConfidence,
                    locationLabel = entity.locationLabel
                )
            }
        }
    }
    
    /**
     * Get log count
     */
    suspend fun getLogCount(): Int = detectionLogDao.getLogCount()
    
    /**
     * Delete logs older than specified time
     */
    suspend fun deleteOlderThan(timestamp: Long) {
        detectionLogDao.deleteOlderThan(timestamp)
    }
    
    /**
     * Delete all logs
     */
    suspend fun deleteAll() {
        detectionLogDao.deleteAll()
    }
}

/**
 * Summary of a detection log
 */
data class DetectionLogSummary(
    val id: String,
    val timestamp: Long,
    val mode: OperatingMode,
    val targetCount: Int,
    val maxConfidence: Float,
    val locationLabel: String?
)

/**
 * Repository for zones
 */
@Singleton
class ZoneRepository @Inject constructor(
    private val zoneDao: ZoneDao,
    private val calibrationDao: CalibrationDao
) {
    /**
     * Save a zone
     */
    suspend fun saveZone(zone: PerimeterZone) {
        val entity = ZoneEntity(
            id = zone.id,
            name = zone.name,
            sector = zone.monitoringSector.name,
            sensitivity = zone.sensitivity.name,
            alertType = zone.alertType.name,
            activeSensors = zone.activeSensors.joinToString(",") { it.name },
            baselineCalibrated = zone.baselineCalibrated,
            createdAt = zone.createdAt
        )
        zoneDao.insert(entity)
        
        // Save calibration if exists
        zone.baselineData?.let { baseline ->
            val calibrationEntity = CalibrationEntity(
                zoneId = zone.id,
                avgRssiVariance = baseline.avgRssiVariance,
                avgSonarEnergy = baseline.avgSonarEnergy,
                avgCameraMotion = baseline.avgCameraMotion,
                ambientNoiseLevel = baseline.ambientNoiseLevel,
                calibrationTime = baseline.calibrationTime,
                sampleCount = baseline.sampleCount,
                environmentType = baseline.environmentType
            )
            calibrationDao.insert(calibrationEntity)
        }
    }
    
    /**
     * Get all zones
     */
    fun getAllZones(): Flow<List<PerimeterZone>> {
        return zoneDao.getAllZones().map { entities ->
            entities.map { entity -> entity.toPerimeterZone() }
        }
    }
    
    /**
     * Get zone with calibration
     */
    suspend fun getZoneWithCalibration(zoneId: String): PerimeterZone? {
        val entity = zoneDao.getZoneById(zoneId) ?: return null
        val calibration = calibrationDao.getCalibration(zoneId)
        
        return entity.toPerimeterZone().copy(
            baselineData = calibration?.toSensorBaseline()
        )
    }
    
    /**
     * Delete zone
     */
    suspend fun deleteZone(zoneId: String) {
        val zone = zoneDao.getZoneById(zoneId) ?: return
        zoneDao.delete(zone)
        calibrationDao.deleteCalibration(zoneId)
    }
    
    /**
     * Delete all zones
     */
    suspend fun deleteAll() {
        zoneDao.deleteAll()
        calibrationDao.deleteAll()
    }
    
    private fun ZoneEntity.toPerimeterZone(): PerimeterZone {
        return PerimeterZone(
            id = id,
            name = name,
            monitoringSector = Sector.valueOf(sector),
            sensitivity = Sensitivity.valueOf(sensitivity),
            alertType = AlertType.valueOf(alertType),
            activeSensors = if (activeSensors.isNotEmpty()) {
                activeSensors.split(",").map { DataSource.valueOf(it) }.toSet()
            } else {
                emptySet()
            },
            baselineCalibrated = baselineCalibrated,
            createdAt = createdAt
        )
    }
    
    private fun CalibrationEntity.toSensorBaseline(): SensorBaseline {
        return SensorBaseline(
            avgRssiVariance = avgRssiVariance,
            avgSonarEnergy = avgSonarEnergy,
            avgCameraMotion = avgCameraMotion,
            ambientNoiseLevel = ambientNoiseLevel,
            calibrationTime = calibrationTime,
            sampleCount = sampleCount,
            environmentType = environmentType
        )
    }
}
