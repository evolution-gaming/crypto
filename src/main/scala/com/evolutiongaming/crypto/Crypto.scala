package com.evolutiongaming.crypto

import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}

import org.apache.commons.codec.binary.Hex

/**
  * Code extract from https://github.com/playframework/playframework/blob/master/framework/src/play/src/main/scala/play/api/libs/Crypto.scala
  * in order not to pull in whole Play framework as a dependency.
  */
object Crypto {
  val aesTransformation: String = "AES/CTR/NoPadding"

  /**
    * Encrypt a String with the AES encryption standard and the supplied private key.
    *
    *
    * The provider used is by default this uses the platform default JSSE provider.  This can be overridden by defining
    * `play.crypto.provider` in `application.conf`.
    *
    * The transformation algorithm used is the provider specific implementation of the `AES` name.  On Oracles JDK,
    * this is `AES/CTR/NoPadding`.  This algorithm is suitable for small amounts of data, typically less than 32
    * bytes, hence is useful for encrypting credit card numbers, passwords etc.  For larger blocks of data, this
    * algorithm may expose patterns and be vulnerable to repeat attacks.
    *
    * The transformation algorithm can be configured by defining `play.crypto.aes.transformation` in
    * `application.conf`.  Although any cipher transformation algorithm can be selected here, the secret key spec used
    * is always AES, so only AES transformation algorithms will work.
    *
    * @param value      The String to encrypt.
    * @param privateKey The key used to encrypt.
    * @return A Base64 encrypted string.
    */
  def encryptAES(value: String, privateKey: String): String = {
    val skeySpec = secretKeyWithSha256(privateKey, "AES")
    val cipher = getCipherWithConfiguredProvider(aesTransformation)
    cipher.init(Cipher.ENCRYPT_MODE, skeySpec)
    val encryptedValue = cipher.doFinal(value.getBytes("utf-8"))
    // return a formatted, versioned encrypted string
    // '2-*' represents an encrypted payload with an IV
    // '1-*' represents an encrypted payload without an IV
    Option(cipher.getIV) match {
      case Some(iv) => s"2-${ Base64.getEncoder.encodeToString(iv ++ encryptedValue) }"
      case None     => s"1-${ Base64.getEncoder.encodeToString(encryptedValue) }"
    }
  }

  /**
    * Decrypt a String with the AES encryption standard.
    *
    * The private key must have a length of 16 bytes.
    *
    * The provider used is by default this uses the platform default JSSE provider.  This can be overridden by defining
    * `play.crypto.provider` in `application.conf`.
    *
    * The transformation used is by default `AES/CTR/NoPadding`.  It can be configured by defining
    * `play.crypto.aes.transformation` in `application.conf`.  Although any cipher transformation algorithm can
    * be selected here, the secret key spec used is always AES, so only AES transformation algorithms will work.
    *
    * @param value      An hexadecimal encrypted string.
    * @param privateKey The key used to encrypt.
    * @return The decrypted String.
    */
  def decryptAES(value: String, privateKey: String): String = {
    val seperator = "-"
    val sepIndex = value.indexOf(seperator)
    if (sepIndex < 0) {
      decryptAESVersion0(value, privateKey)
    } else {
      val version = value.substring(0, sepIndex)
      val data = value.substring(sepIndex + 1, value.length())
      version match {
        case "1" =>
          decryptAESVersion1(data, privateKey)
        case "2" =>
          decryptAESVersion2(data, privateKey)
        case _   =>
          throw new RuntimeException("Unknown version")
      }
    }
  }

  /**
    * Transform an hexadecimal String to a byte array.
    * From https://github.com/playframework/playframework/blob/master/framework/src/play/src/main/scala/play/api/libs/Codecs.scala
    */
  private def hexStringToByte(hexString: String): Array[Byte] = Hex.decodeHex(hexString.toCharArray)

  /** Backward compatible AES ECB mode decryption support. */
  private def decryptAESVersion0(value: String, privateKey: String): String = {
    val raw = privateKey.substring(0, 16).getBytes("utf-8")
    val skeySpec = new SecretKeySpec(raw, "AES")
    val cipher = getCipherWithConfiguredProvider("AES")
    cipher.init(Cipher.DECRYPT_MODE, skeySpec)
    new String(cipher.doFinal(hexStringToByte(value)))
  }

  /** V1 decryption algorithm (No IV). */
  private def decryptAESVersion1(value: String, privateKey: String): String = {
    val data = Base64.getDecoder.decode(value)
    val skeySpec = secretKeyWithSha256(privateKey, "AES")
    val cipher = getCipherWithConfiguredProvider(aesTransformation)
    cipher.init(Cipher.DECRYPT_MODE, skeySpec)
    new String(cipher.doFinal(data), "utf-8")
  }

  /** V2 decryption algorithm (IV present). */
  private def decryptAESVersion2(value: String, privateKey: String): String = {
    val data = Base64.getDecoder.decode(value)
    val skeySpec = secretKeyWithSha256(privateKey, "AES")
    val cipher = getCipherWithConfiguredProvider(aesTransformation)
    val blockSize = cipher.getBlockSize
    val iv = data.slice(0, blockSize)
    val payload = data.slice(blockSize, data.size)
    cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(iv))
    new String(cipher.doFinal(payload), "utf-8")
  }

  /**
    * Generates the SecretKeySpec, given the private key and the algorithm.
    */
  private def secretKeyWithSha256(privateKey: String, algorithm: String) = {
    val messageDigest = MessageDigest.getInstance("SHA-256")
    messageDigest.update(privateKey.getBytes("utf-8"))
    // max allowed length in bits / (8 bits to a byte)
    // For AES we hardcode keylength to 128bit minimum to not depend on environment security policy settings:
    // it may vary between 128 and 256 bits which can yield different encryption keys if we don't
    val maxAllowedKeyLength = if (algorithm == "AES") 16 else Cipher.getMaxAllowedKeyLength(algorithm) / 8
    val raw = messageDigest.digest().slice(0, maxAllowedKeyLength)
    new SecretKeySpec(raw, algorithm)
  }

  private def getCipherWithConfiguredProvider(transformation: String) = {
    Cipher.getInstance(transformation)
  }
}
