package com.annhonii.cloudygallery.crypto

import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AES-256-GCM encryption manager for Cloudy Gallery.
 *
 * Binary file format:
 * [4-byte magic "CGAL"] [16-byte salt] [12-byte IV] [4-byte original-size-int] [AES-256-GCM ciphertext]
 *
 * Key derivation: PBKDF2WithHmacSHA256(masterPassword, salt, 310_000 iterations, 256-bit)
 */
@Singleton
class EncryptionManager @Inject constructor() {

    companion object {
        private const val TAG = "EncryptionManager"
        val MAGIC = byteArrayOf(0x43, 0x47, 0x41, 0x4C) // "CGAL"
        const val MAGIC_SIZE = 4
        const val SALT_SIZE = 16
        const val IV_SIZE = 12
        const val ORIGINAL_SIZE_FIELD = 4
        const val HEADER_SIZE = MAGIC_SIZE + SALT_SIZE + IV_SIZE + ORIGINAL_SIZE_FIELD

        const val PBKDF2_ITERATIONS = 310_000
        const val KEY_LENGTH_BITS = 256
        const val GCM_TAG_LENGTH = 128

        private const val ALGORITHM = "AES/GCM/NoPadding"
        private const val KEY_ALGORITHM = "AES"
        private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    }

    private val secureRandom = SecureRandom()

    /**
     * Encrypts photo bytes using AES-256-GCM with PBKDF2 key derivation.
     * @param inputBytes raw photo/video bytes
     * @param masterPassword user's master password
     * @return .bin file bytes in CGAL format
     */
    fun encryptPhoto(inputBytes: ByteArray, masterPassword: String): ByteArray {
        val salt = ByteArray(SALT_SIZE).also { secureRandom.nextBytes(it) }
        val iv = ByteArray(IV_SIZE).also { secureRandom.nextBytes(it) }

        val key = deriveKey(masterPassword, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))

        val ciphertext = cipher.doFinal(inputBytes)
        val originalSize = inputBytes.size

        // Build output: magic + salt + iv + originalSize + ciphertext
        val output = ByteBuffer.allocate(HEADER_SIZE + ciphertext.size)
            .order(ByteOrder.BIG_ENDIAN)
            .put(MAGIC)
            .put(salt)
            .put(iv)
            .putInt(originalSize)
            .put(ciphertext)

        Log.d(TAG, "Encrypted ${inputBytes.size} bytes → ${output.capacity()} bytes")
        return output.array()
    }

    /**
     * Decrypts a .bin file and returns the original photo bytes.
     * @param binBytes encrypted .bin file bytes
     * @param masterPassword user's master password
     * @return decrypted original photo bytes
     */
    fun decryptPhoto(binBytes: ByteArray, masterPassword: String): ByteArray {
        require(binBytes.size >= HEADER_SIZE) {
            "Invalid .bin file: too small (${binBytes.size} bytes)"
        }

        val buffer = ByteBuffer.wrap(binBytes).order(ByteOrder.BIG_ENDIAN)

        // Verify magic
        val magic = ByteArray(MAGIC_SIZE).also { buffer.get(it) }
        require(magic.contentEquals(MAGIC)) {
            "Invalid .bin file: wrong magic bytes (got ${magic.toHex()})"
        }

        val salt = ByteArray(SALT_SIZE).also { buffer.get(it) }
        val iv = ByteArray(IV_SIZE).also { buffer.get(it) }
        val originalSize = buffer.int

        val ciphertextSize = binBytes.size - HEADER_SIZE
        val ciphertext = ByteArray(ciphertextSize).also { buffer.get(it) }

        val key = deriveKey(masterPassword, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))

        val decrypted = cipher.doFinal(ciphertext)

        // Verify original size
        require(decrypted.size == originalSize) {
            "Decrypted size mismatch: expected $originalSize, got ${decrypted.size}"
        }

        Log.d(TAG, "Decrypted ${binBytes.size} bytes → ${decrypted.size} bytes")
        return decrypted
    }

    /**
     * Derives a 256-bit AES key using PBKDF2WithHmacSHA256.
     */
    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        val spec = PBEKeySpec(
            password.toCharArray(),
            salt,
            PBKDF2_ITERATIONS,
            KEY_LENGTH_BITS
        )
        val keyBytes = factory.generateSecret(spec).encoded
        spec.clearPassword()
        return SecretKeySpec(keyBytes, KEY_ALGORITHM)
    }

    /**
     * Validates that a .bin file has the correct CGAL magic header.
     */
    fun isValidBinFile(bytes: ByteArray): Boolean {
        if (bytes.size < MAGIC_SIZE) return false
        return bytes.take(MAGIC_SIZE).toByteArray().contentEquals(MAGIC)
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02X".format(it) }
}
