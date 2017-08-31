package com.evolutiongaming.crypto

import org.scalatest.{FlatSpec, Matchers}

class CryptoSpec extends FlatSpec with Matchers {
  behavior of "Crypto"

  it should "decrypt data encrypted with same AES key" in {
    val secret = "test secret"
    val original = "test data please ignore"

    val encrypted = Crypto.encryptAES(original, secret)
    val decrypted = Crypto.decryptAES(encrypted, secret)

    original shouldEqual decrypted
  }

  it should "not give same result when encryption and decryption keys are different" in {
    val secret = "test secret"
    val original = "test data please ignore"

    val encrypted = Crypto.encryptAES(original, secret)
    val decrypted = Crypto.decryptAES(encrypted, "wrongSecret12345")

    original should not equal decrypted
  }

  it should "not encrypt with key bigger than 16 bytes" in {
    assertThrows[Crypto.AesKeyTooLong](
      Crypto.encryptAES("", "1234567890123456now_it_became_too_long")
    )
  }

  it should "not decrypt with key bigger than 16 bytes" in {
    assertThrows[Crypto.AesKeyTooLong](
      Crypto.decryptAES("", "1234567890123456now_it_became_too_long")
    )
  }
}
