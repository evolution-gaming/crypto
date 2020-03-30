package com.evolutiongaming.crypto

import java.nio.charset.StandardCharsets.UTF_8

import javax.crypto.Cipher
import javax.crypto.spec.{GCMParameterSpec, SecretKeySpec}
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.digest.DigestUtils
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.Random

class CryptoSpec extends AnyFlatSpec with Matchers {
  behavior of "Crypto"

  it should "decrypt data encrypted with same AES key" in {
    val secret = "test secret"
    val original = "test data please ignore"

    val encrypted = Crypto.encryptAES(original, secret)
    val decrypted = Crypto.decryptAES(encrypted, secret)

    original shouldEqual decrypted
  }

  it should "generate different encrypted strings on every run and should decrypt both" in {
    val secret = "test secret"
    val original = "test data please ignore"

    val encrypted = Crypto.encryptAES(original, secret)
    val encrypted2 = Crypto.encryptAES(original, secret)
    val decrypted = Crypto.decryptAES(encrypted, secret)
    val decrypted2 = Crypto.decryptAES(encrypted2, secret)

    encrypted should not equal encrypted2
    original shouldEqual decrypted
    original shouldEqual decrypted2
  }

  it should "decrypt data encrypted with same AES key (long)" in {
    val original = "test data please ignore"
    val key = "1234567890123456now_it_became_too_long"

    val encrypted = Crypto.encryptAES(original, key)
    val decrypted = Crypto.decryptAES(encrypted, key)

    original shouldEqual decrypted
  }

  it should "fail on decryption when encryption and decryption keys are different" in {
    val original = "test data please ignore"
    val key = "1234567890123456"
    val otherKey = "6543210987654321"

    val encrypted = Crypto.encryptAES(original, key)
    assertThrows[Crypto.DecryptAuthException] {
      Crypto.decryptAES(encrypted, otherKey)
    }
  }

  // backward compatibility tests
  it should "decrypt with key up to 16 bytes" in {
    val key = "1234567890123456"
    val original = "secretvalue"

    val encrypted = "3d458dc2fe2cd11b9e42b2fee8b51f33"
    Crypto.decryptAES(encrypted, key) shouldEqual original
  }

  it should "not decrypt with key bigger than 16 bytes" in {
    assertThrows[Crypto.AesKeyTooLong](
      Crypto.decryptAES("3d458dc2fe2cd11b9e42b2fee8b51f33", "1234567890123456now_it_became_too_long")
    )
  }

  it should "decrypt with key up to 16 bytes (v1)" in {
    val key = "1234567890123456"
    val original = "secretvalue"

    val encrypted = "1-BNW/+juEl+2PQunvhx44/g=="
    Crypto.decryptAES(encrypted, key) shouldEqual original
  }

  it should "decrypt with key bigger than 16 bytes (v1)" in {
    val key = "1234567890123456" + "now_it_became_too_long"
    val original = "secretvalue"

    val encrypted = "1-1oz9Og7x4cOGLLstyGrD/Q=="
    Crypto.decryptAES(encrypted, key) shouldEqual original
  }

  it should "decrypt with key up to 16 bytes (v2)" in {
    val key = "1234567890123456"
    val original = "secretvalue"

    val encrypted = "2-02LBITbJAb8Mopvgtrd8p3ORc/ipArp6/ozd"
    Crypto.decryptAES(encrypted, key) shouldEqual original
  }

  it should "decrypt with key bigger than 16 bytes (v2)" in {
    val key = "1234567890123456" + "now_it_became_too_long"
    val original = "secretvalue"

    val encrypted = "2-aNJt/st3SsxhFYQ/ybgpM9vudiHQjUf1JqJD"
    Crypto.decryptAES(encrypted, key) shouldEqual original
  }

  it should "decrypt with key up to 16 bytes (v3)" in {
    val key = "1234567890123456"
    val original = "secretvalue"

    val encrypted = "3-DKSxKVcjKln6zqU4ZnmDOfcb8xZr5DXG1o2b/eK7d9DSd+Aa4h80XQ=="
    Crypto.decryptAES(encrypted, key) shouldEqual original
  }

  it should "decrypt with key bigger than 16 bytes (v3)" in {
    val key = "1234567890123456" + "now_it_became_too_long"
    val original = "secretvalue"

    val encrypted = "3-DNU9qTYmX6MTWhpq316+cMn/ZCCuM5Cl1GDiEyWrEc8jt4ew4xA19g=="
    Crypto.decryptAES(encrypted, key) shouldEqual original
  }

  it should "decrypt with max IV length - 255 bytes (v3)" in {
    val key = "1234567890123456"
    val original = "secretvalue"

    val encrypted = encryptV3WithIVLength(key, original, 255)

    Crypto.decryptAES(encrypted, key) shouldEqual original
  }

  it should "decrypt with min IV length - 12 bytes (v3)" in {
    val key = "1234567890123456"
    val original = "secretvalue"

    val encrypted = encryptV3WithIVLength(key, original, 12)

    Crypto.decryptAES(encrypted, key) shouldEqual original
  }

  it should "fail decrypt if IV is too small (< 12 bytes) (v3)" in {
    val key = "1234567890123456"
    val original = "secretvalue"

    val encrypted = encryptV3WithIVLength(key, original, 11)

    intercept[IllegalArgumentException] {
      Crypto.decryptAES(encrypted, key)
    }
  }
  // end of backward compatibility tests

  private def encryptV3WithIVLength(key: String, value: String, ivLength: Int): String = {
    val iv = new Array[Byte](ivLength)
    Random.nextBytes(iv)
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(
      Cipher.ENCRYPT_MODE,
      new SecretKeySpec(DigestUtils.sha256(key.getBytes(UTF_8)).take(16), "AES"),
      new GCMParameterSpec(128, iv),
    )
    val encryptedData = cipher.doFinal(value.getBytes(UTF_8))
    s"3-${ Base64.encodeBase64String(Array(ivLength.toByte) ++ iv ++ encryptedData) }"
  }
}
