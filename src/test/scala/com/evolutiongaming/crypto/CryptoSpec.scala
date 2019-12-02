package com.evolutiongaming.crypto

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

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

  it should "not give same result when encryption and decryption keys are different" in {
    val original = "test data please ignore"
    val key = "1234567890123456"
    val otherKey = "6543210987654321"

    val encrypted = Crypto.encryptAES(original, key)
    val decrypted = Crypto.decryptAES(encrypted, otherKey)

    original should not equal decrypted
  }

  it should "not give same result when encryption and decryption keys are different (long and substring)" in {
    val original = "test data please ignore"
    val key = "1234567890123456now_it_became_too_long"

    val encrypted = Crypto.encryptAES(original, key)
    val decrypted = Crypto.decryptAES(encrypted, key.take(16))

    original should not equal decrypted
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
  // enb of backward compatibility tests
}
