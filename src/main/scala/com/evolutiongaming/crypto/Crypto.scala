package com.evolutiongaming.crypto

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8
import java.security.SecureRandom

import javax.crypto.spec.{GCMParameterSpec, IvParameterSpec, SecretKeySpec}
import javax.crypto.{AEADBadTagException, Cipher}
import org.apache.commons.codec.binary.{Base64, Hex}
import org.apache.commons.codec.digest.DigestUtils

/**
  * Copyright (C) 2009-2016 Lightbend Inc. <https://www.lightbend.com>
  *
  * Based on https://github.com/playframework/playframework/blob/master/framework/src/play/src/main/scala/play/api/libs/Crypto.scala
  */
object Crypto {
  class DecryptAuthException(cause: Throwable) extends Exception(
    "Decrypted value is not the original one, most likely wrong private key used for decryption",
    cause,
  )

  /*
  using lazy val to postpone init until the first usage so class loading does not get blocked
  by obtaining entropy for the seed - same trick as in java.util.UUID.Holder
   */
  private lazy val secureRandom = new SecureRandom

  /**
    * Encrypts a string with the AES algorithm and the supplied private key - pair to the
    * [[decryptAES]] method.
    *
    * AES/GCM/NoPadding transformation is used with 128 bit key for authenticated encryption.
    * The secret key entropy is obtained from the given private key by applying the SHA256 hash.
    *
    * @param value      string value to encrypt
    * @param privateKey private key string to use in encryption
    * @return an encrypted string
    */
  def encryptAES(value: String, privateKey: String): String = {
    s"3-${ AES_V3.encrypt(value, privateKey) }"
  }

  /**
    * Decrypts a string with the AES algorithm and the supplied private key - pair to the
    * [[encryptAES]] method.
    *
    * Additionally to the current [[encryptAES]] encryption mode, several legacy modes supported.
    *
    * If the current [[encryptAES]] algorithm is used, it is guaranteed that if a decrypted value is returned
    * it is the original one and the private key is valid. In case a wrong private key is used, an exception
    * will be thrown.
    *
    * @param value      an encrypted string produced by the [[encryptAES]] method
    * @param privateKey private key string used in encryption
    * @return decrypted string
    */
  def decryptAES(value: String, privateKey: String): String = {
    val separator = "-"
    val sepIndex = value.indexOf(separator)
    if (sepIndex < 0) {
      AES_V0.decrypt(value, privateKey)
    } else {
      val version = value.take(sepIndex)
      val data = value.drop(sepIndex + 1)
      version match {
        case "1" =>
          AES_V1.decrypt(data, privateKey)
        case "2" =>
          AES_V2.decrypt(data, privateKey)
        case "3" =>
          AES_V3.decrypt(data, privateKey)
        case _   =>
          throw new RuntimeException("Unknown version")
      }
    }
  }

  /** AES legacy V0 (no versioning) mode support - it has restrictions on key size */
  private object AES_V0 {
    private val CipherAlgorithm = "AES"
    private val CipherTransformation = "AES"
    private val KeySizeBytes: Int = 16 //128 bit

    def decrypt(value: String, privateKey: String): String = {
      val privateKeyBytes = privateKey.getBytes(UTF_8)
      val effectiveSecretKey = privateKeyBytes.take(KeySizeBytes)
      val skeySpec = new SecretKeySpec(effectiveSecretKey, CipherAlgorithm)
      val cipher = Cipher.getInstance(CipherTransformation)
      cipher.init(Cipher.DECRYPT_MODE, skeySpec)
      val valueBytes = Hex.decodeHex(value)
      val decryptedValueBytes = cipher.doFinal(valueBytes)
      new String(decryptedValueBytes, UTF_8)
    }
  }

  /**
    * AES legacy V1 mode support:
    * - no restrictions on key size - SHA256 hash is used to obtain key entropy
    */
  private object AES_V1 {
    private val CipherTransformation = "AES"

    def decrypt(value: String, privateKey: String): String = {
      val valueBytes = Base64.decodeBase64(value)
      val skeySpec = aesSKey128bitWithSha256(privateKey.getBytes(UTF_8))
      val cipher = Cipher.getInstance(CipherTransformation)
      cipher.init(Cipher.DECRYPT_MODE, skeySpec)
      val decryptedValueBytes = cipher.doFinal(valueBytes)
      new String(decryptedValueBytes, UTF_8)
    }
  }

  /**
    * AES legacy V1 mode support:
    * - no restrictions on key size - SHA256 hash is used to obtain key entropy
    * - AES/CTR/NoPadding (128 bit key) cipher with IV
    */
  private object AES_V2 {
    private val CipherTransformation = "AES/CTR/NoPadding"

    def decrypt(value: String, privateKey: String): String = {
      val ivWithEncryptedData = Base64.decodeBase64(value)
      val skeySpec = aesSKey128bitWithSha256(privateKey.getBytes(UTF_8))
      val cipher = Cipher.getInstance(CipherTransformation)
      val blockSize = cipher.getBlockSize
      require(ivWithEncryptedData.length >= blockSize, "invalid data size")
      val (iv, encryptedData) = ivWithEncryptedData.splitAt(blockSize)
      cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(iv))
      val decryptedData = cipher.doFinal(encryptedData)
      new String(decryptedData, UTF_8)
    }
  }

  /**
    * Current AES mode - V3:
    * - no restrictions on key size - SHA256 hash is used to obtain key entropy
    * - AES/GCM/NoPadding (128 bit key) cipher is used to provide authenticated encryption
    * - dynamic length random IV - 12 bytes by default with possible extension up to 255 bytes
    * - 128 bit auth tag length
    */
  private object AES_V3 {
    /*
    implementation based on
    https://proandroiddev.com/security-best-practices-symmetric-encryption-with-aes-in-java-7616beaaade9
     */

    private val CipherTransformation = "AES/GCM/NoPadding"
    /*
    same auth tag length as in
    https://proandroiddev.com/security-best-practices-symmetric-encryption-with-aes-in-java-7616beaaade9
     */
    private val AuthTagLengthBits = 128
    /*
    for GCM IV a 12 byte random byte-array is recommend by NIST
    https://nvlpubs.nist.gov/nistpubs/Legacy/SP/nistspecialpublication800-38d.pdf
     */
    private val CurrentIVLengthBytes = 12
    private val MinIVLengthBytes = 12 //does not allow decrypting with IVs smaller than 12 bytes (96 bits)

    def encrypt(value: String, privateKey: String): String = {
      val skeySpec = aesSKey128bitWithSha256(privateKey.getBytes(UTF_8))
      val iv = new Array[Byte](CurrentIVLengthBytes)
      secureRandom.nextBytes(iv)
      val parameterSpec = new GCMParameterSpec(AuthTagLengthBits, iv)
      val cipher = Cipher.getInstance(CipherTransformation)
      cipher.init(Cipher.ENCRYPT_MODE, skeySpec, parameterSpec)
      val encryptedValue = cipher.doFinal(value.getBytes(UTF_8))
      encodeEncryptedToString(iv, encryptedValue)
    }

    private def encodeEncryptedToString(iv: Array[Byte], encryptedValue: Array[Byte]): String = {
      //1 byte for dynamic IV length encoding
      val buf = ByteBuffer.allocate(1 + iv.length + encryptedValue.length)
      //encode IV length as an unsigned byte
      buf.put(iv.length.toByte)
      buf.put(iv)
      buf.put(encryptedValue)
      Base64.encodeBase64String(buf.array())
    }

    def decrypt(value: String, privateKey: String): String = {
      val payload = Base64.decodeBase64(value)
      val skeySpec = aesSKey128bitWithSha256(privateKey.getBytes(UTF_8))
      val cipher = Cipher.getInstance(CipherTransformation)
      val ivLength = readValidIvLength(payload)

      val ivOffset = 1 //1 byte for encoded IV length
      val ivEndIdx = ivOffset + ivLength
      require(payload.length >= ivEndIdx, "invalid data size")
      val gcmParamSpec = new GCMParameterSpec(AuthTagLengthBits, payload, ivOffset, ivLength)
      cipher.init(Cipher.DECRYPT_MODE, skeySpec, gcmParamSpec)
      val decryptInputLength = payload.length - ivEndIdx
      val decryptedValue = try {
        cipher.doFinal(payload, ivEndIdx, decryptInputLength)
      } catch {
        case e: AEADBadTagException => throw new DecryptAuthException(e)
      }
      new String(decryptedValue, UTF_8)
    }

    private def readValidIvLength(payload: Array[Byte]): Int = {
      require(payload.length > 0, "invalid data size")
      val ivLength = java.lang.Byte.toUnsignedInt(payload(0))
      require(ivLength >= MinIVLengthBytes, s"IV length shouldn't be smaller than $MinIVLengthBytes bytes")
      ivLength
    }
  }

  /**
    * Creates a SecretKeySpec instance for an AES algorithm with an 128 bit key produced from SHA256 hash of
    * the private key data.
    */
  private def aesSKey128bitWithSha256(privateKeyBytes: Array[Byte]): SecretKeySpec = {
    val privateKeyDigest = DigestUtils.sha256(privateKeyBytes)
    val effectiveSecretKey = privateKeyDigest.take(16) //128 bit = 16 bytes
    new SecretKeySpec(effectiveSecretKey, "AES")
  }
}
