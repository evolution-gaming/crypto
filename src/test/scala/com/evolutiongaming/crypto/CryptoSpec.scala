package com.evolutiongaming.crypto

import org.scalatest.{FlatSpec, Matchers}

class CryptoSpec extends FlatSpec with Matchers {
  "Crypto" should "encrypt and decrypt AES" in {
    val secret = "test secret"
    val data = "test data please ignore"

    val encrypted = Crypto.encryptAES(data, secret)
    val decrypted = Crypto.decryptAES(encrypted, secret)

    data shouldEqual decrypted

    val wrongDecrypted = Crypto.decryptAES(encrypted, "wrongSecret12345")
    data should not equal wrongDecrypted
  }
}
