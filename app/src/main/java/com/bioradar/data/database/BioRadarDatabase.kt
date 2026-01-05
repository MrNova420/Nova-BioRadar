package com.bioradar.data.database

import androidx.room.*
import com.bioradar.core.models.*
import kotlinx.coroutines.flow.Flow

/**
 * Room Database for BioRadar
 */
@Database(
    entities = [
        DetectionLogEntity::class,
        ZoneEntity::class,
        CalibrationEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class BioRadarDatabase : RoomDatabase() {
    abstract fun detectionLogDao(): DetectionLogDao
    abstract fun zoneDao(): ZoneDao
    abstract fun calibrationDao(): CalibrationDao
}

/**
 * Detection log entity for storing detection events
 */
@Entity(tableName = "detection_logs")
data class DetectionLogEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val mode: String,
    val targetCount: Int,
    val maxConfidence: Float,
    val activeSensors: String,  // JSON array
    val deviceStable: Boolean,
    val batteryLevel: Int,
    val locationLabel: String?,
    val encrypted: Boolean,
    val encryptedData: ByteArray?  // For encrypted logs
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as DetectionLogEntity
        return id == other.id
    }
    
    override fun hashCode(): Int = id.hashCode()
}

/**
 * Zone entity for storing perimeter zones
 */
@Entity(tableName = "zones")
data class ZoneEntity(
    @PrimaryKey val id: String,
    val name: String,
    val sector: String,
    val sensitivity: String,
    val alertType: String,
    val activeSensors: String,  // JSON array
    val baselineCalibrated: Boolean,
    val createdAt: Long
)

/**
 * Calibration entity for storing sensor baselines
 */
@Entity(tableName = "calibrations")
data class CalibrationEntity(
    @PrimaryKey val zoneId: String,
    val avgRssiVariance: Float,
    val avgSonarEnergy: Float,
    val avgCameraMotion: Float,
    val ambientNoiseLevel: Float,
    val calibrationTime: Long,
    val sampleCount: Int,
    val environmentType: String?
)

/**
 * DAO for detection logs
 */
@Dao
interface DetectionLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: DetectionLogEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<DetectionLogEntity>)
    
    @Query("SELECT * FROM detection_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<DetectionLogEntity>>
    
    @Query("SELECT * FROM detection_logs WHERE timestamp > :since ORDER BY timestamp DESC")
    fun getLogsSince(since: Long): Flow<List<DetectionLogEntity>>
    
    @Query("SELECT * FROM detection_logs WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getLogsBetween(start: Long, end: Long): Flow<List<DetectionLogEntity>>
    
    @Query("SELECT * FROM detection_logs WHERE locationLabel = :label ORDER BY timestamp DESC")
    fun getLogsByLocation(label: String): Flow<List<DetectionLogEntity>>
    
    @Query("SELECT COUNT(*) FROM detection_logs")
    suspend fun getLogCount(): Int
    
    @Query("DELETE FROM detection_logs WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)
    
    @Query("DELETE FROM detection_logs")
    suspend fun deleteAll()
}

/**
 * DAO for zones
 */
@Dao
interface ZoneDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(zone: ZoneEntity)
    
    @Update
    suspend fun update(zone: ZoneEntity)
    
    @Delete
    suspend fun delete(zone: ZoneEntity)
    
    @Query("SELECT * FROM zones ORDER BY createdAt DESC")
    fun getAllZones(): Flow<List<ZoneEntity>>
    
    @Query("SELECT * FROM zones WHERE id = :id")
    suspend fun getZoneById(id: String): ZoneEntity?
    
    @Query("DELETE FROM zones")
    suspend fun deleteAll()
}

/**
 * DAO for calibrations
 */
@Dao
interface CalibrationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(calibration: CalibrationEntity)
    
    @Query("SELECT * FROM calibrations WHERE zoneId = :zoneId")
    suspend fun getCalibration(zoneId: String): CalibrationEntity?
    
    @Query("DELETE FROM calibrations WHERE zoneId = :zoneId")
    suspend fun deleteCalibration(zoneId: String)
    
    @Query("DELETE FROM calibrations")
    suspend fun deleteAll()
}

/**
 * Type converters for Room
 */
class Converters {
    @TypeConverter
    fun fromDataSourceSet(sources: Set<DataSource>): String {
        return sources.joinToString(",") { it.name }
    }
    
    @TypeConverter
    fun toDataSourceSet(data: String): Set<DataSource> {
        if (data.isEmpty()) return emptySet()
        return data.split(",").map { DataSource.valueOf(it) }.toSet()
    }
}
