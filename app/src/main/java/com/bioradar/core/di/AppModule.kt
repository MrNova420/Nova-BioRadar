package com.bioradar.core.di

import android.content.Context
import androidx.room.Room
import com.bioradar.core.managers.AlertManager
import com.bioradar.core.managers.LocationManager
import com.bioradar.core.managers.PowerManager
import com.bioradar.data.database.*
import com.bioradar.data.repository.*
import com.bioradar.ml.FeatureExtractor
import com.bioradar.ml.PresenceClassifier
import com.bioradar.modes.PerimeterGuard
import com.bioradar.modes.TripwireGuard
import com.bioradar.security.PanicWipe
import com.bioradar.security.SecureStorage
import com.bioradar.sensor.drivers.AudioSonarDriver
import com.bioradar.sensor.drivers.BluetoothScanner
import com.bioradar.sensor.drivers.CameraMotionDriver
import com.bioradar.sensor.drivers.WifiScanner
import com.bioradar.sensor.fusion.FusionEngine
import com.bioradar.sensor.processors.FftProcessor
import com.bioradar.sensor.processors.RssiAnalyzer
import com.bioradar.sensor.processors.SelfMotionDetector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt dependency injection module for BioRadar
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    // Database
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): BioRadarDatabase {
        return Room.databaseBuilder(
            context,
            BioRadarDatabase::class.java,
            "bioradar_database"
        ).build()
    }
    
    @Provides
    fun provideDetectionLogDao(database: BioRadarDatabase): DetectionLogDao {
        return database.detectionLogDao()
    }
    
    @Provides
    fun provideZoneDao(database: BioRadarDatabase): ZoneDao {
        return database.zoneDao()
    }
    
    @Provides
    fun provideCalibrationDao(database: BioRadarDatabase): CalibrationDao {
        return database.calibrationDao()
    }
    
    // Repositories
    @Provides
    @Singleton
    fun provideDetectionLogRepository(
        detectionLogDao: DetectionLogDao
    ): DetectionLogRepository {
        return DetectionLogRepository(detectionLogDao)
    }
    
    @Provides
    @Singleton
    fun provideZoneRepository(
        zoneDao: ZoneDao,
        calibrationDao: CalibrationDao
    ): ZoneRepository {
        return ZoneRepository(zoneDao, calibrationDao)
    }
    
    // Sensor Drivers
    @Provides
    @Singleton
    fun provideBluetoothScanner(
        @ApplicationContext context: Context
    ): BluetoothScanner {
        return BluetoothScanner(context)
    }
    
    @Provides
    @Singleton
    fun provideWifiScanner(
        @ApplicationContext context: Context
    ): WifiScanner {
        return WifiScanner(context)
    }
    
    @Provides
    @Singleton
    fun provideAudioSonarDriver(
        @ApplicationContext context: Context
    ): AudioSonarDriver {
        return AudioSonarDriver(context)
    }
    
    @Provides
    @Singleton
    fun provideCameraMotionDriver(
        @ApplicationContext context: Context
    ): CameraMotionDriver {
        return CameraMotionDriver(context)
    }
    
    // Signal Processors
    @Provides
    @Singleton
    fun provideRssiAnalyzer(): RssiAnalyzer {
        return RssiAnalyzer()
    }
    
    @Provides
    @Singleton
    fun provideFftProcessor(): FftProcessor {
        return FftProcessor()
    }
    
    @Provides
    @Singleton
    fun provideSelfMotionDetector(): SelfMotionDetector {
        return SelfMotionDetector()
    }
    
    // Fusion Engine
    @Provides
    @Singleton
    fun provideFusionEngine(
        bluetoothScanner: BluetoothScanner,
        wifiScanner: WifiScanner,
        audioSonarDriver: AudioSonarDriver,
        cameraMotionDriver: CameraMotionDriver
    ): FusionEngine {
        return FusionEngine(
            bluetoothScanner,
            wifiScanner,
            audioSonarDriver,
            cameraMotionDriver
        )
    }
    
    // Security
    @Provides
    @Singleton
    fun provideSecureStorage(
        @ApplicationContext context: Context
    ): SecureStorage {
        return SecureStorage(context)
    }
    
    @Provides
    @Singleton
    fun providePanicWipe(
        @ApplicationContext context: Context,
        secureStorage: SecureStorage
    ): PanicWipe {
        return PanicWipe(context, secureStorage)
    }
    
    // Managers
    @Provides
    @Singleton
    fun provideAlertManager(
        @ApplicationContext context: Context
    ): AlertManager {
        return AlertManager(context)
    }
    
    @Provides
    @Singleton
    fun providePowerManager(
        @ApplicationContext context: Context
    ): PowerManager {
        return PowerManager(context)
    }
    
    @Provides
    @Singleton
    fun provideLocationManager(): LocationManager {
        return LocationManager()
    }
    
    // ML
    @Provides
    @Singleton
    fun providePresenceClassifier(
        @ApplicationContext context: Context
    ): PresenceClassifier {
        return PresenceClassifier(context)
    }
    
    @Provides
    @Singleton
    fun provideFeatureExtractor(): FeatureExtractor {
        return FeatureExtractor()
    }
    
    // Modes
    @Provides
    @Singleton
    fun providePerimeterGuard(
        fusionEngine: FusionEngine,
        alertManager: AlertManager,
        logRepository: DetectionLogRepository
    ): PerimeterGuard {
        return PerimeterGuard(fusionEngine, alertManager, logRepository)
    }
    
    @Provides
    @Singleton
    fun provideTripwireGuard(
        perimeterGuard: PerimeterGuard
    ): TripwireGuard {
        return TripwireGuard(perimeterGuard)
    }
}
