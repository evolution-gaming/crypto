package com.evolutiongaming.crypto

import com.evolutiongaming.crypto.DecryptConfig.DecryptConfigOps
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DecryptConfigSpec extends AnyFlatSpec with BeforeAndAfterEach with Matchers {
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
    val password = "3-AH3fAKRoJ6PkJC3psZjh9YR/1rmoG6Bu6V2PZKfzfOQjsg=="
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
    config.decryptPath("password") shouldEqual correctPassword
  }

  it should "support unencrypted passwords (Config extension method)" in {
    val config = ConfigFactory.load("unencrypted.conf")
    val password = config.getString("password")
    config.decryptString(password) shouldEqual correctPassword
  }
}
