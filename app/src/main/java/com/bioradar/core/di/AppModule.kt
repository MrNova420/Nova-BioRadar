package com.bioradar.core.di

import android.content.Context
import com.bioradar.security.PanicWipe
import com.bioradar.security.SecureStorage
import com.bioradar.sensor.drivers.AudioSonarDriver
import com.bioradar.sensor.drivers.BluetoothScanner
import com.bioradar.sensor.drivers.CameraMotionDriver
import com.bioradar.sensor.drivers.WifiScanner
import com.bioradar.sensor.fusion.FusionEngine
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
}
