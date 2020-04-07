package com.evolutiongaming.crypto

import com.evolutiongaming.crypto.ConfigDecrypter.syntax._
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ConfigDecrypterSpec extends AnyFlatSpec with BeforeAndAfterEach with Matchers {
  val correctPassword = "jboss"

  private def decrypt(configFile: String): String = {
    val config = ConfigFactory.load(configFile)
    val password = config.getString("password")
    val decrypted = config.decryptStringUnsafe(password)
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
    val password = "3-DG4i9kr/lboBjhjgwMsT/2f1Jc6vI4O9VucM+ucM7TDi9Q=="
    val secret = "abcdefghijklmnop"
    Crypto.decryptAES(password, secret) shouldEqual correctPassword
  }

  it should "fail with bad secret" in {
    intercept[Crypto.DecryptAuthException] {
      decrypt("bad-secret.conf")
    }
  }

  it should "decrypt encrypted passwords by path (Config extension method)" in {
    val config = ConfigFactory.load("encrypted.conf")
    config.decryptPathUnsafe("password") shouldEqual correctPassword
  }

  it should "support unencrypted passwords (Config extension method)" in {
    val config = ConfigFactory.load("unencrypted.conf")
    val password = config.getString("password")
    config.decryptStringUnsafe(password) shouldEqual correctPassword
  }
}
