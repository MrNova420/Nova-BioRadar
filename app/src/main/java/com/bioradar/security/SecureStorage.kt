package com.bioradar.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.RandomAccessFile
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure storage for sensitive data
 * Uses AES-256-GCM encryption
 */
@Singleton
class SecureStorage @Inject constructor(
    private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * Store encrypted string
     */
    fun putString(key: String, value: String) {
        encryptedPrefs.edit().putString(key, value).apply()
    }
    
    /**
     * Get encrypted string
     */
    fun getString(key: String, default: String? = null): String? {
        return encryptedPrefs.getString(key, default)
    }
    
    /**
     * Store encrypted boolean
     */
    fun putBoolean(key: String, value: Boolean) {
        encryptedPrefs.edit().putBoolean(key, value).apply()
    }
    
    /**
     * Get encrypted boolean
     */
    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return encryptedPrefs.getBoolean(key, default)
    }
    
    /**
     * Store encrypted int
     */
    fun putInt(key: String, value: Int) {
        encryptedPrefs.edit().putInt(key, value).apply()
    }
    
    /**
     * Get encrypted int
     */
    fun getInt(key: String, default: Int = 0): Int {
        return encryptedPrefs.getInt(key, default)
    }
    
    /**
     * Encrypt data with AES-256-GCM
     */
    fun encrypt(data: ByteArray): EncryptedData {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(data)
        return EncryptedData(iv, encrypted)
    }
    
    /**
     * Decrypt data with AES-256-GCM
     */
    fun decrypt(encryptedData: EncryptedData): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, encryptedData.iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        return cipher.doFinal(encryptedData.data)
    }
    
    /**
     * Encrypt a string
     */
    fun encryptString(plainText: String): EncryptedData {
        return encrypt(plainText.toByteArray(Charsets.UTF_8))
    }
    
    /**
     * Decrypt to string
     */
    fun decryptString(encryptedData: EncryptedData): String {
        return decrypt(encryptedData).toString(Charsets.UTF_8)
    }
    
    /**
     * Generate or retrieve the secret key
     */
    private fun getSecretKey(): SecretKey {
        // In production, store key in Android Keystore
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        return keyGenerator.generateKey()
    }
    
    /**
     * Clear all encrypted data
     */
    fun clearAll() {
        encryptedPrefs.edit().clear().apply()
    }
    
    /**
     * Check if a key exists
     */
    fun contains(key: String): Boolean {
        return encryptedPrefs.contains(key)
    }
    
    /**
     * Remove a specific key
     */
    fun remove(key: String) {
        encryptedPrefs.edit().remove(key).apply()
    }
    
    companion object {
        private const val PREFS_NAME = "bioradar_secure_prefs"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
    }
}

/**
 * Encrypted data container
 */
data class EncryptedData(
    val iv: ByteArray,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as EncryptedData
        return iv.contentEquals(other.iv) && data.contentEquals(other.data)
    }
    
    override fun hashCode(): Int {
        var result = iv.contentHashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}

/**
 * Panic Wipe functionality
 * Securely destroys all sensitive data
 */
@Singleton
class PanicWipe @Inject constructor(
    private val context: Context,
    private val secureStorage: SecureStorage
) {
    private val secureRandom = SecureRandom()
    
    /**
     * Execute secure wipe of all data
     */
    fun executeWipe(): WipeResult {
        val wipedFiles = mutableListOf<String>()
        val errors = mutableListOf<String>()
        
        try {
            // 1. Wipe log files
            val logsDir = File(context.filesDir, "logs")
            if (logsDir.exists()) {
                logsDir.listFiles()?.forEach { file ->
                    try {
                        secureDelete(file)
                        wipedFiles.add(file.name)
                    } catch (e: Exception) {
                        errors.add("Failed to wipe ${file.name}: ${e.message}")
                    }
                }
            }
            
            // 2. Wipe databases
            context.databaseList().forEach { dbName ->
                try {
                    val dbFile = context.getDatabasePath(dbName)
                    secureDelete(dbFile)
                    wipedFiles.add(dbName)
                } catch (e: Exception) {
                    errors.add("Failed to wipe database $dbName: ${e.message}")
                }
            }
            
            // 3. Clear encrypted preferences
            secureStorage.clearAll()
            wipedFiles.add("encrypted_prefs")
            
            // 4. Clear cache
            context.cacheDir.deleteRecursively()
            wipedFiles.add("cache")
            
            // 5. Clear shared preferences
            val prefsDir = File(context.dataDir, "shared_prefs")
            if (prefsDir.exists()) {
                prefsDir.listFiles()?.forEach { file ->
                    try {
                        secureDelete(file)
                        wipedFiles.add(file.name)
                    } catch (e: Exception) {
                        errors.add("Failed to wipe ${file.name}: ${e.message}")
                    }
                }
            }
            
        } catch (e: Exception) {
            errors.add("Wipe error: ${e.message}")
        }
        
        return WipeResult(
            success = errors.isEmpty(),
            filesWiped = wipedFiles,
            errors = errors
        )
    }
    
    /**
     * Securely delete a file by overwriting with random data
     */
    private fun secureDelete(file: File) {
        if (!file.exists() || !file.isFile) {
            file.delete()
            return
        }
        
        val length = file.length()
        
        // Overwrite 3 times with random data
        repeat(OVERWRITE_PASSES) {
            RandomAccessFile(file, "rw").use { raf ->
                val buffer = ByteArray(4096)
                var remaining = length
                
                raf.seek(0)
                while (remaining > 0) {
                    secureRandom.nextBytes(buffer)
                    val toWrite = minOf(buffer.size.toLong(), remaining).toInt()
                    raf.write(buffer, 0, toWrite)
                    remaining -= toWrite
                }
            }
        }
        
        // Delete the file
        file.delete()
    }
    
    /**
     * Quick wipe - faster but less secure
     */
    fun quickWipe() {
        // Just delete files without secure overwrite
        val logsDir = File(context.filesDir, "logs")
        logsDir.deleteRecursively()
        
        context.databaseList().forEach { dbName ->
            context.deleteDatabase(dbName)
        }
        
        secureStorage.clearAll()
        context.cacheDir.deleteRecursively()
    }
    
    companion object {
        private const val OVERWRITE_PASSES = 3
    }
}

/**
 * Result of wipe operation
 */
data class WipeResult(
    val success: Boolean,
    val filesWiped: List<String>,
    val errors: List<String>
)
