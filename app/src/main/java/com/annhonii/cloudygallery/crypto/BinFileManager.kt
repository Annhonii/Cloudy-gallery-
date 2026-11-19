package com.annhonii.cloudygallery.crypto

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages saving/loading/deleting .bin files in the app's private cache directory.
 * Path: /data/data/<package>/cache/cloud_buffer/
 */
@Singleton
class BinFileManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "BinFileManager"
        private const val CLOUD_BUFFER_DIR = "cloud_buffer"
        private const val BIN_EXTENSION = ".bin"
    }

    private val bufferDir: File by lazy {
        File(context.cacheDir, CLOUD_BUFFER_DIR).also { dir ->
            if (!dir.exists()) {
                dir.mkdirs()
                Log.d(TAG, "Created cloud buffer dir: ${dir.absolutePath}")
            }
        }
    }

    /**
     * Saves encrypted .bin bytes to the cloud buffer directory.
     * @param fileName desired file name (without extension, or with)
     * @param bytes encrypted .bin bytes
     * @return absolute path to the saved file
     */
    fun saveBinFile(fileName: String, bytes: ByteArray): String {
        val name = if (fileName.endsWith(BIN_EXTENSION)) fileName else "$fileName$BIN_EXTENSION"
        val file = File(bufferDir, name)
        file.writeBytes(bytes)
        Log.d(TAG, "Saved bin file: ${file.absolutePath} (${bytes.size} bytes)")
        return file.absolutePath
    }

    /**
     * Saves encrypted .bin bytes with an auto-generated UUID filename.
     * @return absolute path to the saved file
     */
    fun saveBinFileWithUuid(bytes: ByteArray): String {
        val uuid = UUID.randomUUID().toString()
        return saveBinFile(uuid, bytes)
    }

    /**
     * Loads .bin file bytes from the given absolute path.
     */
    fun loadBinFile(absolutePath: String): ByteArray? {
        val file = File(absolutePath)
        if (!file.exists()) {
            Log.w(TAG, "Bin file not found: $absolutePath")
            return null
        }
        return try {
            file.readBytes().also {
                Log.d(TAG, "Loaded bin file: $absolutePath (${it.size} bytes)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load bin file: $absolutePath", e)
            null
        }
    }

    /**
     * Deletes a .bin file at the given absolute path.
     */
    fun deleteBinFile(absolutePath: String): Boolean {
        val file = File(absolutePath)
        return if (file.exists()) {
            file.delete().also { deleted ->
                Log.d(TAG, "Deleted bin file: $absolutePath ($deleted)")
            }
        } else {
            Log.w(TAG, "Bin file to delete not found: $absolutePath")
            false
        }
    }

    /**
     * Returns the total size of all buffered .bin files in bytes.
     */
    fun getTotalBufferSize(): Long {
        return bufferDir.walkTopDown()
            .filter { it.isFile && it.name.endsWith(BIN_EXTENSION) }
            .sumOf { it.length() }
    }

    /**
     * Clears all buffered .bin files.
     */
    fun clearBuffer() {
        bufferDir.walkTopDown()
            .filter { it.isFile }
            .forEach { it.delete() }
        Log.d(TAG, "Cleared cloud buffer")
    }

    /**
     * Returns the cloud buffer directory path.
     */
    fun getBufferDirPath(): String = bufferDir.absolutePath

    /**
     * Checks if a local bin file exists for the given path.
     */
    fun exists(absolutePath: String): Boolean = File(absolutePath).exists()
}
