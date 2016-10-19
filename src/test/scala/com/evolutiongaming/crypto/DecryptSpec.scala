package com.evolutiongaming.crypto

import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}

class DecryptSpec extends FlatSpec with BeforeAndAfterEach with Matchers {
  val correctPassword = "jboss"

  private def decrypt(configFile: String): String = {
    val config = ConfigFactory.load(configFile)
    val password = config.getString("password")
    val decrypted = DecryptConfig(password, config)
    decrypted
  }

  it should "support configuration without system.encryptedPasswords" in {
    val decrypted = decrypt("not-specified.conf")
    decrypted shouldEqual correctPassword
  }

  it should "support unencrypted passwords" in {
    val decrypted = decrypt("unencrypted.conf")
    decrypted shouldEqual correctPassword
  }

  it should "support encrypted passwords" in {
    val decrypted = decrypt("encrypted.conf")
    decrypted shouldEqual correctPassword
  }

  it should "work with plain AES call" in {
    val password = "2-DpBV9t/8a19P5o0fohf//Lpup8DF"
    val secret =  "abcdefghijklmnop"
    Crypto.decryptAES(password, secret) shouldEqual correctPassword
  }

  it should "fail with bad secret" in {
    val decrypted = decrypt("bad-secret.conf")
    decrypted should not equal correctPassword
  }
}
